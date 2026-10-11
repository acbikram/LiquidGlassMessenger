package com.example.liquidglass

import android.content.Context
import android.os.Build
import android.util.Base64
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.drinkless.tdlib.Client
import org.drinkless.tdlib.TdApi
import java.security.SecureRandom

data class TelegramChat(val id: Long, val title: String, val lastMessage: String = "", val unread: Int = 0)

sealed interface TelegramStatus {
    data object NeedsApiCredentials : TelegramStatus
    data object Connecting : TelegramStatus
    data object NeedsPhone : TelegramStatus
    data object NeedsCode : TelegramStatus
    data object NeedsPassword : TelegramStatus
    data object NeedsEmail : TelegramStatus
    data object Ready : TelegramStatus
    data class Error(val message: String) : TelegramStatus
}

object TelegramClient {
    private lateinit var appContext: Context
    private var client: Client? = null
    private var apiId: Int = 0
    private var apiHash: String = ""
    private val _status = MutableStateFlow<TelegramStatus>(TelegramStatus.NeedsApiCredentials)
    val status = _status.asStateFlow()
    private val _chats = MutableStateFlow<List<TelegramChat>>(emptyList())
    val chats = _chats.asStateFlow()
    private val _messages = MutableStateFlow<Map<Long, List<String>>>(emptyMap())
    val messages = _messages.asStateFlow()
    private val titles = mutableMapOf<Long, String>()
    private val messageCache = mutableMapOf<Long, MutableList<String>>()

    private fun securePrefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        "liquid_glass_secure",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun initialize(context: Context) {
        appContext = context.applicationContext
        val prefs = securePrefs(appContext)
        apiId = prefs.getString("telegram_api_id", "")?.toIntOrNull() ?: 0
        apiHash = prefs.getString("telegram_api_hash", "") ?: ""
        if (apiId > 0 && apiHash.isNotBlank()) start()
        else _status.value = TelegramStatus.NeedsApiCredentials
    }

    fun saveApiCredentials(context: Context, id: String, hash: String): Boolean {
        val parsed = id.trim().toIntOrNull() ?: return false
        if (parsed <= 0 || hash.trim().length < 20) return false
        appContext = context.applicationContext
        securePrefs(appContext).edit().putString("telegram_api_id", parsed.toString())
            .putString("telegram_api_hash", hash.trim()).apply()
        apiId = parsed
        apiHash = hash.trim()
        start()
        return true
    }

    private fun databaseKey(): ByteArray {
        val prefs = securePrefs(appContext)
        var encoded = prefs.getString("tdlib_db_key", null)
        if (encoded == null) {
            val key = ByteArray(32).also { SecureRandom().nextBytes(it) }
            encoded = Base64.encodeToString(key, Base64.NO_WRAP)
            prefs.edit().putString("tdlib_db_key", encoded).apply()
        }
        return Base64.decode(encoded, Base64.NO_WRAP)
    }

    @Synchronized private fun start() {
        if (client != null) return
        _status.value = TelegramStatus.Connecting
        try {
            client = Client.create({ update -> handle(update) }, { error -> _status.value = TelegramStatus.Error(error.message ?: "Telegram client error") }, { error -> _status.value = TelegramStatus.Error(error.message ?: "Telegram client error") })
            client?.send(TdApi.GetAuthorizationState()) { result -> handleAuthorization(result) }
        } catch (t: Throwable) {
            _status.value = TelegramStatus.Error(t.message ?: "Could not start TDLib")
        }
    }

    private fun handle(update: TdApi.Object) {
        when (update) {
            is TdApi.UpdateAuthorizationState -> handleAuthorization(update.authorizationState)
            is TdApi.UpdateNewChat -> {
                val c = update.chat
                titles[c.id] = c.title
                publishChats()
            }
            is TdApi.UpdateChatTitle -> {
                titles[update.chatId] = update.title
                publishChats()
            }
            is TdApi.UpdateNewMessage -> {
                val chatId = update.message.chatId
                val content = when (val c = update.message.content) {
                    is TdApi.MessageText -> c.text.text
                    is TdApi.MessagePhoto -> "📷 Photo"
                    is TdApi.MessageVideo -> "🎬 Video"
                    is TdApi.MessageDocument -> "📎 File"
                    is TdApi.MessageVoiceNote -> "🎤 Voice message"
                    is TdApi.MessageSticker -> "Sticker"
                    else -> "Message"
                }
                val list = messageCache.getOrPut(chatId) { mutableListOf() }
                list.add(content)
                if (list.size > 300) list.removeAt(0)
                _messages.value = messageCache.mapValues { it.value.toList() }
                publishChats()
            }
            is TdApi.UpdateChatLastMessage -> publishChats()
            is TdApi.UpdateChatReadInbox -> publishChats()
        }
    }

    private fun handleAuthorization(state: TdApi.Object) {
        when (state) {
            is TdApi.AuthorizationStateWaitTdlibParameters -> {
                val dir = appContext.filesDir.absolutePath
                client?.send(TdApi.SetTdlibParameters(
                    false, "$dir/telegram-db", "$dir/telegram-files", databaseKey(),
                    true, true, true, true, apiId, apiHash, "en",
                    Build.MODEL ?: "Android", Build.VERSION.RELEASE ?: "Android",
                    "LiquidGlassMessenger/0.2.0"
                )) { result -> if (result is TdApi.Error) _status.value = TelegramStatus.Error(result.message) }
            }
            is TdApi.AuthorizationStateWaitPhoneNumber -> _status.value = TelegramStatus.NeedsPhone
            is TdApi.AuthorizationStateWaitCode -> _status.value = TelegramStatus.NeedsCode
            is TdApi.AuthorizationStateWaitPassword -> _status.value = TelegramStatus.NeedsPassword
            is TdApi.AuthorizationStateWaitEmailAddress, is TdApi.AuthorizationStateWaitEmailCode -> _status.value = TelegramStatus.NeedsEmail
            is TdApi.AuthorizationStateReady -> {
                _status.value = TelegramStatus.Ready
                client?.send(TdApi.LoadChats(TdApi.ChatListMain(), 100)) { }
                client?.send(TdApi.GetChats(TdApi.ChatListMain(), 100)) { result ->
                    if (result is TdApi.Chats) {
                        result.chatIds.forEach { id -> client?.send(TdApi.GetChat(id)) { chatResult ->
                            if (chatResult is TdApi.Chat) { titles[chatResult.id] = chatResult.title; publishChats() }
                        } }
                    }
                }
            }
            is TdApi.AuthorizationStateLoggingOut, is TdApi.AuthorizationStateClosing -> _status.value = TelegramStatus.Connecting
            is TdApi.AuthorizationStateClosed -> { client = null; _status.value = TelegramStatus.NeedsPhone }
        }
    }

    fun submitPhone(phone: String) {
        val normalized = phone.trim()
        if (normalized.isBlank()) {
            _status.value = TelegramStatus.NeedsPhone
            return
        }
        if (normalized.count { it.isDigit() } < 7) {
            _status.value = TelegramStatus.Error("Enter a complete phone number including country code, for example +9665XXXXXXXX.")
            return
        }
        client?.send(TdApi.SetAuthenticationPhoneNumber(normalized, null)) { result ->
            if (result is TdApi.Error) _status.value = TelegramStatus.Error(result.message)
        }
    }
    fun submitCode(code: String) {
        client?.send(TdApi.CheckAuthenticationCode(code.trim())) { result -> if (result is TdApi.Error) _status.value = TelegramStatus.Error(result.message) }
    }
    fun submitPassword(password: String) {
        client?.send(TdApi.CheckAuthenticationPassword(password)) { result -> if (result is TdApi.Error) _status.value = TelegramStatus.Error(result.message) }
    }
    fun submitEmail(email: String) {
        client?.send(TdApi.SetAuthenticationEmailAddress(email.trim())) { result -> if (result is TdApi.Error) _status.value = TelegramStatus.Error(result.message) }
    }
    fun sendMessage(chatId: Long, text: String) {
        if (text.isBlank()) return
        val content = TdApi.InputMessageText(TdApi.FormattedText(text.trim(), emptyArray()), null, true)
        client?.send(TdApi.SendMessage(chatId, null, null, null, null, content)) { result ->
            if (result is TdApi.Error) _status.value = TelegramStatus.Error(result.message)
        }
    }
    fun openChat(chatId: Long) {
        client?.send(TdApi.GetChatHistory(chatId, 0, 0, 50, false)) { result ->
            if (result is TdApi.Messages) {
                val list = result.messages.reversed().mapNotNull { m ->
                    when (val c = m.content) {
                        is TdApi.MessageText -> (if (m.isOutgoing) "YOU: " else "") + c.text.text
                        is TdApi.MessagePhoto -> "📷 Photo"
                        is TdApi.MessageVideo -> "🎬 Video"
                        else -> null
                    }
                }
                messageCache[chatId] = list.toMutableList()
                _messages.value = messageCache.mapValues { it.value.toList() }
            }
        }
    }
    fun logout() {
        client?.send(TdApi.LogOut()) { }
        client = null
        titles.clear()
        messageCache.clear()
        _chats.value = emptyList()
        _messages.value = emptyMap()
        _status.value = TelegramStatus.NeedsPhone
        start()
    }
    private fun publishChats() {
        _chats.value = titles.map { (id, title) ->
            val list = messageCache[id].orEmpty()
            TelegramChat(id, title, list.lastOrNull() ?: "")
        }.sortedByDescending { it.id }
    }
}

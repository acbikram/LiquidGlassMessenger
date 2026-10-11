package com.example.liquidglass

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.collectLatest

private val Ice = Color(0xFF9BEFFF)
private val Ink = Color(0xFF070B16)
private val Violet = Color(0xFF9B8CFF)
private val Teal = Color(0xFF55D9D0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        TelegramClient.initialize(applicationContext)
        setContent { LiquidGlassApp(onOpenTelegramPortal = { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://my.telegram.org"))) }) }
    }
}

@Composable
private fun LiquidGlassApp(onOpenTelegramPortal: () -> Unit) {
    val context = LocalContext.current
    val status by TelegramClient.status.collectAsState()
    val chats by TelegramClient.chats.collectAsState()
    val allMessages by TelegramClient.messages.collectAsState()
    var apiId by rememberSaveable { mutableStateOf("") }
    var apiHash by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var selected by remember { mutableStateOf<TelegramChat?>(null) }
    var search by rememberSaveable { mutableStateOf("") }
    var dark by rememberSaveable { mutableStateOf(true) }
    var glassStrength by rememberSaveable { mutableFloatStateOf(.72f) }
    var pendingPhotoChatId by remember { mutableStateOf<Long?>(null) }
    val photoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val chatId = pendingPhotoChatId
        if (uri != null && chatId != null) TelegramClient.sendPhoto(chatId, uri)
        pendingPhotoChatId = null
    }
    val fg = if (dark) Color.White else Color(0xFF16243A)
    val base = if (dark) Ink else Color(0xFFEAF1FC)
    val glassColor = (if (dark) Color(0xFF172A42) else Color.White).copy(alpha = glassStrength.coerceIn(.3f, .92f))

    Box(Modifier.fillMaxSize().background(base)) {
        // Layered luminous fields create a glassmorphism look without relying on unsupported blur APIs.
        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF3859A6).copy(alpha = if (dark) .46f else .18f), Color.Transparent), radius = 900f)))
        Box(Modifier.fillMaxSize().background(Brush.radialGradient(listOf(Color(0xFF4DCEC9).copy(alpha = if (dark) .18f else .10f), Color.Transparent), center = androidx.compose.ui.geometry.Offset(850f, 300f), radius = 700f)))
        Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(horizontal = 18.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(45.dp).clip(RoundedCornerShape(16.dp)).background(Brush.linearGradient(listOf(Ice.copy(alpha = .95f), Violet.copy(alpha = .8f)))), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Forum, null, tint = Ink, modifier = Modifier.size(24.dp))
                }
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    Text("Telegram Pro", color = fg, fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = (-.7).sp)
                    Text("LIQUID GLASS MESSENGER", color = fg.copy(alpha = .55f), fontSize = 9.sp, letterSpacing = 1.5.sp)
                }
                if (status is TelegramStatus.Ready) {
                    IconButton(onClick = { selected = null; search = "" }) { Icon(Icons.Default.Search, "Search chats", tint = fg) }
                    IconButton(onClick = { TelegramClient.logout() }) { Icon(Icons.Default.Logout, "Log out", tint = fg) }
                }
            }

            if (status is TelegramStatus.Ready) {
                if (selected == null) {
                    Row(Modifier.fillMaxWidth().clip(CircleShape).background(glassColor).border(1.dp, Brush.horizontalGradient(listOf(Color.White.copy(alpha=.28f), Color.White.copy(alpha=.04f))), CircleShape).padding(horizontal = 16.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Search, null, tint = fg.copy(alpha=.55f), modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        BasicTextField(search, { search = it }, Modifier.weight(1f), singleLine = true, textStyle = MaterialTheme.typography.bodyLarge.copy(color = fg), decorationBox = { inner -> Box { if (search.isEmpty()) Text("Search your Telegram chats", color = fg.copy(alpha=.45f)); inner() } })
                    }
                    Row(Modifier.fillMaxWidth().padding(vertical = 17.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Your chats", color = fg, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("${chats.size} synced", color = Teal, fontSize = 12.sp)
                    }
                    if (chats.isEmpty()) {
                        GlassPanel(glassColor, Modifier.fillMaxWidth().weight(1f)) {
                            Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.CloudSync, null, tint = Ice, modifier = Modifier.size(48.dp))
                                    Spacer(Modifier.height(14.dp))
                                    Text("Syncing your Telegram", color = fg, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                                    Text("Your real conversations will appear here when TDLib finishes loading.", color = fg.copy(alpha=.6f), fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(9.dp), contentPadding = PaddingValues(bottom = 12.dp)) {
                            items(chats.filter { it.title.contains(search, true) || it.lastMessage.contains(search, true) }, key = { it.id }) { chat ->
                                GlassPanel(glassColor, Modifier.fillMaxWidth().clickable { selected = chat; TelegramClient.openChat(chat.id) }) {
                                    Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Avatar(chat.title, 52)
                                        Column(Modifier.weight(1f).padding(start = 13.dp)) {
                                            Text(chat.title, color = fg, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Spacer(Modifier.height(5.dp))
                                            Text(chat.lastMessage.ifBlank { "Open conversation" }, color = fg.copy(alpha = .57f), fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        Icon(Icons.Default.ChevronRight, null, tint = fg.copy(alpha = .4f))
                                    }
                                }
                            }
                        }
                    }
                    GlassPanel(glassColor, Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(horizontal = 15.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VerifiedUser, null, tint = Teal, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Connected directly through TDLib", color = fg.copy(alpha = .72f), fontSize = 11.sp, modifier = Modifier.weight(1f))
                            IconButton(onClick = { dark = !dark }, modifier = Modifier.size(34.dp)) { Icon(if (dark) Icons.Default.LightMode else Icons.Default.DarkMode, "Toggle appearance", tint = fg) }
                        }
                    }
                } else {
                    val chat = selected!!
                    Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { selected = null }) { Icon(Icons.Default.ArrowBack, "Back", tint = fg) }
                        Avatar(chat.title, 40)
                        Column(Modifier.weight(1f).padding(start = 10.dp)) { Text(chat.title, color = fg, fontWeight = FontWeight.SemiBold); Text("Telegram conversation", color = Teal, fontSize = 11.sp) }
                        IconButton(onClick = { glassStrength = if (glassStrength > .8f) .55f else .9f }) { Icon(Icons.Default.BlurOn, "Adjust glass", tint = fg) }
                    }
                    val messages = allMessages[chat.id].orEmpty()
                    LazyColumn(Modifier.weight(1f).fillMaxWidth(), reverseLayout = false, contentPadding = PaddingValues(vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        items(messages) { message ->
                            val mine = message.startsWith("YOU: ")
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
                                GlassPanel(if (mine) Color(0xFF1C6171).copy(alpha=.88f) else glassColor, Modifier.widthIn(max = 310.dp)) {
                                    Text(message.removePrefix("YOU: "), color = fg, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp))
                                }
                            }
                        }
                    }
                    var draft by rememberSaveable(chat.id) { mutableStateOf("") }
                    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp).clip(CircleShape).background(glassColor).border(1.dp, Color.White.copy(alpha=.15f), CircleShape).padding(horizontal = 6.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { pendingPhotoChatId = chat.id; photoPicker.launch("image/*") }) {
                            Icon(Icons.Default.AttachFile, "Attach photo", tint = Ice)
                        }
                        BasicTextField(draft, { draft = it }, Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 10.dp), singleLine = true, textStyle = MaterialTheme.typography.bodyLarge.copy(color = fg), decorationBox = { inner -> Box { if (draft.isEmpty()) Text("Message", color = fg.copy(alpha=.45f)); inner() } })
                        IconButton(onClick = { if (draft.isNotBlank()) { TelegramClient.sendMessage(chat.id, draft); draft = "" } }) { Icon(Icons.AutoMirrored.Filled.Send, "Send message", tint = Ice) }
                    }
                }
            } else {
                val err = (status as? TelegramStatus.Error)?.message
                GlassPanel(glassColor, Modifier.fillMaxWidth().weight(1f)) {
                    Column(Modifier.fillMaxSize().padding(22.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.Start) {
                        Box(Modifier.size(58.dp).clip(RoundedCornerShape(20.dp)).background(Brush.linearGradient(listOf(Ice, Violet))), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Lock, null, tint = Ink, modifier = Modifier.size(29.dp))
                        }
                        Spacer(Modifier.height(20.dp))
                        Text(
                            when (status) {
                                TelegramStatus.NeedsApiCredentials -> "Connect your Telegram"
                                TelegramStatus.NeedsPhone -> "Your phone number"
                                TelegramStatus.NeedsCode -> "Verification code"
                                TelegramStatus.NeedsPassword -> "Two-step verification"
                                TelegramStatus.NeedsEmail -> "Email verification"
                                TelegramStatus.Connecting -> "Connecting securely…"
                                is TelegramStatus.Error -> "Telegram needs attention"
                                TelegramStatus.Ready -> "Connected"
                            }, color = fg, fontSize = 25.sp, fontWeight = FontWeight.Bold, letterSpacing = (-.5).sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            when (status) {
                                TelegramStatus.NeedsApiCredentials -> "Add your Telegram API credentials. They are stored encrypted on this device and are never added to the GitHub source."
                                TelegramStatus.NeedsPhone -> "Enter your number with country code. Telegram will send a sign-in code through its official authorization flow."
                                TelegramStatus.NeedsCode -> "Enter the login code from Telegram. Never share this code with anyone."
                                TelegramStatus.NeedsPassword -> "Enter your Telegram two-step verification password, if enabled."
                                TelegramStatus.NeedsEmail -> "Enter the email address or code requested by Telegram."
                                TelegramStatus.Connecting -> "Starting the Telegram client and preparing encrypted local storage."
                                is TelegramStatus.Error -> "Check the message below. Your account credentials are not displayed here."
                                TelegramStatus.Ready -> ""
                            }, color = fg.copy(alpha = .63f), fontSize = 13.sp, lineHeight = 19.sp
                        )
                        Spacer(Modifier.height(20.dp))
                        when (status) {
                            TelegramStatus.NeedsApiCredentials -> {
                                OutlinedField("API ID", apiId, { apiId = it.filter(Char::isDigit) }, fg)
                                Spacer(Modifier.height(10.dp))
                                OutlinedField("API Hash", apiHash, { apiHash = it }, fg)
                                Spacer(Modifier.height(8.dp))
                                TextButton(onClick = onOpenTelegramPortal) { Text("Get credentials from my.telegram.org ↗", color = Ice) }
                                Spacer(Modifier.height(8.dp))
                                GlassAction("Save securely & continue", enabled = apiId.isNotBlank() && apiHash.isNotBlank()) {
                                    if (!TelegramClient.saveApiCredentials(context = context, id = apiId, hash = apiHash)) { }
                                }
                            }
                            TelegramStatus.NeedsPhone -> {
                                OutlinedField("Phone number (+country code)", phone, { phone = it }, fg)
                                Spacer(Modifier.height(14.dp))
                                GlassAction("Send sign-in code", enabled = phone.trim().length >= 7) { TelegramClient.submitPhone(phone) }
                            }
                            TelegramStatus.NeedsCode -> {
                                OutlinedField("Telegram login code", code, { code = it }, fg)
                                Spacer(Modifier.height(14.dp))
                                GlassAction("Verify code", enabled = code.isNotBlank()) { TelegramClient.submitCode(code) }
                            }
                            TelegramStatus.NeedsPassword -> {
                                OutlinedField("Two-step password", password, { password = it }, fg, password = true)
                                Spacer(Modifier.height(14.dp))
                                GlassAction("Verify password", enabled = password.isNotBlank()) { TelegramClient.submitPassword(password) }
                            }
                            TelegramStatus.NeedsEmail -> {
                                OutlinedField("Email address or code", email, { email = it }, fg)
                                Spacer(Modifier.height(14.dp))
                                GlassAction("Continue", enabled = email.isNotBlank()) {
                                    if (email.contains("@")) TelegramClient.submitEmail(email)
                                    else Unit
                                }
                            }
                            TelegramStatus.Connecting -> CircularProgressIndicator(color = Ice)
                            is TelegramStatus.Error -> {
                                Text(err ?: "Unknown error", color = Color(0xFFFFA7B4), fontSize = 13.sp)
                                Spacer(Modifier.height(14.dp))
                                // Keep a visible phone input on the error screen too, so a missing
                                // phone number can never be submitted from a button with no field.
                                OutlinedField("Phone number (+country code)", phone, { phone = it }, fg)
                                Spacer(Modifier.height(12.dp))
                                GlassAction("Try phone sign-in", enabled = phone.trim().length >= 7) {
                                    TelegramClient.submitPhone(phone)
                                }
                            }
                            TelegramStatus.Ready -> Unit
                        }
                        if (status !is TelegramStatus.Connecting) {
                            Spacer(Modifier.height(18.dp))
                            Text("Your sign-in code and password are sent directly to Telegram via TDLib. Never paste them into GitHub or a public chat.", color = fg.copy(alpha = .45f), fontSize = 11.sp, lineHeight = 16.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable private fun GlassPanel(color: Color, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.clip(RoundedCornerShape(25.dp)).background(Brush.linearGradient(listOf(color.copy(alpha = (color.alpha + .09f).coerceAtMost(1f)), color.copy(alpha = .72f), color.copy(alpha = .9f)))).border(1.dp, Brush.linearGradient(listOf(Color.White.copy(alpha = .30f), Color.White.copy(alpha = .04f), Ice.copy(alpha = .15f))), RoundedCornerShape(25.dp)), content = content)
}
@Composable private fun Avatar(title: String, size: Int) {
    val initials = title.split(" ").mapNotNull { it.firstOrNull()?.toString() }.take(2).joinToString("").ifBlank { "T" }
    Box(Modifier.size(size.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Ice.copy(alpha = .85f), Violet.copy(alpha = .9f), Teal.copy(alpha = .65f)))), contentAlignment = Alignment.Center) {
        Text(initials, color = Ink, fontSize = (size / 3.1f).sp, fontWeight = FontWeight.Bold)
    }
}
@Composable private fun OutlinedField(label: String, value: String, onValueChange: (String) -> Unit, fg: Color, password: Boolean = false) {
    Column {
        Text(label, color = fg.copy(alpha = .6f), fontSize = 12.sp)
        Spacer(Modifier.height(6.dp))
        BasicTextField(value, onValueChange, Modifier.fillMaxWidth().clip(RoundedCornerShape(15.dp)).background(Color.White.copy(alpha = .07f)).border(1.dp, Color.White.copy(alpha = .15f), RoundedCornerShape(15.dp)).padding(horizontal = 14.dp, vertical = 15.dp), singleLine = true, textStyle = MaterialTheme.typography.bodyLarge.copy(color = fg), visualTransformation = if (password) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None, decorationBox = { inner -> Box { if (value.isEmpty()) Text(label, color = fg.copy(alpha = .35f)); inner() } })
    }
}
@Composable private fun GlassAction(label: String, enabled: Boolean = true, action: () -> Unit) {
    Box(Modifier.fillMaxWidth().clip(CircleShape).background(if (enabled) Brush.linearGradient(listOf(Ice, Violet)) else Brush.linearGradient(listOf(Color.Gray, Color.DarkGray))).clickable(enabled = enabled, onClick = action).padding(horizontal = 18.dp, vertical = 15.dp), contentAlignment = Alignment.Center) {
        Text(label, color = if (enabled) Ink else Color.White.copy(alpha = .55f), fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

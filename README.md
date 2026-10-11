# LiquidGlassMessenger

An Android Telegram client prototype built with Jetpack Compose and TDLib, styled with a premium glassmorphism interface.

## Implemented in this iteration
- TDLib-backed authorization flow for API ID/hash, phone number, login code, and two-step password.
- TDLib local database encryption key generated per install and stored in Android encrypted preferences.
- Telegram main chat list, text/media previews, conversation history, and text sending.
- Compose glass panels, luminous gradients, reflective borders, and adjustable surface transparency.
- GitHub Actions builds a debug APK on each push to main.

## First-time setup
1. Create Telegram API credentials at https://my.telegram.org (API development tools).
2. Download the APK from the LiquidGlassMessenger-debug-apk artifact on the GitHub Actions page.
3. Enter your API ID and API hash inside the app.
4. Sign in with your phone number and enter the verification code Telegram provides. If enabled, enter your two-step verification password.

Credentials and TDLib's database encryption key are stored in Android encrypted preferences. Login codes and passwords are not committed to GitHub. Never publish your Telegram session data.

## Important limitations
This is an early custom-client implementation, not a feature-complete replacement for official Telegram. This iteration focuses on authorization, chat synchronization, text messaging, and the glass UI. Media sending, calls, notifications/background sync, full contacts management, secret chats, advanced message actions, and production hardening still require implementation and device testing. The TDLib Android AAR is community-maintained; review its provenance and license before public distribution.

## Build
GitHub Actions runs gradle --no-daemon assembleDebug and uploads app/build/outputs/apk/debug/app-debug.apk.

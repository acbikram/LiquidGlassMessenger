# LiquidGlassMessenger

An Android Telegram client prototype built with Jetpack Compose and TDLib, with a Liquid Glass-inspired interface.

## Current version

- App version: 0.3.0 (versionCode 3)
- Package ID: `com.example.liquidglass`
- GitHub Actions builds the app on every push to `main`.

## Important: configure stable signing once

Android only accepts an in-place APK update when the package ID and signing certificate match the installed app, and the incoming version code is not lower. Debug builds created on separate GitHub runners can have different automatically generated signing keys. To make future updates install over the existing app, configure a persistent signing key in GitHub Actions.

In the repository, open **Settings → Secrets and variables → Actions → New repository secret** and add these secrets:

- `SIGNING_KEY_BASE64`: the full single-line Base64 contents of the private release keystore file supplied to the repository owner.
- `SIGNING_KEY_PASSWORD`: the keystore/key password.
- `SIGNING_KEY_ALIAS`: `liquidglass`.

Never commit the keystore, its Base64 contents, or its password to the repository. Keep a secure backup of the original `.jks` file. After all three secrets are configured, the workflow uploads `LiquidGlassMessenger-release-apk`; that signed release APK is the update channel for future versions. Until then, the workflow deliberately labels its debug artifact as temporary.

**One-time transition note:** the previously installed 0.2.0 APK was signed by an ephemeral GitHub Actions debug key whose private key is not available for reuse. After configuring the persistent key, you may need to uninstall that old APK one final time before installing the first persistent-key release. This removes local app data, so make sure you have anything important backed up. After that transition, use the signed release APK for every update and do not switch back to debug APKs.

## First-time Telegram setup

1. Create Telegram API credentials at https://my.telegram.org (API development tools).
2. Download the APK from the latest successful GitHub Actions run.
3. Enter the API ID and API hash inside the app.
4. Sign in with your phone number and enter the Telegram verification code. If enabled, enter your two-step verification password.

Credentials and TDLib's database encryption key are stored in Android encrypted preferences. Never publish your Telegram session data, API hash, or login codes.

## Current implementation scope

Implemented: TDLib authorization, chat list/history, text message sending, basic message previews, and the Liquid Glass-inspired Compose interface.

Still requires implementation and real-device testing: media uploads, voice/video calls, reliable background notifications and sync, contacts, full groups/channels/topics, message replies/edits/deletes/reactions/pinning/forwarding, polls, location sharing, multi-account support, and production hardening. Secret chats and other end-to-end encrypted features have distinct Telegram protocol constraints. This is not yet a feature-complete replacement for the official Telegram app.

The TDLib Android AAR currently used is community-maintained; review its provenance and license before public distribution.

# Zunévio privacy and updates

Zunévio connects directly to the music, lyrics, artwork and optional account/integration services you use. Those providers receive the network requests needed for their features and apply their own policies. Signing in is optional for guest/local use; signed-in capabilities may store session credentials on the device. See the source and in-app provider disclosures before using integrations.

The official update checker contacts GitHub's public Releases API on app open, when requested, and approximately every 12 hours while background execution/network access are allowed. GitHub receives normal network metadata such as IP address and app User-Agent. No private repository address, GitHub access token, or signing private key is included in the app.

Notifications require Android permission and system/channel settings. Checks are periodic, not real-time push; battery restrictions, force-stop and connectivity can delay them. APKs download only after your tap; checksums, package identity, version code and signing certificate are checked before Android's installer is opened. Installation needs your confirmation and Android's per-app install permission. Keep Play Protect enabled.

This file describes app behavior, not the privacy policies of external services. Music playback availability depends on providers and permissions to the content.

## Optional in-app voice search (1.2.5 build 44)

Microphone access is requested only when the user explicitly opens voice search. The installed Android speech service may transmit spoken audio to its provider according to its own privacy settings. The app receives recognized text and language/confidence metadata, not microphone recordings; it does not save microphone audio or run always-on recording. Leaving the app or dismissing the panel cancels and destroys recognition. Audio/video music pauses before listening.

On Android 14+, the app requests automatic language detection/switching without a language allowlist. Actual languages and behavior depend on the installed speech service and its available models. No language is silently forced on unsupported devices. Unverified or uncertain recognition requires review before submitting its text to the same music-search path used for typed text. Humming recognition and guaranteed singing identification are not implemented.

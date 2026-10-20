# Zunéviu v1.2.6 · Build 51

## What changed since v1.2.5

### Spotify sign-in
- Moved sign-in into a dedicated native Android Activity, outside the animated music screen.
- One WebView per Activity; progress updates and cookie polling do not reload or replace the page. Only an explicit Retry reloads it.
- Keep progress-bar space stable, apply keyboard/system insets, and restore navigation state on Activity recreation.
- Accept a session only after reaching the trusted Spotify web-player origin; an old cookie on the login form no longer closes sign-in prematurely.
- Keep normal responsive provider layout/mobile user agent, main-frame error reporting, a visible Retry action, HTTPS origin checks and default TLS protections.

### Google sign-in
- Wait for session-cookie writes before opening the signed-in page, with SPA readiness handling and improved Back/disposal handling. Device/account acceptance still varies.

### Video playback and lyrics
- Resolve catalogue video availability before changing playback. Missing video shows “Video unavailable” while preserving/restoring audio.
- Use a static “Lyrics >” entry in video mode to open the lyrics panel instead of a live lyric strip.
- Bounded ranged stream requests, tuned buffering and automatic quality downshift on stalls improve network handling; no zero-buffering guarantee.
- Existing audio lyric timing/calibration remains unchanged.

### Settings, branding and cache
- Improve equalizer switch contrast/accessibility and Zunéviu branding/source text, including the red Z styling on Library cards.
- Default disposable audio cache reduced to 128 MiB; image 64 MiB, canvas 64 MiB, remote art 32 MiB. Existing explicit audio-cache choices are preserved and can be changed in Settings. These are component budgets, not a whole-app storage cap.
- Safely remove already-installed/older matching update APKs and abandoned partial update downloads. Saved songs, playlists and login/session data are not targets of this cleanup.

### Genius reliability
- Cancellable requests; explicit temporarily-unavailable status and manual retry; stop repeated blocked search attempts and retain other enabled provider fallbacks.
- More robust parsing of supported search response shapes and trusted result URL checks.

### Gallery and source distribution
- Replace the three gallery cards with the approved new owner-provided screenshots. The existing first banner is unchanged.
- Full corresponding application source, dependencies/build inputs and notices are public in [source/ at v1.2.6](https://github.com/amitsami/zunevio-releases/tree/v1.2.6/source). No separately uploaded corresponding-source ZIP asset for this release. GitHub's automatic source archives include that tree.

## Validation and known limitations
- Signed with the same owner certificate; package `com.zunevio.music`, version `1.2.6`, version code `51`, Android 8.0+.
- 1,323 of 1,324 automated tests passed, zero skipped. The unchanged real-network Genius live-fetch test fails with HTTP 403 / provider blocked. Genius access has NOT been restored by these error-handling changes.
- Spotify navigation/policy and lifecycle wiring tests passed, but no authenticated end-to-end login on the affected Android phone was performed by the agent. Do not interpret unit tests or release approval as proof of universal login success or a bug-free build.
- No physical-device audiovisual-sync, universal compatibility, Play Protect certification or zero-buffering guarantee is claimed. Existing R8/Kotlin metadata compatibility warnings remain.

## Install
Download `Zuneviu-1.2.6-universal.apk` below. Keep Play Protect enabled and review Android's installation prompts. Existing installs on the owner release signing chain can update normally; do not uninstall to update.

Maintained by [Amit Hasan Sami](https://github.com/amitsami). Upstream copyrights, licenses and notices remain in the source and documentation.

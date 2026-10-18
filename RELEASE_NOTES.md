# Zunéviu 1.2.5 — Build 47

## What's new since 1.2.3

### Live lyrics and timing
- Restore the v1.2.3 audio lyric-clock/presentation behavior and make the compact audio caption switch without its previous entrance delay.
- Set the visible Android audio Lyrics offset default to **−0.4s**, based on the maintainer's device testing. Migrate previous zero/default settings once; preserve existing nonzero manual offsets without double application. An explicitly saved audio zero remains zero on subsequent launches.
- Keep audio and video Lyrics offsets separate, with consistent highlighting and lyric-line seeking.
- Restore timed fallback lyrics during inline music video playback: line, word and gap cues are no longer erased when exact-video captions are unavailable. Exact-video captions remain preferred, and disabled sources remain disabled.

### Voice search
- Remove the fixed Bengali/Hindi/English language picker. On Android 14 and later, request automatic language detection/switching from a compatible installed speech service without a fixed language allowlist.
- Check speech-service capabilities, retain uncertain recognition for review, and handle timeout, cancellation and retry states. Supported languages/models depend on the installed service; this is not humming/song fingerprint recognition or Spotify's proprietary engine.
- The app still supports Android 8 and later. This automatic-language voice flow needs Android 14+; typed search remains available on older versions.

### Credits and source availability
- Present **Developed and maintained by Amit Hasan Sami** and **GitHub @amitsami** at the top of Licenses & source.
- Provide the official releases/source-download link directly. Retain original-author attribution, licenses and warranty notices in a separate **Open-source notices** view.

## Retained playback features
Inline music video in the player/mini-player, Back to audio, existing transport/seek controls and Auto/Low/Medium/High video-quality settings remain available. This release does not replace the audio/video engines.

## Validation and known limitations
- 1,307 automated tests: **1,306 passed**; the existing live Genius network search/scraping test failed. Android lint: **0 errors, 687 warnings**.
- The APK's production signature/package and the matching source archive were verified. Package: `com.zunevio.music`; versionName `1.2.5`; versionCode `47`.
- Restoring fallback timestamps is **not** acoustic alignment. Alternate video intros/edits and inaccurate provider timings may still need the independent video Lyrics offset. Perfect video/music/lyrics synchronization is not claimed.
- Physical-device audiovisual sync was not verified. Software-emulator screenshot attempts failed at Android system startup/installation; no new Build 47 screenshots are included. Existing repository galleries refer to earlier versions.
- Install over the existing production app without uninstalling. Android requires installation confirmation; no silent install, Play Protect certification or universal performance guarantee is claimed.

- If you already installed an earlier local **1.2.5** build, download/install Build 47 manually: update discovery compares version names, so it may not offer another build with the same 1.2.5 name. Existing public 1.2.3 users can discover the newer 1.2.5 release.

## Downloads and source
The APK below is the same signed Build 47 file delivered before publication (158,895,238 bytes). Both repositories carry identical release assets, including the corresponding-source ZIP, validation report, license notices and SHA-256 checksums. No signing keys or credentials are included.

Developed and maintained by [Amit Hasan Sami](https://github.com/amitsami).

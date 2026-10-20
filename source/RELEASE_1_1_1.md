# Zunévio 1.1.1 — review candidate

Version name `1.1.1`, version code `34`. Android 8.0/API 26 and later; universal APK includes arm64-v8a, armeabi-v7a, x86 and x86_64. Package `com.zunevio.music.preview3`, same private test signing identity as the supplied 1.1.0. Not published.

## Changed

- Settings now starts with account access and separate category links, rather than composing every settings control in one long root page. Audio quality, Appearance, Playback, Downloads, Recommendations, Performance, Local music, Storage, Your data, Language, Miscellaneous and Advanced options each open independently. All previous controls remain available; global settings search is retained.
- Category Back navigation works through the page's button, system Back and the outer toolbar. Source/account/equalizer overlays return to the category underneath.
- Settings keeps cached, painted glass styling rather than per-card GPU backdrop blur, shadows or continuous animations. Hidden category controls are not composed. Download progress is throttled and network/hash/archive work stays off the UI thread. This is an implementation improvement, not a measured all-device FPS guarantee.
- App-rendered branding consistently colors only the initial **Z** red in Zunévio, including translated sentences containing the brand, settings footer and generated lyrics/Replay share artwork. Android system labels and native plain notifications cannot carry this rich-text styling.
- Added **Settings → App updates**. The repository is deliberately blank until you create and select your trusted public GitHub repo. No hard-coded upstream/fictitious repository is used.
- Reads the newest stable GitHub Release at startup, manually and approximately every 12 hours via network-constrained WorkManager. Notifications require Android permission and are not real-time push. Force-stop, Doze, restricted background operation and offline state affect delivery.
- Displays the future release description as a changelog; notification taps open the updater even if the app was already running.
- Explicit download → SHA-256/package/versionCode/minSdk/signing certificate/non-debuggable verification → Android installation confirmation. No automatic download, silent installation or Play Protect bypass. A missing GitHub digest fails closed.
- Added a manual signed Android build workflow. It uploads build artifacts only, never publishes. See `GITHUB_UPDATES.md` for repository setup, signing continuity and release-asset naming.

## Security/distribution note

`REQUEST_INSTALL_PACKAGES` was deliberately restored for the in-app updater requested for 1.1.1. Only private `cache/updates/` was added to FileProvider. This changes the previous 1.1.0 no-installer policy; its static regression tests were updated to require strict verification and narrow sharing instead of asserting the absence of the requested feature. It is not a workaround for a harmful-app classification.

This candidate is optimized/non-debuggable but **test-signed**, not production-signed. Do not publish the private test keystore. For final production distribution establish the final app identity and owner-controlled signing plan first; a production `com.zunevio.music` APK is a different app and cannot update this preview package.

## Validation and remaining acceptance work

See `VALIDATION_1_1_1.json` and `validation/1.1.1/` for the exact final build/test/lint results. No physical-device smoothness measurement, chosen-repository end-to-end update, Google certification or Play Protect appeal is claimed. Earlier candidates carried an external live Genius lookup failure and existing Lint errors; use this version's report rather than assuming those are fixed.

Before public release, test the exact APK, every settings category, large font/light/dark themes, playback while scrolling, a matching next-version GitHub release, rejected wrong-signature/corrupt APKs, cancelled/denied installs and preserved user data. **Do not uninstall the previous preview merely to test an update**; same-package/same-key installation is intended to preserve its data.

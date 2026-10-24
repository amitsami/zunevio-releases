# Zunevio 1.3.1 · Build 64

Production Android update from 1.3.0 / Build 62. Package: `com.zunevio.music`. Minimum Android: 8.0 (API 26). This release uses the existing production signing certificate.

## What changed
- **Repeated-track seeking:** refresh drag/release callbacks after Next and audio/video handovers so the scrubber targets the current playback session, not a stale controller.
- **Seek state and cancellation:** scope scrub state to its track and playback clock, clear cancelled gestures without submitting an unintended seek, and retain the latest requested position during delayed video preparation and recovery.
- **Landscape output label:** separate “This phone” from the headphones button without shrinking the video pane.
- **Soft video edge:** dissolve the native video’s right edge to match the bottom edge, retaining video size, crop behavior and transitions.
- **Footer selection:** only the selected section’s icon is brand red; other icons return to the normal theme-aware color.
- **Rendering efficiency:** remove a redundant offscreen composition layer around already-masked native video. This is not a claim of measured FPS improvement.

Existing player layout, media quality, buffer budgets and unrelated features are retained. No new analytics or credential-storage hardening was added in this release.

## Download
- **ARM64:** `Zunevio-1.3.1-build64-arm64.apk` for compatible 64-bit ARM devices.
- **Universal:** `Zunevio-1.3.1-build64-universal.apk` if unsure which architecture to choose.
- **Corresponding source:** `Zunevio-1.3.1-build64-corresponding-source.zip` is the clean build-source archive for these APKs. It includes required sources, build inputs and legal notices, not private CI or internal reports. GitHub’s automatic “Source code” downloads refer to the release repository tag and are not a substitute for this explicitly named corresponding-source asset.

Install only one APK. Keep the existing production app installed; do not uninstall or clear its data just to update. Preview/debug packages may be separate apps and are not the production signing chain. Android controls installation and any security prompts.

## Validation and known limits
- 1,420 unit tests: **1,419 passed**; one known external Genius live-fetch test failed (HTTP 403).
- Android lint: **0 Error/Fatal**, 701 warnings; warnings have not all been resolved.
- Offline decoder/seek instrumentation: **2/2 passed on Android API 28 and 2/2 on API 35**.
- Gesture regression cases were checked before and after the correction.
- APK identities and production certificates were verified in the signed build; these release files match the already-delivered APK SHA-256 hashes.

No physical-phone/FPS certification, full security audit, all-provider/network guarantee or “100% bug-free” claim. See `VALIDATION.json`, `SIGNING-CERTIFICATE.txt` and `SHA256SUMS.txt`.

## Source, privacy and credits
The app is derived from BitChord. Source and third-party license/notice files are included. No owner signing key, password or account token is intentionally distributed. Optional integrations require their own legitimate configuration.

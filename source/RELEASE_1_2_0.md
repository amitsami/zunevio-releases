# Zunévio 1.2.0 — review candidate

Version code 35. This review build keeps package `com.zunevio.music.preview3` and the same private **test** signing identity as the supplied 1.1.1. Android 8.0/API 26+, universal APK (arm64-v8a, armeabi-v7a, x86, x86_64). Not published to GitHub.

## Changes
- The current single-line lyric remains synchronized and tappable, without its glass/background box. Full lyrics behavior is unchanged.
- A visible heart beside the title/overflow in both player orientations. Signed-in online tracks use the account's existing rating API. Guests and local/downloaded files use **Liked songs · On this device**, a persistent collection in Library. Device favorites do not automatically upload or merge into a YouTube account. A failed remote rating rolls back rather than pretending it was saved.
- Explore pull-to-refresh forces personalized discovery past its five-minute TTL and clears category-content cache. The bundled category names are intentionally stable; a successful refresh need not show different server results.
- Library pull-to-refresh no longer returns immediately for guests. It verifies saved downloads off the UI thread and removes missing files from the downloaded-library record; signed-in refresh also reloads the account's library and playlist picker. Local/imported collections already update through their state flow.
- Feed refresh is bounded to 35 seconds and always clears its busy flag, including cancellation/failure. Previously displayed feed content is retained on refresh errors, with an error message instead of a blank replacement.
- All 25 remaining Settings switches and the update-notification switch share explicit dark/light On, Off and disabled colors. Off has a visible thumb and border. Unavailable rows are less heavily faded; standard Material switch checked/disabled accessibility semantics remain.
- Ordered background serialization for local playlist/favorite saves avoids serializing a growing collection on the UI thread. Local URIs survive favorites persistence.
- Release maintenance: actual Media3 AndroidX opt-ins, observable localized resource snapshots and StateFlow reads, remembered cast-output projection, Android 13 runtime-shader guard, Spanish literal-percent format fix, required Hebrew/Polish plural forms. No lint baseline or blanket suppression added.

- Removed the Legacy mesh gradient row from Appearance/search. The Android legacy-gradient state is read-only false; old stored values are removed on settings initialization and cannot silently restore the removed effect. The normal artwork background remains.

- Settings scroll is owned above navigation and saved for the root and every category independently, with a separate bounded search slot. Returning from a category, Sources/Equalizer/account sub-screen, or reopening Settings no longer forces the list to the top. Search editing still resets only its result list; Activity recreation restores saved offsets.

## Preserved
Red initial Z branding, category-based Settings, lower-cost cached Settings surfaces, reduced-motion/reduced-blur options, and the verified/consent-based GitHub updater. No extra infinite animation or per-card blur was added.

## Before public release
See `VALIDATION_1_2_0.json` and `validation/1.2.0/` for actual results, not historical reports.

- Test on a physical phone: install over 1.1.1 **without uninstalling**; check data retention; repeat heart toggles and restart; open device favorites; test online rating failure; refresh Explore/Library both signed in and out, offline and after deleting a downloaded file; test portrait/landscape and both themes, disabled switches, lyrics timing, playback during Settings scrolling, and reduced-motion/blur options.
- Smoothness has not been measured on a physical device. Static/source guards and optimized compilation are not frame-time measurements.
- Do not publish as a certified production build solely because it assembles. A public production identity/key decision and the remaining validation gates must be completed. Changing to `com.zunevio.music` creates a separate app, not an in-place update to preview3.
- Configure **Settings → App updates → Set repository** after choosing your GitHub repository. A newer stable GitHub Release, not a source-code push alone, triggers update detection. Keep package/signing identity and increase both version fields. Upload the universal APK with GitHub's asset SHA-256 digest. Background checks are approximately every 12 hours plus app-open/manual checks, not instant push.
- Downloads and installation require user action and Android confirmation. Never disable Play Protect. No antivirus/Play Protect approval or guaranteed warning-free installation is claimed.
- Private signing keys and local credentials must never be put in GitHub or the source ZIP. Preserve your existing signing backup privately.

## Actual validation result

- Optimized universal APK: build successful; signature and frozen-source manifest verified. Same preview3 signing certificate as 1.1.1, version code 35, non-debuggable, all four ABIs.
- Tests: 1158 executed, 1157 passed, 1 failed (the existing live Genius lookup). All 16 new v1.2 regression checks pass. No skipped tests.
- Android Lint task: successful, 0 errors; 677 warnings and 5 hints remain. Kotlin metadata compatibility diagnostics and compile-SDK support warnings remain in the build tooling logs.
- Public release readiness: not certified. Physical-device testing, production identity decisions, the remaining warnings/live-provider failure, and the chosen-repository updater flow still require review.

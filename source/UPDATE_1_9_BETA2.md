# Zunévio 1.9-beta2 — account-free discovery and lighter Android UI

Developer of this fork: **Amit Hasan Sami**  
Public contact: amitsami110@gmail.com  
GitHub: https://github.com/amitsami

This is a native Kotlin/Compose Android beta, derived from BitChord. GPLv3, original copyright and native/JNI identifiers are retained. Earlier reports describe earlier builds; current machine-readable results belong in `validation/1.9-beta2/`.

## Current requested changes

- Home no longer renders the YouTube Music sign-in banner. The guest Library sign-in call-to-action was also removed, keeping account sign-in in Settings. Account-only library operations may still require legitimate sign-in; removing a banner does not unlock an online account library.
- Account-free discovery uses real listening samples. After approximately 20 seconds of audible playback of a YouTube track, up to eight recent track seeds are kept locally. Home and Explore can show a “Made for you” shelf using related-track radio, with an artist-search fallback. A seed is not a fabricated recommendation; if providers return no usable songs, no fake results are shown.
- Explore categories can add a real artist/category search based on the latest qualified seed. Generic category results are published as soon as ready, without waiting for the optional personal request. A generation guard prevents an older request replacing a newly opened category.
- Settings provides an on/off switch and a reset for these eight local seeds. Turning it off stops collection and removes personal shelves; reset clears seeds, not the separate listening-statistics history. The seed preference file is excluded from Android backup/device transfer. Local files are not sent as radio seeds. An account is not required, but Internet and catalog/playback service availability are still required.
- Android 10/11 get a lightweight tinted, outlined glass-style fallback. Actual backdrop blur requires Android 12+. Low-RAM devices avoid backdrop capture; modern Reduce blur also avoids it. This is not a claim of identical refraction/blur on every Android version.
- Main tab changes use a short directional transition over opaque page backgrounds, rather than crossfading through a dim frame. Reduce animation disables this transition. Existing single-tap fixes remain.
- The video-canvas disk cache is opened lazily on actual canvas use rather than on the application startup join path. This removes identifiable startup work, not a measured phone-startup improvement.
- Normal “Lyrics by …” provider/status footer is hidden. The bottom-left globe/romanization button was removed in both player layouts. The separate translation control is retained. Provider licensing/source attribution is not erased from the distributed source.
- Hindi is no longer offered as an app language. **বাংলা (বাংলাদেশ)** is offered, with complete Android and shared-UI resource catalogues plus base Bengali fallback. Existing Hindi app selections migrate to bn-BD. Translations are machine-assisted with manual music/UI terminology corrections; this is not professional linguistic certification of every technical caption.
- Word/letter lyric sweep from beta1 is retained where the provider supplies genuine word/syllable timestamps. Line-only lyrics still have line-level timing; accurate word timing is not invented.

## Icon priority

The user requested all functional fixes first, then a **plain white music symbol on a black background**, with no Z or extra waveform. Earlier coloured/gradient demos were rejected. Check current validation/release notes for whether the launcher artwork has subsequently been applied; do not confuse icon mockups with a running app screenshot.

## Inherited beta1 changes retained

JioSaavn, Discord and scrobbling controls removed; those integrations do not auto-start. No default upstream presence/update/party-server requests are configured. Listen Together is absent because no owner-controlled backend was provided. Sixteen bundled Explore banners are retained. Generic Home discovery uses live current-month English-music/playlist searches, not an independently verified chart ranking. No hosted service or private credential was invented.

YouTube/Google receive normal catalog, related-track/search, playback and legitimate sign-in requests. The eight seeds stay in the app sandbox, but the selected track IDs and artist/category query terms are sent to the catalog provider to retrieve results. Lyric providers and optional artwork/canvas/translation/user-configured sources can receive relevant lookups. No active BitChord/Kushagra HTTP reference appeared in the current Kotlin source scan, but this is **not a packet capture of every binary dependency**, nor a claim that all third-party endpoints are developer-owned or permanently free.

## Compatibility and signing

- Minimum Android 8.0 / API 26; compile SDK 37, target 36. Four ABIs plus universal. This does not mean every Android version, OEM, codec, login provider or external service has been tested.
- Package for this delivery: **com.zunevio.music.preview3**, version **1.9-beta2 (29)**. It installs beside previous test packages; their settings, downloads and accounts are not automatically migrated. Keep the old app; do not uninstall it just to install this beta.
- A previous session's test private key was unavailable in this build environment. The distinct package avoids an incompatible-update/data-loss scenario. This is an optimized, non-debuggable `devBenchmark` beta signed with a test/debug key, not a production signing identity. Retain a secure signing-key backup for subsequent in-place updates; never publish it with the source.
- POCO M6 Pro 5G / Android 15 is the user's target. That physical phone is not connected here. FPS, touch reliability under load, OEM battery limits, Bluetooth and background playback still need phone acceptance checks.
- ARM64 is appropriate for the POCO. Universal is larger. 32-bit x86 does not bundle the optional FFmpeg fallback; normal supported platform codecs remain.

## Rebuild

Use JDK 21, SDK 36 + official 37.0, Build Tools 35.0.0, CMake 3.22.1 and NDK 27.0.12077973. The checked-in metadata adapter preserves official android.jar byte-for-byte while adapting API-37.0 naming for the inherited AGP. It is not a modern-toolchain compatibility certification.

```sh
python3 scripts/prepare_android37_legacy.py "$ANDROID_HOME"
python3 scripts/check_vector_resources.py
./gradlew :app:assembleDevBenchmark :app:lintDevBenchmark \
  :app:testDevDebugUnitTest :shared:jvmTest :sharedUi:jvmTest \
  --continue --console=plain --max-workers=1 -PzunevioSideBySide=true \
  '-Dorg.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=896m -Dfile.encoding=UTF-8' \
  -Pkotlin.compiler.execution.strategy=in-process
```

The enabled Genius live-service test is separate from local parser tests and may fail because the service rejects lookup requests. Do not skip it or claim an all-green suite while it fails. See actual final logs/results. R8/Lint warnings about newer dependency Kotlin metadata are inherited; inspect real task outcomes rather than treating every warning as a passing or failing build.

## Phone acceptance checks

1. Keep the older app. Install the ARM64 beta, cold-open online and offline, and check Home/Explore/Library/Settings with single taps.
2. Listen audibly to one or two YouTube tracks for at least 20 seconds each. Return to Home/Explore and allow the real network lookup to finish. Test reset and disable in Settings. A playback tap without audible playback is intentionally not enough.
3. Switch Settings → App language to বাংলা (বাংলাদেশ). Inspect Home, Settings, player and long dialog captions for shaping/overflow. Hindi should not appear in the picker.
4. Check the bottom-left lyrics globe and normal provider footer are gone; verify the separate translation button and accurate word-synced track animation.
5. Compare Liquid Glass / Reduce blur / Reduce animation on the POCO. Check short tab transitions and background/lock-screen playback. Do not use software-emulator timings as phone performance measurements.
6. If there is a crash, export a fresh log from this package/version and provide the last action. Redact tokens, cookies and identifiers. No additional personal information is needed now.

## Completed build/test evidence

- Final resource build completed `:app:assembleDevBenchmark`; all five ABI/universal APKs were produced. Signatures, package/version/label, non-debuggable flag, 16 bundled banners, ZIP 16-KiB alignment and all 31 bundled native ELF load-segment alignments were checked. Alignment checks are not proof of device compatibility.
- Android unit tests: **975/976 passed**, one enabled live Genius lookup failure, zero skipped. Public Genius probe returned **HTTP 403 / cf-mitigated: challenge**; no challenge bypass was attempted.
- Shared core **66/66**, shared UI **42/42** passed, including actual Compose vector-parser crash regressions. Total **1,083 passed / 1,084**, no skipped tests. These are unit/regression results, not successful real playback on a physical phone.
- **Full Lint FAILED: 198 errors, 671 warnings, 5 hints.** Findings include API/opt-in/composition/older-locale issues; 107 errors are in files byte-identical to the supplied beta1 archive. Logs also show the inherited Lint frontend's inability to read newer dependency Kotlin metadata, so individual findings require review; do not assume every report is a confirmed runtime defect or dismiss all as false positives. No suppression/baseline was added. This update does not resolve every inherited finding and is not production-ready.
- One combined validation attempt lost its Gradle daemon; the sandbox recorded an OOM kill. The abandoned worker was stopped and checks were rerun successfully to completion in a fresh process, with the above Genius and full-Lint failures still explicitly reported. This was a build-environment interruption, not an observed Android app crash.
- The two Bengali catalogues have every English Android/shared-UI resource group, with no missing keys or checked `%d/%s/%f` placeholder mismatch. Music/UI terms were manually refined. This does not replace native typography/translation review.
- Launcher icon remains unchanged in this functional beta, per the user's instruction to prioritize fixes. The next icon must be a plain white music symbol on black; rejected coloured demos were not applied.

## Android 10 native launch attempt

The API-29 emulator booted without KVM/hardware acceleration. The install command timed out after APK transfer; a later `pm path` confirmed the package installed. `am start -W` returned a launch timeout. The app process was alive and the crash buffer was empty at capture, but the screenshot command produced only a truncated 62-byte PNG header. **This is an inconclusive native test, not a successful rendered-UI acceptance test and not proof of a phone crash.** No valid screenshot is supplied. Navigation, Bengali layout, playback, background audio, startup duration and smoothness still require the user's physical device.

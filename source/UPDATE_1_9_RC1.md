# Zunéviu 1.9-rc1 — translation crash and Settings back correction

Developer of this fork: **Amit Hasan Sami**  
Public contact: amitsami110@gmail.com  
GitHub: https://github.com/amitsami

This is a **release-candidate test build**, not a declaration that every inherited issue, external service and Android phone has passed production acceptance. It is the native Kotlin/Compose fork, not an HTML wrapper. Current evidence is in `validation/1.9-rc1/` and `VALIDATION_1_9_RC1.json`. Earlier reports refer to earlier versions.

## Requested changes

### Approved icon and full in-app name

- The user approved the **white paired music notes with one top beam on a pure-black background**. That exact shape is now used for launcher resources: adaptive foreground/background, Android themed monochrome foreground, and density-specific regular/round legacy WebP icons. No Z, second beam, added waveform or coloured gloss was applied.
- Launcher foreground geometry accounts for the adaptive mask inset so the perceived mark scale matches the approved preview. System-themed icons can intentionally tint the monochrome variant.
- The Home header now renders the full **Zunéviu** wordmark as native scalable Text, not the old Z image. The existing page heading remains below it. Shared/notification/replay graphic resources now use the approved music mark rather than a Z.
- The actual Compose XML resource parser regression tests remain enabled; vector path colours remain literal ARGB values. Their geometry assertion was updated for the approved note, not removed.

### Translation force-close: cause verified from supplied log

The attached `Kotlin Coroutines Error.rtf` reports:

```text
java.lang.IllegalArgumentException: Unexpected char 0xe9 at 3 in User-Agent value: Zunévio/1.7.1
    at okhttp3.Request$Builder.header(...)
    at com.music.bitchord.data.lyrics.LyricsTranslation.requestBatch(...)
```

This is not evidence that Kotlin Coroutines itself is broken. The accented display name was accidentally used as an HTTP header. OkHttp rejects that non-ASCII header value during request construction, before a network request is sent. The old exception boundary covered transport IO only, so it did not catch request construction, and an uncaught UI coroutine could close the application.

Correction:

- New shared `NetworkIdentity.USER_AGENT` is printable ASCII: `Zunevio/1.9 (+https://github.com/amitsami)`. The visible UI name keeps **é**; only the network identity is ASCII.
- All three translation/romanization/Input Tools request paths use that identity and place request construction inside a guarded request/transport boundary.
- The same invalid accented User-Agent in Genius and artist-facts requests was corrected. Source/addon requests now use the same owner-branded ASCII identity instead of the inherited upstream agent.
- The shared player also guards translation/romanization operations. Ordinary construction/provider/cache exceptions produce the existing unavailable/retry UI instead of escaping into an uncaught UI coroutine. This is failure handling, not a fabricated successful translation.
- Cancellation is deliberately rethrown so changing track/language or closing the player cancels the old work correctly. Fatal errors are not silently swallowed.

Regression tests reproduce the exact old OkHttp exception, validate the new ASCII header, send it to an actual local MockWebServer, and check request-construction failure, transport failure, unchanged successful responses, cancellation and fatal-error behaviour. Shared UI tests independently cover both translation/romanization fallback and cancellation. Local-server success is **not** proof that every public translation provider is reachable.

### Settings back arrow: one action pops both states

Settings keeps its list mounted under sub-page overlays to preserve scroll. The top-left arrow previously cleared a sub-page's visibility flag first while leaving `settingsSubScreen` set. The overlay still rendered, making the first tap appear ineffective. A second tap cleared the overlay marker.

Correction:

- One shared state transition now clears the active sub-page visibility flag and overlay marker in the same back action.
- The top bar gives that overlay transition priority over individual sub-page flags.
- Account/integrations, Sources, Equalizer, Replay and the retained dormant party route are covered by pure state regression tests. Repeated dismissal is idempotent; a Replay page underneath Settings is preserved when closing a different sub-page.
- Nested Spotify back still returns to its Account page rather than skipping an extra level. Existing system-back handlers that already cleared both states remain.
- This is verified navigation-state logic, not a claim that a physical phone touch test was performed.

## Completed regression checks

- **21 new regression tests passed.**
- Android JVM: **975 passed / 976 total**, one enabled live Genius lookup failure, zero skipped.
- Shared core JVM: **82/82 passed**.
- Shared UI JVM: **47/47 passed**, including the actual Compose vector-parser regressions.
- Overall: **1,104 passed / 1,105 tests**. No failed test was disabled to obtain those results.
- A normal request with the corrected ASCII identity still received **HTTP 403 / cf-mitigated: challenge** from Genius in this environment. No challenge bypass was attempted. The ASCII-header fix removes a local crash/early failure; it does not promise public service access.
- Full Lint and final APK verification results are recorded separately. The inherited toolchain previously reported unresolved full-Lint findings and newer Kotlin metadata compatibility warnings. Do not treat a generated APK as an all-green quality gate.

## Installation, identity and supported range

- Version: **1.9-rc1 (30)**. Package: **com.Zunéviu.music.preview3**.
- **Same package and same test signing key as the supplied 1.9-beta2.** On that exact beta2 installation, Android should allow an in-place update without intentionally clearing app data. Certificate continuity is checked during packaging. If Android reports an incompatible update, do not uninstall and lose data; confirm which earlier package/build is installed.
- Older beta1/test packages have different IDs and/or signatures; they are not automatically migrated.
- Display name: **Zunéviu**. This remains an optimized, non-debuggable `devBenchmark` build signed with a test/debug identity, not a production/store signing key. Keep the key private and establish an owner-controlled release identity before public store distribution.
- Android 8+ / API 26 minimum, target 36. Four ABIs plus universal. POCO M6 Pro 5G / Android 15 should use the ARM64 APK. This is not an all-device/OEM/codec compatibility guarantee. Optional FFmpeg is not bundled for 32-bit x86.

## Inherited beta2 behaviour retained

Home/guest Library sign-in banners remain removed; legitimate account sign-in remains in Settings. Local “Made for you” seeds qualify after approximately 20 seconds of actual YouTube playback, bounded to eight tracks, with off/reset controls. Internet/provider availability is still required; no account is required for that local taste record. Local files do not become online radio seeds. Selected track IDs/artist terms are sent to the catalog provider for discovery; this is not an offline recommendation engine.

Android 10/11 use lightweight glass-style fallback; Android 12+ can use backdrop blur, with low-RAM/Reduce blur safeguards. Existing tab transitions, lazy canvas cache, single-tap playback/nav changes, sixteen bundled banners, English generic discovery and Bengali (Bangladesh) UI catalogues remain. The bottom-left lyrics globe and normal “Lyrics by…” footer remain absent. Accurate word sweep still depends on genuine provider timing.

No default upstream presence/update/party-server service was re-enabled. Removed JioSaavn/Discord/scrobbling controls remain absent. No private account, paid API key or hosted server was invented. YouTube/Google, lyric/artwork/canvas/translation providers and user-configured services still receive relevant normal requests. Source scan is not a packet-capture audit of every dependency.

## Remaining acceptance checks

The physical POCO is not connected to this environment. The previous no-KVM software-emulator attempt was inconclusive; that result must not be represented as a successful RC1 device test. No new native screenshot or phone FPS/startup benchmark is claimed here.

On the phone:

1. Update **1.9-beta2** with the ARM64 RC1 APK; do not uninstall to resolve a signature conflict. Confirm App info says **1.9-rc1**.
2. Check the approved music launcher icon and **Zunéviu** Home wordmark.
3. Settings → Sources / Equalizer / Accounts & integrations: tap the top-left back arrow once. It should return to Settings, not remain on the sub-page. Check the saved Settings scroll position and ordinary system back too.
4. Open a track with lyrics. Tap translation; test supported output, offline/unavailable response, repeated retry, changing track while loading, and changing target language. No provider reachability guarantee is made.
5. Confirm playback/queue/word-synced lyrics, screen-off background audio and tab navigation on the POCO. If a crash remains, export a fresh log from this RC1 and provide the exact last action. Redact cookies/tokens; no additional personal details are needed now.

## Rebuild

Use JDK 21, SDK 36 + official SDK 37.0, Build Tools 35.0.0, CMake 3.22.1 and the requested NDK. The inherited SDK naming adapter preserves official android.jar bytes; it is not a modern toolchain certification.

```sh
python3 scripts/prepare_android37_legacy.py "$ANDROID_HOME"
python3 scripts/check_vector_resources.py
./gradlew :app:testDevDebugUnitTest :shared:jvmTest :sharedUi:jvmTest \
  --continue --max-workers=1 -PzunevioSideBySide=true \
  '-Dorg.gradle.jvmargs=-Xmx1536m -XX:MaxMetaspaceSize=768m -XX:ActiveProcessorCount=2' \
  -Pkotlin.compiler.execution.strategy=in-process
./gradlew --stop
./gradlew :app:assembleDevBenchmark --max-workers=1 -PzunevioSideBySide=true \
  '-Dorg.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=768m -XX:ActiveProcessorCount=2' \
  -Pkotlin.compiler.execution.strategy=in-process
./gradlew --stop
./gradlew :app:lintDevBenchmark --max-workers=1 -PzunevioSideBySide=true \
  '-Dorg.gradle.jvmargs=-Xmx1280m -XX:MaxMetaspaceSize=640m -XX:ActiveProcessorCount=2' \
  -Pkotlin.compiler.execution.strategy=in-process
```

Test/lint failures remain failures; inspect the exact reports. Separate memory-bounded commands avoid combining a large R8 heap with test workers in a 4-GB builder. The archive excludes signing keys, local credentials, SDK/JDK/Gradle caches and compiled outputs. Root upstream sources, GPLv3 and attribution remain; this fork does not claim authorship of the original BitChord implementation.

## Final packaged results

| Gate | RC1 result |
| --- | --- |
| Isolated optimized APK assembly | PASS, exit 0 |
| Android/shared/shared-UI unit tests | 1,104 passed / 1,105, no skipped; one live Genius failure |
| Full Lint | FAILED: 198 errors, 666 warnings, 5 hints; no suppression added |
| Package / version / label | com.Zunéviu.music.preview3 / 1.9-rc1 (30) / Zunéviu |
| Signatures / update identity | All five APKs verified; certificate matches delivered beta2 |
| Non-debuggable manifest | Verified |
| Invalid accented translation header in packaged DEX | Absent; corrected ASCII identity present |
| Compiled adaptive icon | Foreground geometry and pure-black background match source |
| Legacy launcher icons | All ten byte-match source despite optimized APK resource filenames |
| Shared vectors | Six decoded/checked; packaged logo byte-matches current source |
| ZIP / native ELF alignment | All APK ZIPs pass 16-KiB checks; all 31 native libraries meet load-segment alignment check |
| Current physical-device/UI/playback acceptance | NOT performed; no all-device/FPS/startup claim |

The public translation probe with the corrected header returned HTTP 200 and Bengali **হ্যালো বিশ্ব** for the harmless input **Hello world**. This is endpoint evidence from the build environment, not a successful phone UI test. Full-Lint findings and newer Kotlin metadata frontend warnings remain unresolved; this is an RC test candidate, not an all-green production release.

### APK file integrity

| Build output | Bytes | SHA-256 |
| --- | ---: | --- |
| app-dev-arm64-v8a-benchmark.apk | 60020676 | 6f152e4a5b74c9c1277bad0f49607f77c6ea8955b5e5e87aed888af7f3b51b6b |
| app-dev-armeabi-v7a-benchmark.apk | 51029788 | bb3f2f999a57a96c21e4891a7a9a46649f0cacfcee4f09145c9755e269293487 |
| app-dev-universal-benchmark.apk | 158335552 | 2f5b402d8115d940fc5489591758aae4f426f88e1cd070c3a9aa682b7ac072ea |
| app-dev-x86-benchmark.apk | 63812623 | 591dfaf2d43245153fa2cdffd4b446e0b7349a8110ce50a35cb58d352ceb30cd |
| app-dev-x86_64-benchmark.apk | 66222900 | dc9bf06439e2d9506d63b22431baed5ddf3ed726016e6ad5128f88b3e3e12f21 |

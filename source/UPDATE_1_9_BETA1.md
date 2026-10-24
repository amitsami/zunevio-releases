# Zunéviu 1.9-beta1 — Android update

Developer: **Amit Hasan Sami**  
Public contact: amitsami110@gmail.com  
GitHub: https://github.com/amitsami

## বাংলা সংক্ষিপ্ত নির্দেশনা

- এটি native Android beta; UI/Settings হালকা করা হয়েছে এবং এক ট্যাপের কয়েকটি নির্দিষ্ট সমস্যা ঠিক করা হয়েছে। ফোনে FPS/startup মাপা হয়নি।
- Developer: Amit Hasan Sami; amitsami110@gmail.com; https://github.com/amitsami।
- JioSaavn, Discord ও scrobbling controls সরানো হয়েছে। নিজস্ব server না থাকায় Listen Together বন্ধ; upstream presence beacon ও default server সংযোগ নেই।
- নতুন ১৬টি Explore banner bundled আছে। Home এখন English songs/playlists-এর live search দেখায়; যাচাই করা chart ranking নয়।
- সঠিক word/syllable timestamp থাকলে full lyrics ও lyric strip-এ word/letter sweep চলে; সব গানে এমন timing পাওয়া যায় না।
- Android 8+ এবং চারটি ABI লক্ষ্য করা হয়েছে; সব Android/OEM/codec-এ কাজের নিশ্চয়তা নয়। আপনার POCO-তে পরীক্ষা বাকি।
- নতুন package `com.zunevio.music.preview2` পুরোনো app-এর পাশে বসবে; পুরোনো app uninstall করবেন না। পুরোনো data নিজে থেকে এতে আসবে না।
- Source-এর GPL/copyright/JNI নাম মুছে দেওয়া হয়নি। App-এর visible branding Zunéviu; এটি upstream code-এর authorship নিজের নামে দাবি নয়।
- Genius live lookup এই environment-এ HTTP 403/challenge দিচ্ছে। Final test counts আলাদা validation JSON-এ আছে; provider failure লুকানো বা test skip করা হয়নি।

## Scope and installation

This is the existing native Kotlin/Compose music app, not an HTML shell. This update targets Android. It is an optimized, minified beta built with the `devBenchmark` variant and a **test/debug signing identity**, not a production/store release.

- Minimum: Android 8.0 / API 26. Target: API 36. Your POCO M6 Pro 5G on Android 15 is within this range, but has **not** been connected or tested here.
- Four requested ABIs: arm64-v8a, armeabi-v7a, x86 and x86_64; universal APK bundles all four. An ABI build is not a promise of compatibility with every device or OEM.
- Package: `com.zunevio.music.preview2`; version code 28. This installs beside previous Zunéviu test builds. It does **not** migrate their app data automatically. Keep the older app until the new one passes your device checks. Do not uninstall it merely to install this beta.
- The inherited FFmpeg fallback is bundled for ARM32, ARM64 and x86_64, but not 32-bit x86. On x86, normal platform-supported audio remains available; do not assume every optional codec works.
- On API 26–30, glass uses the retained fallback. Advanced blur requires API 31+. No Android 7-or-older, iPhone, Windows or macOS binary is supplied by this update.

## Changes

### Single taps and lighter native UI

- Navigation stays expanded: tapping a tab navigates directly, rather than first expanding a collapsed pill and requiring another tap.
- Loading/buffering play controls are clickable, including the mini player and glass navigation bar. Play/pause uses `playWhenReady`, so a buffering track can be paused on the first tap.
- The floating-bar drag handler gets current selection/geometry instead of retaining an old closure.
- The lyrics reveal gesture observes taps without consuming the first tap intended for child controls.
- The current-lyric strip has one click target, not nested competing click handlers; its transition is shorter.
- Removed a refraction/chromatic lens pass from liquid glass; reduced lyric-capsule blur; the selected-tab pill is flat tinted. The overall native layout and main glass surface remain.
- Expensive unfocused-lyrics blur defaults off on new installs; the optional switch remains. Existing explicit user preferences are respected.
- Explore metadata/thumbnail fan-out is removed. Its 16 banners are bundled WebP images; opening Explore does not issue an artwork search for every card.
- Explore loads when first opened, not eagerly at startup. Compatible image/audio caches are no longer deliberately purged on each beta version change. Home uses two concurrent discovery searches and a 30-minute guest-session memory cache; explicit refresh bypasses it. Signed-in loads bypass that cache.
- Removed two unused orphan lyric-setting translations (six localized entries) that blocked release Lint; the check was not disabled.
- Settings have smaller headings, softer grouped surfaces, 18dp corners and 56dp rows.

These changes remove identifiable work and tap conflicts. They are **not measured proof** of a particular startup time, FPS, battery saving, or elimination of every missed tap. Real-device profiling remains necessary.

### Branding and removed integrations

- Visible Android strings and About/publisher links use Zunéviu / Amit Hasan Sami. About no longer promotes the upstream app, community or updater.
- JioSaavn is removed from built-in and restored source configurations, not merely hidden in the list. YouTube and user-configured source modules remain.
- Discord, Last.fm and ListenBrainz/scrobbling controls are removed from Accounts & integrations. Their automatic observers/startup initialization are disabled; their enabled settings are forced off. The dormant Discord application ID is blank.
- Listen Together controls, incoming invite handling and app-link entry points are removed from this Android edition. No owner-controlled server was provided/deployed. Its configured default server is blank and on a fresh install initialization returns before health polling or restoration. The retained source can still honor an explicitly saved custom server when rebuilt under an existing package; this new preview package does not import those older preferences. No upstream fallback service is bundled.
- Required GPLv3 notices, source copyright and internal package/JNI identifiers remain. Removing them would either misrepresent the original authorship or break native linkage. They are not app-facing branding or telemetry.

### Home and Explore

- The guest Home performs live YouTube Music searches for popular English pop songs and English top-hits/community playlists using the current month/year.
- These are **search-driven discovery results**, not a verified Billboard/Spotify chart or independently measured community-trending ranking. No chart data, counts or song lists are fabricated.
- Catalog locale is English/US; the UI language picker remains separate.
- Old Home supplement feeds are disabled. Explore has 16 local categories with new photographic banners: Chill, Focus, Sleep, Energize, Workout, Romance, Party, Feel good, Road trip, Rainy day, Pop, Rock, Hip-hop, Electronic, Jazz and Classical.
- Indian-language/genre-labelled discovery categories/results are filtered. Filtering checks titles/subtitles; it cannot prove the sung language of every result. It does not filter artists by nationality. A signed-in user's own listening history is still their history, not forcibly erased or rewritten.
- Category taps perform real playlist searches; no fake album/song cards are bundled. Network/provider errors remain errors with retry, not invented content.

### Timed lyrics

- Existing native word/letter sweep is used in both the full lyrics view and the current-line strip. Word/syllable-synced sources are prioritized by default, including after resetting lyric-source preferences.
- The sweep follows provider word/syllable timestamps and playback clock. It is not newly generated forced alignment or phoneme recognition.
- For line-only lyrics, the fallback remains line-by-line. Untimed/missing lyrics cannot truthfully become accurate word-by-word lyrics. Provider coverage, account restrictions and timing quality vary. No paid key or private account was invented.

## Privacy / where requests can still go

The former upstream presence beacon is not installed, and its endpoint is blank. No default upstream party health polling or upstream app-update request is enabled. No active BitChord/Kushagra HTTP endpoint remains in the updated Android/shared runtime Kotlin source scan.

This is **not an offline/no-network app**. YouTube/Google receive normal catalog, search, playback and legitimate sign-in requests. Optional Spotify library/canvas features use Spotify. Lyric providers can receive the artist/title/track lookup; examples include LRCLIB, Musixmatch, Genius, BetterLyrics, SimpMusic, LyricsPlus mirrors, Kugou and others. Optional translations use their configured services. Canvas/artist-art/config providers can receive relevant lookups; InnerTubeX retains a public GitHub-hosted player-configuration URL. User-added modules/WebDAV use the user's configured endpoints.

No claim is made that all third-party providers are owner-controlled, private, permanently free, licensed for every use, or currently reachable. A source scan is not a packet-capture audit of every dependency. The source and final validation record document precisely what was checked.

## Rebuild

Use JDK 21, Android SDK 36 + 37.0, Build Tools 35.0.0, CMake 3.22.1 and the NDK version Gradle requests. Upstream AGP 8.10.1 cannot parse the official API-37.0 naming as-is:

```sh
python3 scripts/prepare_android37_legacy.py "$ANDROID_HOME"
./gradlew :app:assembleDevBenchmark -PzunevioSideBySide=true --max-workers=1 \
  '-Dorg.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=896m -Dfile.encoding=UTF-8' \
  -Pkotlin.compiler.execution.strategy=in-process
./gradlew :app:testDevDebugUnitTest :shared:jvmTest :sharedUi:jvmTest \
  -PzunevioSideBySide=true --max-workers=1 \
  '-Dorg.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=896m -Dfile.encoding=UTF-8' \
  -Pkotlin.compiler.execution.strategy=in-process
```

The SDK helper only adapts release-37.0 metadata, verifies byte-identical official `android.jar`, and preserves the original SDK. It is a temporary compatibility adapter, not a modern-toolchain certification. The retained R8 override avoids an existing upstream debug dex-verifier defect. Logs may include legacy Lint metadata warnings because that Lint frontend predates Kotlin 2.3/2.4 dependencies. See final logs/results for actual task success or failure; warnings are not automatically passing checks.

This archive does not contain a production keystore, account cookies, API secrets or a deployed backend. A fresh builder's debug key differs, so independently rebuilt APKs cannot automatically update this signed beta without preserving its signing identity. Configure and securely retain your own release key before public distribution.

## Live-provider limitation

The live Genius scrape test is retained, not skipped. A normal request to its search endpoint returned HTTP 403 with `cf-mitigated: challenge` in this environment. No bypass was attempted. Local Genius HTML parsing tests are separate from that live request. Other enabled lyric providers/fallbacks remain; device/network availability is not proven by their presence in source.

## Required phone acceptance checks

1. Cold-open several times (including offline). If it crashes, share a fresh exported log from **this version**, not the previous APK.
2. Tap each tab and playback control once, both during loading and after playback starts. Test lyrics seek, queue and drag gestures.
3. Search/play several English tracks and a local audio file; test pause while buffering, lock-screen controls, background playback, Bluetooth and wired audio.
4. Open Home, pull to refresh, open every Explore category, inspect Settings and Sources. Confirm removed integrations are absent.
5. Compare full-screen and strip lyric timing on a track whose provider actually supplies word timestamps; verify the line-only fallback on a different track.
6. Report slow screens with a short screen recording and whether Liquid Glass/Reduce blur/lyrics blur are enabled. No universal performance claim is made without these measurements.

## Final verified results

| Check | Result |
| --- | --- |
| Optimized Android assembly | PASS; isolated Gradle command exit 0 |
| Release Lint | PASS; no new suppression/baseline |
| Android JVM unit tests | 975 passed / 976; 1 live Genius failure, 0 skipped |
| Shared JVM tests | 51 passed / 51 |
| Shared UI JVM tests | 42 passed / 42; includes four actual Compose vector parser regressions |
| APK signatures | All five APKs verified; debug/test certificate, not production |
| Package / label / version | com.Zunéviu.music.preview2 / Zunéviu / 1.9-beta1 (28) |
| Debuggable manifest flag | false |
| Four-ABI universal APK | Verified ARM64, ARM32, x86, x86_64 |
| Bundled mood banners | 16 in every APK |
| ZIP 16KB native alignment | PASS on every APK |
| Native ELF load-segment alignment | All bundled native libraries have at least 16KB alignment; not a device-run claim |
| Current phone/emulator launch | NOT performed |
| Current device performance/background playback | NOT measured/tested |

The combined assemble+test command exits 1 because the one live Genius test fails; APK assembly itself independently exits 0. Earlier failed attempts and their fixes are retained in the validation folder. R8 warns about newer Kotlin metadata in inherited dependencies; successful packaging is not proof of runtime correctness on every device. No failing tests were skipped. Source-order tests were updated to require removal of JioSaavn, and an invite parser regression was fixed, not ignored.

| APK | Exact bytes | SHA-256 |
| --- | ---: | --- |
| Zunevio-1.9-beta1-arm64-v8a.apk | 59887940 | b2039698128e9834c5ee980aae3585c8a1514c1525afbad047d05dceb50b6ece |
| Zunevio-1.9-beta1-armeabi-v7a.apk | 50897052 | 43a50e8bea7e0e39936b2f0485b8b13de310cab7f68e80264c8f2c40d3b22d6d |
| Zunevio-1.9-beta1-universal.apk | 158202816 | cd98fc32d2020dc3322b52cc97db23dc673f022c5db142b1367963333c251cc2 |
| Zunevio-1.9-beta1-x86.apk | 63679887 | 9d07b67ed53336dff76574ef3f9c4d441d75ad18c2a25fde8cfd7154f4be4a4f |
| Zunevio-1.9-beta1-x86_64.apk | 66090164 | 3458d65b8bfe221c15a345b272322c8aefb84384eefdd57d6eda2019908472d5 |

## Legal/source provenance

Derived from BitChord, upstream commit `fe95bb60aa9cd205cf4ab8bd25a5bec71a595b4f` (https://github.com/kushagrasinghx/BitChord). The complete original source tree, GPLv3 license and attribution remain. Root upstream documentation and earlier validation reports are historical, not current test claims. Current results: `VALIDATION_1_9_BETA1.json` and `validation/1.9-beta1/`. This is not a claim that Amit Hasan Sami authored the upstream implementation.

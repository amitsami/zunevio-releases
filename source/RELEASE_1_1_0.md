# Zunéviu 1.1.0 — Universal Android release candidate

Developer of this fork: **Amit Hasan Sami**  
Contact: amitsami110@gmail.com  
GitHub profile: https://github.com/amitsami

This is a candidate for review before the owner makes a public GitHub release. Nothing has been published to GitHub, submitted to Google Play Protect or certified by Google. The source remains a GPLv3 derivative of BitChord; retain its original copyright and license notices.

## Universal distribution

- One universal APK contains **arm64-v8a, armeabi-v7a, x86 and x86_64**. It is not restricted or named for one phone model.
- Minimum Android **8.0 / API 26**, target API 36. Android versions below 8 are unsupported; native codecs/OEM behaviour are not guaranteed on every device. Optional FFmpeg is not included for 32-bit x86.
- Version name **1.1.0**, version code **33**. The existing `com.zunevio.music.preview3` package and private **test** certificate are retained so the delivered v1/1.0.1 can be updated rather than uninstalled.
- The APK is R8-optimized and non-debuggable, but uses the existing benchmark/test signing identity. Do not describe this as Play Store release signing. An owner-controlled production signing/distribution plan is a public-release decision, not a reason to rotate the package or certificate to evade a warning.

## Expanded Home discovery

Home is no longer limited to two generic sections and a single personal row.

- A plan of **12 distinct catalogue-search sections**: English hits, community playlists, Chill & unwind, Pop discoveries, Rock discoveries, Focus flow, Workout energy, Electronic pulse, Jazz lounge, Acoustic moments, Late night listening and more discovery playlists.
- Four initial sections, with the two supplements arriving independently; remaining sections load in pages of up to three as the lazy Home feed nears its end. At most three generic discovery requests run concurrently, with per-request timeouts and a 30-minute successful-result cache. Pull-to-refresh also refreshes the two supplements.
- Qualified on-device listening can produce **Made for you plus up to four “More like…” rows**, using at most four distinct recent artists. Personal requests are limited to two concurrently. The existing eight-seed bound, ~20-second qualification, no-local-file seed rule and Settings off/reset controls remain.
- A row appears only when actual provider results are available. Rich test fixtures produce five personal rows; sparse/duplicate/failed provider responses may produce fewer. The application does not invent tracks, claim Spotify's algorithm, manufacture official chart rankings or promise a fixed number of rows offline.
- Track/browse identifiers are deduplicated across discovery rows. Recently played history is preserved independently rather than truncated or treated as a new-discovery claim.
- New English/Bengali Home headings. Existing generic category exclusions remain.

These are live catalogue searches/radios from the existing provider, **not a Spotify recommendation API integration**. Search phrases, selected track IDs and artist terms are sent to the provider. Results depend on network, account/provider access and available catalogue responses.

## Settings performance retained

The 1.0.1 Settings fix stays in place: no per-card sampled blur/shadow, cached lightweight glass-style paint, one Reduce blur observer and a distinct Atmos Boolean instead of observing full playback telemetry. All existing Settings controls, monochrome icons, normal Android switches, initial-red-Z wordmarks and developer links remain. No phone FPS measurement is claimed.

## Distribution hardening

- Remove `REQUEST_INSTALL_PACKAGES` and unused in-app APK downloading/installer code. The app no longer asks to be an APK installer or opens “install unknown apps” settings. The disabled updater's UI-facing types remain compatible; no update repository is invented or configured.
- Remove the dev flavor's global cleartext override. App traffic is HTTPS-only under the main manifest policy; previously configured plain-HTTP custom endpoints may stop working and should use a properly secured HTTPS endpoint.
- FileProvider shares only the intended `cache/shared/` media directory, not the old APK-update cache.
- Exclude credential-bearing general preferences and WebView data from Android automatic cloud backup/device transfer, in addition to existing auth/source exclusions. This does not delete installed app data. For portable ordinary settings, use the app's credential-filtered manual export; automatic transfer of those preferences is intentionally no longer available.

These changes reduce unnecessary privileges/exposure. They do not establish what triggered the reported Play Protect warning. No exact warning screenshot/classification has been supplied; no antivirus scan, Google verdict, third-party malware submission or complete dependency-vulnerability audit is claimed.

## Validation and remaining gates

See `VALIDATION_1_1_0.json` and `validation/1.1.0/` for current tests, APK identity/signature/alignment checks and Lint results. Historical reports describe older builds. The enabled live Genius test is not disabled to obtain a green test count. No physical Android phone or emulator is connected here.

Before public release, test installation and cold start on representative Android versions/ABIs; Home initial load/paging/refresh with slow/offline responses; several qualified listening seeds; recommendation off/reset during requests; Settings while playback is active; one-tap back, lyrics/translation, screen-off audio, notification controls, Bluetooth and OEM battery restrictions. Resolve relevant Lint/test issues rather than representing an installable APK as an all-green public release.

Install this candidate over the delivered v1/1.0.1. If Android rejects the update, do not uninstall or clear data blindly; check the installed package/certificate first. Keep Play Protect enabled and follow its legitimate scan/review process.

## Rebuild

Use JDK 21, official SDK 37.0 (inherited naming adapter), Build Tools 35.0.0, CMake 3.22.1 and the project's NDK.

```sh
python3 scripts/prepare_android37_legacy.py "$ANDROID_HOME"
./gradlew :shared:jvmTest :sharedUi:jvmTest :app:testDevDebugUnitTest \
  --continue --max-workers=1 -PzunevioSideBySide=true \
  '-Dorg.gradle.jvmargs=-Xmx1536m -XX:MaxMetaspaceSize=768m -XX:ActiveProcessorCount=2' \
  -Pkotlin.compiler.execution.strategy=in-process
./gradlew --stop
./gradlew :app:assembleDevBenchmark --max-workers=1 -PzunevioSideBySide=true \
  '-Dorg.gradle.jvmargs=-Xmx2048m -XX:MaxMetaspaceSize=768m -XX:ActiveProcessorCount=2' \
  -Pkotlin.compiler.execution.strategy=in-process
```

A new machine's default debug certificate cannot update the delivered APK. The source archive deliberately excludes all keystores, passwords, local configuration, compiled outputs and toolchain caches. Never publish the private test-signing backup.

## Measured final gates

- Unit tests: **1,132 passed / 1,133 total**, no skipped tests. Shared core 98/98, shared UI 50/50; Android 984/985. The single failure is the enabled live Genius search/scraping test.
- Full Android Lint: **198 errors, 664 warnings and 5 hints**; failed, no added suppressions. The same 198-error total existed in RC1. This count is not a malware diagnosis and does not make the APK a production-certified release.
- APK signing, identity, architecture and alignment results are in the final validation JSON. No physical-device acceptance, Play Protect review/appeal result, antivirus verdict or complete dependency security audit is claimed.

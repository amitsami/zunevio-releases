> Latest Android candidate: **1.9-rc1 (30)**. See `UPDATE_1_9_RC1.md` and `validation/1.9-rc1/` for current fixes and unresolved quality gates. Reports below are historical.

> Current Android beta: **1.9-beta2 (29)**. Read `UPDATE_1_9_BETA2.md` and `validation/1.9-beta2/` for current changes, exact test/lint outcomes, signing and device-validation limits. Older build reports below are historical.

# Zunéviu native fork

> **Current Android update: 1.9-beta1. Read UPDATE_1_9_BETA1.md and VALIDATION_1_9_BETA1.json first.** The sections below describe the earlier 1.8 handoff; its test counts, three-ABI scope and default integrations are historical, not the current validation.

Zunéviu developer: Amit Hasan Sami. Public contact: amitsami110@gmail.com; GitHub: https://github.com/amitsami. Upstream: https://github.com/kushagrasinghx/BitChord
Upstream commit: fe95bb60aa9cd205cf4ab8bd25a5bec71a595b4f

This is a GPLv3 derivative fork, not a claim that Amit Hasan Sami wrote the upstream code. Original source, copyright, licence, contributor acknowledgements and upstream documentation are retained. Review inherited third-party asset/font licences before a public release.

## Native UI, not an HTML shell

The Android and desktop applications keep BitChord's actual Compose UI, player engines, floating navigation, artwork transitions, lyric clock, word/letter sweep, queue, service integrations and native analysis code. They are not a WebView wrapping the design preview.

Zunéviu changes include distinct application/package identifiers, Amit Hasan Sami publisher metadata, Z icons, visible/export branding, Glass enabled by default for new Android installs, and a translucent lyric capsule connected to the actual player backdrop in both portrait and landscape layouts. Reduced-blur settings and unsupported-device fallbacks remain in place. Saved user preferences are not overwritten. Lyrics need a provider with word timestamps for the native letter/word sweep; line-only and unavailable lyrics retain upstream behavior rather than fabricating sung timing.

The HTML in design-preview remains a separate interactive concept. It is not evidence that its entire custom layout has been ported to native code. The native UI deliberately retains BitChord's own layouts and animation implementation.

## Platform scope

- Android: real native application. Development APKs were assembled for arm64-v8a, armeabi-v7a, x86_64 and universal; signing verification and final test results are recorded in VALIDATION.json. Development package: com.Zunéviu.music.dev. Minimum Android API 26. Advanced Android glass requires API 31+, with older-device fallback.
- Linux: native desktop compilation and UI launch exercised. Home, album navigation, Settings and player opening were inspected. FFmpeg needs GLIBC 2.35+ and libva; GTK3 is needed for JavaFX authentication. The workspace's older system libc could not load FFmpeg; tests were rerun with isolated Ubuntu 22.04 libraries without changing the system installation.
- Windows and macOS: source and platform packaging paths retained; manual multi-host validation workflow included. No successful Windows or macOS build/device run is claimed here.
- iPhone/iOS: NOT implemented. Shared code currently targets Android/JVM and includes Java-only dependencies. An iOS player, authentication/network/storage adaptations and a deliberate iOS port are still required before Xcode signing or an IPA is possible. An Apple Developer account alone does not implement this port.

Localization fixes add the missing Chinese language labels and system locale choices. The locale completeness test now maps BCP-47 tags to valid Android resource qualifiers; no completeness assertion was disabled. The live Genius provider test remains enabled and any network-dependent failure is reported, not hidden.

See VALIDATION.json and validation/ for the actual completed checks. Passing unit tests is not proof that every provider/account/device works.

## Building

Use Java 21. For Android, install the SDK platforms and the CMake/NDK versions requested by Gradle. The current official API-37 package uses the name platforms;android-37.0, whereas upstream AGP 8.10.1 expects android-37. For that exact release/minor-zero SDK only:

```sh
sdkmanager "platforms;android-36" "platforms;android-37.0"
python3 scripts/prepare_android37_legacy.py "$ANDROID_HOME"
./gradlew :app:assembleDevDebug :app:testDevDebugUnitTest
```

The helper creates a local compatibility copy, normalizes only metadata, checks that android.jar remains byte-identical, and leaves the original installation untouched. It is a temporary build adapter, not a supported replacement for a future Android toolchain migration. It refuses other SDK versions. Prefer a fully supported modern Android toolchain for a production release.

Desktop:

```sh
./gradlew :shared:jvmTest :sharedUi:jvmTest :desktopApp:test
./gradlew :desktopApp:run
./gradlew :desktopApp:createDistributable
```

Use gradlew.bat on Windows. Native analysis requires CMake and a C++ compiler. Build Windows/macOS installers on their respective hosts. The manual .github/workflows/zunevio-validate.yml runs build/test jobs on Linux, Windows, macOS and Android but does not publish a release or deploy a server. It has not been run on GitHub in this workspace.

## Release gates

- A development APK uses a debug key, not the owner's production signing identity. Do not submit it to a store as a release.
- Phone installation, background playback, media controls, audio routes, performance, accessibility and live-account integrations still need device testing.
- YouTube anonymous playback hit LOGIN_REQUIRED in this workspace. Do not bypass service verification or describe anonymous streaming as verified working. Legitimate sign-in/provider access may be needed.
- Set up owner-controlled release endpoints before enabling either updater. Update engines are retained but fork update checking stays off, so upstream binaries cannot replace Zunéviu accidentally.
- Supply your own credentials, signing keys, server endpoints and public maintainer email. Never commit secrets. Some inherited service defaults still refer to upstream services; inspect their terms and configure owned infrastructure before distribution.
- No public repository, store submission, hosted backend deployment or Windows/macOS/iOS release was created by this work.

Root README.md and DESKTOP.md remain upstream documentation; this file describes the fork and its actual status. The source archive is a working source tree, not a Git-history bundle.

# Zunévio Android startup crash correction

Developer of the Zunévio fork: **Amit Hasan Sami**. Public contact: amitsami110@gmail.com; GitHub profile: https://github.com/amitsami. Based on BitChord by Kushagra Singh and contributors. GPLv3 and upstream copyright notices remain in force; this is a derivative native application, not a claim of original authorship of BitChord.

## Evidence and root cause

The supplied `Logs.rtf` reports `IllegalArgumentException: Invalid color value @android:color/transparent` in Compose resources' `parseColorValue`, through `painterResource`, while `HomeTitle(HomeScreen.kt:500)` composes. The supplied APK itself contains that reference in `assets/composeResources/com.music.bitchord.sharedui.resources/drawable/ic_logo.xml`. This is a concrete resource-parser startup failure, not evidence of a phone RAM, account, permission or audio problem.

Android framework vector resources can resolve Android color references. Compose Multiplatform's shared XML vector parser cannot resolve external Android resources in path colors. Replace the reference with the literal transparent ARGB value `#00000000`. White stroke, path geometry, size, native navigation, player transitions, floating tabs, lyrics animation and glass code are unchanged by this correction. The matching Android-native logo, notification and launcher vectors are normalized too.

## Changes in this correction

- Four vector XML resources use literal transparent path fill.
- Android Settings colophon identifies Zunévio and developer Amit Hasan Sami, and explicitly labels the retained BitChord source / upstream developer / upstream community links.
- Desktop packaging publisher metadata uses Amit Hasan Sami instead of the previous placeholder publisher.
- Android version code is 27; version name is `1.8-crashfix1`. Default development ID remains `com.zunevio.music.dev`; production ID remains `com.zunevio.music`. **The delivered build** uses `-PzunevioSideBySide=true`: ID `com.zunevio.music.dev.crashfix1`, label `Zunévio Test`, so the old app and its data remain untouched.
- Four new shared-UI JVM regression tests use Compose's **actual** `decodeToImageVector` parser, not a mock: decode the Home logo, verify transparent fill and visible stroke, decode all shared vectors at several densities, and reproduce the original exception using the old color value.
- `scripts/check_vector_resources.py` provides an additional source / packaged-APK path-color check. This is a static packaging check, not a substitute for the Compose tests or device testing.

## Validation scope

See `validation/crashfix1/RESULTS.json` for this correction's measured results and the included build logs. The inherited `VALIDATION.json`, old `validation/` contents, original build logs and older documentation describe prior work supplied with the archive. Their prior test counts / desktop UI claims have **not** been independently reverified by merely reading them. This correction does not establish that every external provider works.

No physical Android phone or Android emulator is connected in this environment. Installation, cold launch, navigation, background playback, Bluetooth, Android Auto, provider sign-in and OEM battery restrictions therefore still require real-device validation. Do not call this a production-validated release.

## Installation and signing

This is a development APK, not a Play Store release. A new build environment generates a new debug signing key. Check `RESULTS.json` for comparison with the supplied APK's certificate. A higher version code alone does not permit an update when certificates differ.

If Android reports a conflicting package / incompatible update, do **not** immediately uninstall if you need saved settings or app-private downloads: uninstalling can delete them. Back up/export data where possible. A seamless in-place update needs the original signing key through a secure signing process; the private key is not recoverable from the APK. The delivered side-by-side test package uses this separate-ID option: install `Zunévio Test`, not the older `Zunévio Dev`. Data/settings are not automatically copied between these independent app sandboxes. Never share a production keystore password, account password, cookies or API secrets in chat.

## Information needed

### To confirm this crash correction now

1. Phone confirmed by the user: POCO M6 Pro 5G, Android 15. Immediate launch crash confirmed. No further personal information is needed for this resource-parser fix.
2. User confirmed the supplied build crashes immediately on launch.
3. After installing a corrected build: the exact reproduction steps and a **new** crash log if it still fails. Redact personal identifiers, tokens and account data before sharing logs.

### Public About / release details (only when ready)

- Confirm display spelling: `Zunévio`; developer is already set to `Amit Hasan Sami`.
- Confirmed public developer email: amitsami110@gmail.com. Confirmed GitHub profile: https://github.com/amitsami. No project repository or hosted support website has been supplied or created; retained upstream links are labelled as upstream.
- For a public release: owner-controlled repository and release/update endpoint, privacy policy and support contact, distribution channel, and an owner-controlled signing arrangement. Do not use the test debug key as a production identity.

### Optional services — not required for the logo/startup fix

- Last.fm scrobbling: own API registration / credentials through a secure local configuration.
- Listen Together hosting: own server URL and deployment decision. The inherited default points at the BitChord upstream server; availability or authorization for your fork is not guaranteed.
- Discord presence: own application registration / ID if enabling fork-specific presence.
- YouTube and other providers: availability, legitimate account access and provider terms vary. No anonymous-playback guarantee or service-verification bypass is implemented here.

## Rebuild

Use JDK 21, the checked-in Gradle wrapper, SDK platforms 36 and 37.0, and the CMake/NDK versions requested by Gradle. See `ZUNEVIO_README.md` for the metadata-only API-37 naming adapter inherited from the supplied fork.

```sh
python3 scripts/prepare_android37_legacy.py "$ANDROID_HOME"
python3 scripts/check_vector_resources.py
./gradlew :app:assembleDevDebug :app:testDevDebugUnitTest :sharedUi:jvmTest :shared:jvmTest -PzunevioSideBySide=true --continue --console=plain
python3 scripts/check_vector_resources.py app/build/outputs/apk/dev/debug/app-dev-universal-debug.apk
```

Keep real credentials in local environment/configuration excluded from version control. For low-memory builders, use one Gradle worker and in-process Kotlin compilation. The source zip excludes local SDK/JDK caches, build outputs and signing keys.

## Measured results for the delivered build

- APK assembly-only task: **BUILD SUCCESSFUL**.
- Android unit tests: **970 passed / 971 total**, zero skipped; one enabled live Genius test failed.
- Shared core JVM: **46/46 passed**. Shared UI JVM: **38/38 passed**, including all four new real-parser crash regression tests. The final contact/side-by-side build reused unchanged shared/JVM test results as UP-TO-DATE; Android tests ran again.
- Direct Genius search HTTP probe with the application's `BitChord` User-Agent: **403** in this environment. This is not a guarantee of the response on the user's network, and the failed live test has not been disabled or rewritten to pass.
- Both delivered APKs: signature verified, ZIP alignment checked with 16-KiB page alignment, corrected shared vector assets byte-matched to source, final public contact strings checked in packaged DEX. These ZIP checks do not establish native ELF compatibility on every 16-KiB device.
- The inherited dev-flavor manifest hardcoded the old app label; this is now a flavor-supplied manifest placeholder so the delivered app is consistently labelled **Zunévio Test**.

### Quick test on POCO M6 Pro 5G / Android 15

1. Install the ARM64 test APK and open **Zunévio Test** (not the old Zunévio Dev). No uninstall is required.
2. Check cold launch → Home → Settings. Confirm developer name/email/GitHub.
3. Try a local audio file first, then navigate the native player, queue and lyrics. Online playback/lyrics depend on the chosen provider and account/network access.
4. Test screen-off/background playback and notification controls.
5. If it crashes, share the new log and exact action. Do not resend passwords, cookies or keys.

No additional personal information is required at present. A new test-package sandbox will not automatically inherit the old app's saved settings, sign-in or app-private downloads.

# Zunéviu v1 — approved Settings and navigation update

Developer of this fork: **Amit Hasan Sami**  
Email: amitsami110@gmail.com  
GitHub: https://github.com/amitsami

## Changes

- Native Compose Settings with rounded glass group cards, directional edge highlights and monochrome colour scheme. The glass samples a separate stationary background, not text or controls. API/low-RAM and Reduce blur fallbacks remain; no new perpetual animation loop or per-row image capture was introduced.
- Approved soft-solid, matte black/white section glyphs without icon tiles. Existing Android Material switch controls are retained.
- Home and Settings account wordmark render **Zunéviu**, with only the initial **Z** red. The actual account name/email remain underneath the Settings wordmark.
- Top-right profile action becomes a Settings gear with a 48dp hit target; account management remains accessible in Settings.
- Only Explore's footer glyph changes. Original selection-pill code and existing spring/tab motion remain, with sampled glass re-enabled on supported, non-low-RAM devices when Reduce blur is off. The original top-level footer's non-folding layout is retained.
- All existing Settings string references and callback expressions are retained. No JioSaavn, Discord or scrobbling features are restored.
- Settings colophon includes the developer name, email and clickable **@amitsami** GitHub link. Upstream attribution and GPLv3 remain.
- Version name **1.0**, increasing Android version code **31**. Package **com.Zunéviu.music.preview3** and private test signing lineage are preserved for an update over the supplied RC1/beta2 packages.
- Prior literal-colour startup crash correction, ASCII HTTP identity/translation failure handling and one-action Settings-back correction are retained. The network identity now reads `Zunevio/1.0` (ASCII only).

## Validation and limits

See `VALIDATION_V1.json` and `validation/v1/` for measured results from this build. Earlier version reports in this archive are historical, not current test claims.

The Android tests use the debug variant; the delivered APK uses the R8-optimized, non-debuggable benchmark variant with a **test** signing identity. No physical phone or emulator is connected. Native rendering, scrolling smoothness, FPS, playback, provider authentication, OEM background restrictions and real taps still require device acceptance. No production/store readiness or all-provider success is claimed.

The enabled live Genius lookup test failed; it was not disabled or weakened. Full Lint has inherited unresolved findings (the RC1 report records 198 errors). This change does not claim those were fixed, and the v1 validation report states whether full Lint was rerun.

## Install on the POCO M6 Pro 5G

Use the **ARM64** APK. The universal APK is a larger alternative, not an additional app to install. Over the supplied RC1/beta2 with matching package/certificate, v1 has a higher version code and should update without intentionally clearing data. If Android rejects an update, **do not uninstall to resolve it** until you have backed up the data and checked the installed package/signature. Other older packages do not migrate automatically.

Check after installation:

1. App info shows **1.0**; open Home and confirm only Z is red.
2. Tap the top-right gear; inspect Settings in dark and light modes, all sections, the normal switches and GitHub link.
3. Move Home → Explore → Library; check the original travelling selection animation, no layout redesign, and the compass icon.
4. Open Accounts, Sources and Equalizer; verify one back tap returns to Settings.
5. Try translation with online/offline responses, change track while loading, and verify ordinary playback, notification controls and screen-off audio.
6. If anything crashes, export the **new v1 log** with reproduction steps; redact private account tokens/cookies.

## Rebuild

JDK 21, official Android SDK 37.0, target 36, minimum 26, Build Tools 35.0.0, CMake 3.22.1 and NDK requested by Gradle. The legacy SDK naming adapter is inherited; no SDK implementation bytecode is modified.

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
python3 scripts/check_vector_resources.py app/build/outputs/apk/dev/benchmark/app-dev-universal-benchmark.apk
```

A fresh environment's default debug key will not update the delivered APK. Signing keys/passwords, local configuration, build outputs and toolchain caches are intentionally excluded from the source archive. Keep the existing private signing backup secure; do not publish it.

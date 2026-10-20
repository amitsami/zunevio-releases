# Build Zunévio for Android

Use JDK 21, the Gradle wrapper, Android SDK platforms 36 and 37.0, build-tools 35.0.0, NDK 27.0.12077973 and CMake 3.22.1. Linux with at least 7 GB memory is recommended.

```sh
sdkmanager 'platforms;android-36' 'platforms;android-37.0' 'build-tools;35.0.0' 'ndk;27.0.12077973' 'cmake;3.22.1'
chmod +x gradlew
./gradlew :app:assembleProdRelease -Pkotlin.compiler.execution.strategy=in-process --max-workers=2
```

Dependencies are resolved using the exact versions in the Gradle files from Google Maven, Maven Central and JitPack. This build explicitly names the official release target android-37.0. The retained legacy metadata adapter is not required; official android.jar is unchanged. Source includes the NewPipe compatibility replacement and native decoder source/build configuration.

A clean checkout builds unsigned unless you supply your own `keystore.properties` following the example. Generate your own key to install a modified build. Owner production keys and account tokens are intentionally excluded. Optional Last.fm credentials and party-server URL are not required for the baseline app; related integrations require the operator's configuration.

QA: `./gradlew --continue :app:testProdReleaseUnitTest :shared:jvmTest :sharedUi:jvmTest :app:lintProdRelease`.
The live Genius network test is reported separately if it fails; deterministic failures or lint errors block publication. See each release's VALIDATION.json for actual results, not a claim of physical-device certification.

## Owner release operation

In the private development repository, update both versionName and versionCode, update RELEASE_NOTES.md, commit and push. Run **Zunévio production release** with `publish=true`. The workflow builds once, validates the certificate/package/tests/lint, creates matching APK/source/checksum assets, stages two draft releases, verifies uploads, then publishes both. GitHub has no cross-repository transaction; if publication fails after one repo becomes live, inspect both before retrying and never replace a published APK/version.

The private repository holds signing/publishing secrets in GitHub Actions only. The public download repository has no signing secret. Never commit keystores or passwords. Publish a newer stable release, not just a commit, to trigger update discovery. Preserve the same application ID and production signing key and increase the numeric version code for every update.

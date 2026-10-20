# GitHub Releases → Zunévio updates

No repository has been published or selected for you. This build starts with the updater **unconfigured**. It never contacts the upstream project's releases and does not download an APK automatically.

## After testing 1.2.0

1. Create the public repository under your own GitHub account. Upload the complete corresponding source, GPLv3 license, upstream attribution and build instructions. Keep private keys, credentials and account backups out of Git.
2. In the app, open **Settings → App updates → Set repository**. Enter `owner/repository` or its `https://github.com/owner/repository` URL. Only set a repository you trust and control.
3. Enable update notifications and allow Android's notification permission. If it was denied, use **Android notification settings**. Foreground checks still work without notification permission.
4. Open **Releases → Draft a new release** on GitHub. For the next version, use a stable numeric tag such as `v1.1.2`. Add your changelog to the release description. Drafts and pre-releases are not offered by this stable update channel.
5. Increase **both** `versionName` and `versionCode` in `app/build.gradle.kts`. The supplied candidate is `1.2.0`, code `35`.
6. Build the same app track, package and signing identity. Attach one signed APK whose name contains `universal` and ends in `.apk`, e.g. `Zunevio-1.1.2-preview-universal.apk`. A source ZIP or an Actions artifact alone is not an installable GitHub Release asset.
7. Upload `SHA256SUMS.txt` for people verifying manually. The app additionally requires GitHub's own `sha256:...` asset `digest` returned by the Releases API; a missing digest fails closed. New GitHub release asset uploads provide this digest. If absent, re-upload the signed asset and check again.
8. Publish the stable release. Test **Check for updates** on a device still running the previous build. The app displays the release description as plain changelog text, verifies its download, then hands installation to Android after the user taps Install.

A regular commit/push, README change, tag without a published release, same-version release, or a desktop-only release will not produce an Android download. No webhook secret, access token or GitHub password belongs in the app.

## Signing and package continuity (important)

This review APK is `com.zunevio.music.preview3`, signed with the **existing private development/test signing identity**, like the previously supplied 1.1.0. It is an optimized, non-debuggable benchmark build, not a production signing identity.

- To update these existing test installs, build `:app:assembleDevRelease -PzunevioSideBySide=true` with that **same** test key supplied privately through `keystore.properties` / GitHub Actions secrets. `assembleDevBenchmark` uses the locally restored Android debug keystore; the packaged source never includes it.
- A normal production build uses `com.zunevio.music`. It **cannot** update `preview3`; it installs as a different app. Select the final production identity and securely back up an owner-controlled release key before the first public production distribution. Do not rotate package/key to evade security classification.
- Never commit or attach a signing keystore to a public release. Repository secrets for the optional manual Android build are `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, and `KEY_PASSWORD`. Keep the key itself privately backed up. The manual Android workflow builds artifacts only; it does **not** publish a release. The inherited desktop release workflow is also manual-only now, so adding an Android release tag cannot unexpectedly start desktop publishing.
- Key rotation is intentionally not supported by this simple exact-certificate updater. Plan a separate audited migration if rotation is needed.

## Update behavior and safety

- Checks at app startup, manually, and approximately every 12 hours on connected networks using WorkManager. Android battery restrictions, Doze, force-stop and disabled notifications can delay or prevent background alerts; this is not instantaneous push notification delivery.
- Download starts only after a tap. It runs on IO, with throttled progress updates. Closing the update dialog does not stop a download; Cancel does. Process death requires retrying. No install happens in the background.
- Only HTTPS GitHub metadata/assets and a narrow GitHub CDN redirect allowlist are accepted. Downloads are size-bounded and checked against GitHub's SHA-256 digest, the installed package, increasing versionCode, minimum SDK, non-debuggable flag and exact signing certificate set.
- APKs stay in private update cache; only the APK selected for installation is granted temporary read access to Android's package installer through FileProvider.
- Android may ask to allow installs from this app. If the user chooses to allow it, return to Zunévio and tap Install again. Android's own installer and Play Protect make the final installation decision. Never disable Play Protect.
- The sideload update feature requires `REQUEST_INSTALL_PACKAGES`, restored deliberately at the user's request in 1.1.1. This can receive additional scrutiny. No antivirus, Google approval, Play Store policy compliance or permanent warning-free guarantee is claimed. A Play Store distribution would need its own compliant update channel/policy review.
- Android system app labels, notification titles, launcher text and other OS-controlled plain strings do not support our red-initial rich text. The red-Z rule applies to app-rendered text and generated share artwork.

## Device acceptance checklist before publishing

- Settings root → each category → system Back / toolbar Back; Audio quality → Sources → Back; switching theme/light/dark and large font.
- Scroll Settings and Appearance while music plays; compare on a lower-end Android 8+ device and a modern device. No per-device FPS guarantee is claimed without measurement.
- Publish a **test** newer release in the chosen repo with a matching signed universal APK. Check changelog, notification tap, download progress/cancel/retry, permission refusal/return, Android install, and preserved library/account data.
- Reject a wrong package, wrong signer, older/same versionCode, debug APK, corrupted/truncated download, missing digest, foreign host, and incompatible minSdk.
- Test offline, no stable release, rate-limited API, notifications denied, force-stop and background restrictions. Do not publish the test release as production without review.

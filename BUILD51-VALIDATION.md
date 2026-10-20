# Build 51 validation — Zunéviu 1.2.6

Source: the current Build 50 backup plus the dedicated native Spotify sign-in Activity and login navigation policy. No changes to audio lyric timing/calibration.

## Automated validation
- shared JVM: 169 tests, zero failures, zero skipped (includes three Spotify login policy tests).
- sharedUi JVM: 81 tests, zero failures, zero skipped.
- app release unit tests: 1074 tests, one failure, zero skipped.
- Total: 1323 / 1324 pass.
- The sole failing test is the unchanged real-network Genius live-fetch test: HTTP 403 / provider blocked. It has not been skipped or weakened.
- Existing Spotify wiring assertions were updated to exercise the new Activity wiring, no-reload polling/progress, page restoration, trusted landing requirement and visible error/retry handlers. These source-wiring assertions are NOT runtime Android tests.

## Acceptance still required
An authenticated end-to-end login on the user's affected Android device was not available in this environment. The dedicated Activity removes sign-in from the animated music screen and prevents polling/progress-triggered page reloads by construction, but it is not proof that the reported blink is resolved or that Spotify accepts login/loads playlists. Test the supplied APK on the affected phone before recommending GitHub publication.

Genius live access is still blocked in this environment. The release notes must not claim it was restored. Build warnings include existing R8/Kotlin metadata compatibility warnings; no clean lint claim.

## Publication and backup
No GitHub/app-store publication has been performed for this build. Source backups exclude owner signing keys, keystore.properties, local.properties and password.txt. Keep upstream licenses and source notices intact.

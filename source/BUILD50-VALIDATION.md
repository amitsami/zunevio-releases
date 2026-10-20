# Zunéviu 1.2.6-preview3 — Build 50

Test preview only. Not approved for publication.

## Changes
- Disposable audio cache default reduced to 128 MiB; existing saved cache preferences remain unchanged. Settings can now select 128 MiB. Coil image cache 64 MiB; canvas cache 64 MiB; remote-art cache 32 MiB. These are component budgets, not a hard whole-app storage cap.
- Remove an old update.apk only when its package matches this app and its version is already installed or older. Preserve newer and unknown installers. Remove an abandoned download.apk only after 24 hours, while no update download is active.
- Never target saved songs, playlists, account cookies or session data in this cleanup.
- Spotify login uses fresh HTTP assets; Retry clears WebView HTTP cache before reloading, not cookies or DOM storage. This is not a verified fix for the reported login failure. Need a new recording or precise failure stage from the affected Android device.
- Genius uses cancellable network requests, explicit unavailable/error status and manual retry. Stop repeated search spelling requests on a provider block. Parse both supported search response shapes and reject unexpected off-site result URLs. Other enabled lyric providers can still run.
- Existing audio lyric timing/calibration remains unchanged.

## Validation
Current source test run: shared 166/166, sharedUi 81/81, app 1073/1074. Total 1320/1321 pass; zero skipped.
The unchanged real-network Genius test fails: LyricsProviderUnavailableException, HTTP 403. Search and lyric-page probes returned Cloudflare challenge. No challenge bypass, fake production data, weakened live assertion or claim of restored Genius access.
New deterministic tests cover Genius HTTP failures, response parsing, cancellation, blocked retry count, URL trust, cache budgets and installer cleanup guards. Fixture-based tests do not prove that Genius is live or that Spotify login works.
No physical Android or authenticated Spotify end-to-end validation was available. Do not describe this preview as fully fixed or bug-free.

## Publication
Do not publish to GitHub or app stores without user approval after APK testing. Owner signing secrets are excluded from source backups.

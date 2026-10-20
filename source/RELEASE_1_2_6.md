# Zunéviu v1.2.6 — Build 51

## Spotify sign-in stability
- Move Spotify sign-in into its own native Android Activity, independent of the music screen's animated Compose hierarchy.
- Keep a single WebView instance per Activity. Progress updates and session polling do not reload or replace the page.
- Keep the progress bar's layout space stable; reserve a normal keyboard-resizable viewport and apply system/IME insets.
- Restore the page's navigation state across Activity recreation. Reload only when the user taps Retry.
- Require the Spotify web-player landing origin before accepting the session cookie; a stale cookie on the login form no longer completes sign-in prematurely.
- Preserve cookies and DOM storage on Retry, while refreshing HTTP assets. Keep HTTPS origin restrictions and default TLS error handling.

## Retained cache and lyrics reliability changes
- Default disposable audio cache 128 MiB; reduced image, canvas and remote-art component budgets. Existing explicit cache preferences are retained.
- Safely clean already-installed update APKs and abandoned partial update downloads without targeting music downloads or account data.
- Distinguish temporarily unavailable Genius requests from genuinely missing lyrics. Cancel network requests correctly and stop repeated blocked searches.
- Preserve the existing audio lyric timing calibration.

## Known limitations / release acceptance
- Genius live access remains blocked with HTTP 403 in the build environment. Error handling changes do not restore provider access.
- Automated navigation/lifecycle wiring tests are not an authenticated Android login test. The affected phone must verify that sign-in no longer blinks and that playlists load after sign-in before publication is recommended.
- This file is release preparation, not confirmation of a GitHub publication.

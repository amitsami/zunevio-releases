# Zunéviu 1.2.6-preview2 · Build49

Private owner acceptance build. Do not publish without approval.

## New corrections over preview1
- Spotify login uses its native responsive mobile WebView user agent; removed desktop spoof and broad forced CSS. Navigation allowlist applies only to main frames, not verification subframes. Standard WebChromeClient, progress, main-frame HTTP/network errors, stalled/blank-page detection and visible Retry. HTTPS and existing provider allowlist retained; no TLS bypass.
- Convert to video resolves a catalogue match before changing playback. Missing match shows Video unavailable and leaves audio alone. Requests cancel cleanly; stale completion cannot change a newly selected song.
- A missing playable stream on initial video opening shows Video unavailable, clears the failed video request and restores audio. Timeouts have a separate connection/retry message.
- Video player disclosure is Lyrics followed by a chevron; opens the same lyrics panel. Audio lyric clock and calibration untouched.
- All preview1 changes retained. Cleanup remains scoped; no feature/native-library removal or broad dependency changes. Universal APK still covers all four ABIs.

## Validation limits
No physical Android device, Spotify/Google account sign-in or live video buffering acceptance was performed. Static wiring and unit tests cannot prove provider login works on a user's phone. This is not a 100% bug-free claim. Mandatory upstream licenses preserved.

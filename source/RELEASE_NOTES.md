# v1.2.5 — Build 47: restore live video lyrics

- Put Zunéviu developer/maintainer Amit Hasan Sami and GitHub @amitsami first in Licenses & source. Move the retained upstream attribution and full licenses to the separate Open-source notices view; keep matching source downloads directly accessible.
- Restore line and word timestamps on enabled lyric-provider fallbacks during inline music video playback. Previously, if exact-video captions were unavailable, the fallback was deliberately flattened to plain text; this prevented the live caption and highlighting from appearing.
- Keep exact-video captions first in the lookup order. Disabled/hidden providers remain disabled; genuinely untimed lyrics remain untimed.
- Keep original provider timings rather than inventing intro delays or inferring a speed from recording duration. A fallback matched by title/artist/duration can still differ from an alternate video edit, so perfect video synchronization is not claimed. Video retains its own manual Lyrics offset.
- The current lyric strip, full panel and lyric seeking continue to use the active inline-video position and separate video offset. No new playback engine or clock changes.
- Preserve build46's visible -0.4s audio default/migration without double application. Voice search, playback services, video quality, inline-video rendering and audio timing remain unchanged.
- Automated checks are recorded in the accompanying validation report. This build is not physically device-tested and is not published to GitHub.

---

# v1.2.5 — Build 46: audio lyric calibration

The user confirmed that -0.4s aligns all audio tracks tested on their device. This build applies that value as the visible Android audio-lyrics default, not as a hidden extra shift or an automatic acoustic measurement.

- Fresh/previous-zero audio settings migrate once to -400 ms.
- An existing -400 ms or other nonzero manual offset is preserved, never added twice.
- Explicitly saved audio zero remains zero on subsequent launches.
- Audio offset controls, highlights and lyric-line seeking use the same separate saved value. Reset restores the Android audio default (-0.4s).
- Inline video retains its existing saved offset and timing behavior; voice search and audio/video engines are unchanged.
- Real-device sync after installation is not certified, and availability/quality of provider timestamps still varies. No GitHub publication is authorized for this build.

---

# v1.2.5 — Build 45 (unreleased audio-lyrics candidate)

- Restore the unmodified v1.2.3 frame-steered audio lyric clock, including pause/resume handling.
- Restore the audio panel scroll/backing-line presentation lead without shifting highlight/seek timestamps.
- Make the compact audio lyric caption switch immediately rather than waiting through a 180 ms slide/fade.
- Retain build44 video timing, voice search, provider selection and saved lyric offsets.

A user screen recording of Sunflower (Post Malone & Swae Lee) shows the compact caption arriving late; the user reports a +0.0s lyric offset. This does not by itself identify an engine-clock vs. recording/provider-timestamp discrepancy. No global timing shift has been guessed, and physical-device synchronization has not been certified. This candidate is not published.

---

# Zunéviu 1.2.5 — Build 44

## Automatic-language voice search — this build's only functional change
- Removed the Bengali/Hindi/English picker and the fixed-language request. Opening the microphone panel now starts the permission/listening flow without asking the user to choose a language.
- On Android 14/API 34 and later, request language detection and high-precision automatic language switching from the installed Android speech service. No language allowlist is imposed by the app.
- Available languages still depend on the speech service and its models. This does NOT add every world language or Spotify's proprietary recognition engine, and does not guarantee accurate transcription of singing, proper names or every accent.
- Android 8–13 still installs/runs the same universal APK, but this automatic voice-search flow explains its Android 14 requirement instead of silently forcing English. Typed search remains available.
- Check the speech service's request support before listening. Because optional flags can be ignored even after that check succeeds, language-detection callbacks and confidence determine whether a final transcript may auto-submit.
- Unconfirmed language, missing-model switching failures or low-confidence words remain visible for review. They are not silently auto-searched as though detection succeeded; the user may retry or explicitly search the displayed words. There is no language-selection step.
- Select the highest-confidence nonblank recognition alternative when usable scores are provided. No-score results preserve the provider's order.
- Pending service checks have a short fallback timer; recognition has a 30-second watchdog. Cancellation, backgrounding and retries destroy the old recognizer and ignore stale callbacks.
- Explicit microphone permission, opt-in foreground recording only, partial text, permission/network/no-match errors and pausing our music before listening remain.

## Retained unchanged from build 43 — lyric timing
- Removed the lyric panel's 350–500 ms future scroll/focus anchor and the 250 ms backing-vocal pre-reveal. Focus now follows the current playback timestamp rather than anticipating it.
- Video-mode lookup prioritizes enabled exact-video timed captions before album/title-matched lyric providers, without overriding disabled/hidden source preferences.
- Audio/video mode is part of request deduplication, including when the same recording ID is retained. Manual provider requests apply the same timing policy.
- Album lyrics can have different intros, speeds or edits. When a result does not carry exact-video timing, its words remain available as plain text; unrelated album timestamps no longer drive video highlights. This is a correctness fallback, not automatic acoustic realignment or a claim that every music video has synced lyrics.
- Existing engine-authoritative position, buffering/seek clock and manual lyric offset remain. No arbitrary fixed delay or duration-based time stretching has been added.

## Installation and validation
- Android 8.0+; package com.zunevio.music, version 1.2.5, build 44. Same production signing identity.
- See the delivered VALIDATION.json for actual test/lint results and corresponding-source commit.
- No physical-device microphone recognition, background playback, lyric synchronization or rendered UI testing has been performed. These are targeted code changes, not a bug-free or device-level verification claim.
- No new GitHub publish is performed by this task.
- Sources: Android RecognizerIntent and SpeechRecognizer API reference: https://developer.android.com/reference/android/speech/RecognizerIntent and https://developer.android.com/reference/android/speech/SpeechRecognizer

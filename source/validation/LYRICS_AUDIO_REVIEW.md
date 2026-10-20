# Audio lyric synchronization review — unreleased build45

Based on delivered v1.2.5 build44. No GitHub release has been issued.

## Evidence and scope

The user supplied a 17.172-second screen recording of Sunflower (Post Malone & Swae Lee), showing the compact lyric caption arriving late, and reported a +0.0s lyric offset. The large panel scroll is not the only affected display. Local automatic speech timestamps are approximate and are not a precise acoustic calibration. The installed provider, internal controller samples and phone audio pipeline are not exposed in that recording. The recording cannot identify the entire root cause or certify a corrected APK. Raw user audio/video is not included in this source archive.

## Candidate changes

- Route audio through the unmodified published v1.2.3 LyricClock, including hold/restart behavior.
- Restore audio panel scroll and backing-line presentation lead. Highlight and seek timestamps are not advanced.
- Switch the compact audio caption immediately at its timestamp, eliminating its additional 180 ms entrance fade/slide. This is not represented as correcting the entire observed delay by itself.
- Keep build44 inline-video timing, speech recognition, audio/video playback engines, provider selection and saved lyric offset unchanged.
- No guessed global/per-track offset and no special case for Sunflower.

## Validation boundary

The prior 37 standalone tests covered the original clocks and 12 baseline routing/presentation regressions. Two further caption-transition tests were added for build45. Full Android compile/unit/lint results will be recorded in the candidate validation report. Physical-device acoustic synchronization remains untested, and no 100% accuracy claim is made. Do not publish or describe this candidate as phone-verified.

## Completed Android checks

Production Kotlin/Java compilation passed. Full app/shared/sharedUi tests: 1281/1282 passed; only the pre-existing live Genius network test failed. All 39 focused lyric-clock tests passed. Android lint completed with 0 errors and 683 warnings. These do not certify the exact delay on the user’s device.

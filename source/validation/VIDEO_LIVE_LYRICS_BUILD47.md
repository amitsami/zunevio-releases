# Build47 — live video lyrics restoration

## Root cause and targeted change
The UI already passed inline video position and the separate video offset to its live strip and full lyrics panel. However, LyricsRepository.videoTimingLines flattened every non-video-keyed provider to LyricLine(0, text) in video mode. Missing exact-video captions therefore meant no timed lyrics even if another enabled provider returned timed lyrics. The fallback now preserves those timestamps, word cues, backing lines and gaps unchanged.

Exact-video captions retain lookup priority. Source order, enable/disable filtering, and hidden-source filtering are extracted unchanged into a tested helper. No provider is silently enabled. Truly untimed responses are not made to appear synchronized.

## Limits
Restoring fallback timestamps is not acoustic alignment. Album and video edits may have different intros/speed, and timestamps may themselves be inaccurate. No duration-based offset or stretching is guessed. Use the video's independent Lyrics offset when necessary. Audio's user-confirmed -400 ms default and persistence are untouched.

## Checks
VideoLyricTimingTest covers all-provider timestamp preservation, word/gap cues, genuine plain text, mode round-trips, exact-caption priority, audio source order, and disabled/hidden filtering. VideoLiveLyricsPlayheadTest exercises the actual playhead and clock objects for offset isolation, seek inversion, paused seek and offset discontinuities. Existing audio-calibration/clock suites remain enabled. These are unit tests, not Android rendered-UI, hardware-decoder or audible real-device tests.

See VALIDATION.json for final build/test/lint results and video-live-lyrics-review.json for scope evidence. No GitHub publication is authorized.

## Creator-credit request added to the same build
The main Licenses & source dialog now presents Zunéviu developer/maintainer Amit Hasan Sami, GitHub @amitsami, and the official release/source download link. Original-project attribution and license texts remain available from the separate Open-source notices view. The bundled and root NOTICE files match, preserve upstream attribution, licenses and warranty wording, and do not claim that all upstream code was originally written by the maintainer. CreatorCreditNoticesTest is an asset/source guard; Android rendered-dialog and external-link launch testing are not claimed.

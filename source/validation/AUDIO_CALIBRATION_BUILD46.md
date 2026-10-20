# Build46 audio calibration

The user reported that -0.4s aligns all audio tracks they tested. This is a user-confirmed calibration, not a measured universal offset for every Android device.

Android now stores a separate audio_lyrics_offset_ms preference. Missing audio settings migrate from a legacy nonzero offset without addition; a legacy zero/fresh install uses -400 ms. An already stored audio value, including an explicit zero, wins on every subsequent initialization. Audio controls display/edit that actual value. Reset restores the default -400 ms; manually selecting zero remains supported. Rendering and lyric seeking use the same audio offset. No extra constant is added to the lyric clock.

Inline video reads/writes the existing lyrics_offset_ms setting, unchanged. Desktop hosts use the previous shared offset through default interface implementations, so this Android calibration does not silently affect desktop playback. Voice search, audio/video engines and lyric provider selection are unchanged from build45.

Tests cover migration, no double application, zero preservation, repeat initialization, mode selection, display/reset/seek wiring and Android host binding. Source-wiring guards are not a substitute for physical-device testing. Automated build/test/lint results are saved separately. Physical-device acoustic synchronization of the final APK is not certified. GitHub publication is not authorized.

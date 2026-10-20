package com.music.bitchord

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Source wiring guards; actual preference persistence still requires Android runtime testing. */
class AudioLyricsOffsetIntegrationTest {
    private fun app(path: String) = File("src/main/java/com/music/bitchord/$path").readText()
    private fun ui(name: String) =
        File("../sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/player/$name.kt").readText()

    @Test fun migrationUsesOneSeparatePersistedAudioPreference() {
        val s = app("data/settings/AppSettings.kt")
        assertTrue(s.contains("AudioLyricsOffset.initial("))
        assertTrue(s.contains("prefs.contains(KEY_AUDIO_LYRICS_OFFSET_MS)"))
        assertTrue(s.contains("putInt(KEY_AUDIO_LYRICS_OFFSET_MS, audioLyricsOffsetMs.value)"))
        assertTrue(s.contains("legacyMs = lyricsOffsetMs.value"))
    }
    @Test fun renderAndLyricSeekSelectTheSameOffset() {
        assertTrue(ui("PlayerLyrics").contains("PlayerSettings.lyricOffsetFor(videoTiming)"))
        val n = ui("NowPlayingScreen")
        assertTrue(n.contains("PlayerSettings.lyricOffsetFor("))
        assertTrue(n.contains("videoTiming = musicVideoArtwork != null"))
        assertTrue(n.contains("adjustedLyricsSeekTarget(lineTimeMs, lyricsOffsetMs)"))
        assertTrue(ui("PlayerSidePanels").contains("lyricOffsetFor(videoTiming = false)"))
    }
    @Test fun videoKeepsTheLegacyOffsetAndSetter() {
        val s = ui("PlayerHost")
        assertTrue(s.contains("if (videoTiming) lyricsOffsetMs else audioLyricsOffsetMs"))
        assertTrue(s.contains("if (videoTiming) setLyricsOffsetMs(value) else setAudioLyricsOffsetMs(value)"))
        assertTrue(s.contains("if (videoTiming) 0 else defaultAudioLyricsOffsetMs"))
    }
    @Test fun sheetDisplaysAndEditsTheSelectedModeWithMatchingReset() {
        val s = ui("LyricsOffsetSheet")
        assertTrue(s.contains("PlayerSettings.lyricOffsetFor(videoTiming)"))
        assertTrue(s.contains("PlayerSettings.setLyricOffsetFor(videoTiming, it)"))
        assertTrue(s.contains("setOffset(defaultOffsetMs)"))
        assertFalse(s.contains("PlayerSettings.setLyricsOffsetMs("))
    }
    @Test fun androidExposesItsAudioCalibrationExplicitly() {
        val s = app("ui/player/AndroidPlayerHost.kt")
        assertTrue(s.contains("override val audioLyricsOffsetMs get() = AppSettings.audioLyricsOffsetMs"))
        assertTrue(s.contains("AudioLyricsOffset.DEFAULT_MS"))
        assertTrue(s.contains("AppSettings.setAudioLyricsOffsetMs(value)"))
    }
}
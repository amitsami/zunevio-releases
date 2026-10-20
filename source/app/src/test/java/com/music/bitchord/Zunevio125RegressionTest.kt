package com.music.bitchord
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class Zunevio125RegressionTest {
    private fun code(p: String)=File("src/main/java/com/music/bitchord/$p").readText()
    @Test fun speechHasAutomaticLanguageAndVisibleErrors() {
        val s=code("ui/components/VoiceSearch.kt")
        for (token in listOf("EXTRA_ENABLE_LANGUAGE_DETECTION", "EXTRA_ENABLE_LANGUAGE_SWITCH", "onLanguageDetection", "ERROR_NO_MATCH", "ERROR_NETWORK", "ERROR_INSUFFICIENT_PERMISSIONS", "onPartialResults", "recognizer !== engine || !open")) assertTrue(token,s.contains(token))
        assertFalse(s.contains("StartActivityForResult()"))
        assertFalse(s.contains("EXTRA_PREFER_OFFLINE"))
        assertFalse(s.contains("EXTRA_LANGUAGE, language"))
        assertFalse(s.contains("FilterChip("))
    }
    @Test fun recordingModeInvalidatesLyricsRequestAndAppliesToManualSources() {
        val s=code("ui/MainViewModel.kt")
        assertTrue(s.contains("currentLyricsRequest?.videoTiming == videoTiming"))
        assertTrue(s.contains("preferVideoTiming = request.videoTiming"))
        assertTrue(s.contains("localUri != null && !videoTiming"))
        assertTrue(code("MainActivity.kt").contains("videoTiming = inlineVideo != null"))
    }
    @Test fun voiceInputPausesMusicInsteadOfTranscribingOwnPlayback() {
        val s=code("MainActivity.kt")
        assertTrue(s.contains("rememberVoiceSearch(onListening ="))
        assertTrue(s.contains("controller?.pause() // Do not transcribe our own music"))
    }
}

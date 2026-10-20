package com.music.bitchord
import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Architectural regressions only; real background/lock-screen behavior requires a phone. */
class Zunevio124RegressionTest {
    private fun source(path: String) = File("src/main/java/com/music/bitchord/$path").readText()
    @Test fun serviceNotCompositionOwnsDecoder() {
        val s = source("ui/player/InlineVideoService.kt")
        assertTrue(s.contains("MediaSessionService()"))
        assertTrue(s.contains("setWakeMode(C.WAKE_MODE_NETWORK)"))
        assertTrue(s.contains("MediaController.Builder(applicationContext"))
        assertTrue(s.contains("setSessionActivity(openIntent())"))
        assertTrue(s.contains("session.onTransportPlayIntent(play)"))
        assertFalse(source("ui/player/InlineMusicVideo.kt").contains("Lifecycle.Event.ON_STOP"))
        assertFalse(source("ui/player/InlineMusicVideo.kt").contains("onDispose"))
    }
    @Test fun screenClosureDoesNotDestroyQueue() {
        val s = source("playback/PlaybackService.kt")
        assertTrue(s.contains("InlineVideoPlayback.request.value != null"))
        assertTrue(s.contains("!AppSettings.stopOnTaskRemoved.value) return"))
        assertTrue(File("src/main/AndroidManifest.xml").readText().contains("android:stopWithTask=\"false\""))
    }
    @Test fun videoLyricsUseVideoEditAndRealClock() {
        val s = source("MainActivity.kt")
        assertTrue(s.contains("inlineVideo?.resolvedVideo ?: player.song"))
        assertTrue(source("ui/player/InlineMusicVideo.kt").contains("position.playbackSpeed = video.playbackParameters.speed"))
        assertTrue(source("playback/PlayerConnection.kt").contains("position.advancing = player.isPlaying"))
    }
    @Test fun voiceSearchIsOptInAndCanFailSafely() {
        val s = source("ui/components/VoiceSearch.kt")
        assertTrue(s.contains("RecognizerIntent.ACTION_RECOGNIZE_SPEECH"))
        assertTrue(s.contains("SpeechRecognizer.createSpeechRecognizer(context)"))
        assertTrue(s.contains("ActivityResultContracts.RequestPermission()"))
        assertTrue(s.contains("No background recording or humming recognition"))
    }
    @Test fun standaloneDiscoveryRowGetsPadding() {
        val s = source("ui/screens/SettingsSheet.kt")
        assertTrue(s.contains("checked = localRecommendations, standalone = true"))
        assertTrue(s.contains("top = if (standalone) 18.dp"))
    }
}

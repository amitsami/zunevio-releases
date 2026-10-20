package com.music.bitchord
import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Source architecture checks, not physical-device playback acceptance. */
class VideoPolish41RegressionTest {
    private fun source(path: String) = File("src/main/java/com/music/bitchord/$path").readText()
    @Test fun menuReflectsActiveVideoSession() {
        assertTrue(source("MainActivity.kt").contains("inlineVideoActive = musicVideoRequest?.videoId == song.videoId"))
        assertTrue(source("ui/components/SongActionsSheet.kt").contains("if (inlineVideoActive) R.string.back_to_audio"))
        assertTrue(source("MainActivity.kt").contains("setMusicVideoRequest(null) // Active video"))
    }
    @Test fun autoUsesActualTransferMeasurements() {
        val s = source("ui/player/InlineMusicVideo.kt") + source("ui/player/InlineVideoService.kt")
        assertTrue(source("data/settings/AppSettings.kt").contains("VideoQuality.AUTO"))
        assertTrue(s.contains(".setTransferListener(session.bandwidthMeter)"))
        assertTrue(s.contains("session.measuredBitrate = bitrate"))
        assertTrue(s.contains("AutoVideoPolicy.next("))
    }
    @Test fun seeksAreQueuedAndIdenticalStreamsNotRestarted() {
        val s = source("ui/player/InlineMusicVideo.kt")
        assertTrue(s.contains("pendingSeekMs = clamped"))
        assertTrue(s.contains("pendingFraction = fraction.coerceIn"))
        assertTrue(s.contains("else video.seekTo(target)"))
        assertTrue(s.contains("previousId -> handoverPosition"))
        assertTrue(s.contains("streamKey == session.activeStreamKey"))
    }
    @Test fun largerFrameAndMiniCoverCrop() {
        val s = source("ui/player/InlineMusicVideo.kt")
        val layout = File("../sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/player/NowPlayingScreen.kt").readText()
        assertTrue(s.contains("session.aspectRatio, 0f"))
        assertTrue(s.contains("resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM"))
        assertTrue(layout.contains("card.left - extra / 2f"))
    }
    @Test fun lyricsUnblockedByVideoAndFirstProviderResult() {
        assertTrue(source("MainActivity.kt").contains("inlineVideo?.resolvedVideo ?: player.song"))
        assertTrue(source("ui/MainViewModel.kt").contains("selectedLyricsSource == null && _lyrics.value.isNullOrEmpty()"))
    }
}

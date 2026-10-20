package com.music.bitchord

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Source contracts supplement pure policy tests; not rendered/device acceptance. */
class VideoQuality40RegressionTest {
    private fun source(path: String) = File("src/main/java/com/music/bitchord/$path").readText()
    @Test fun independentPreferencesPersistWithoutChangingAudioQuality() {
        val s = source("data/settings/AppSettings.kt")
        assertTrue(s.contains("putString(\"video_quality_wifi\", value.name)"))
        assertTrue(s.contains("putString(\"video_quality_cellular\", value.name)"))
        assertTrue(s.contains("prefs.getString(\"video_quality_wifi\", null)"))
        assertTrue(s.contains("NetworkCapabilities.TRANSPORT_WIFI"))
    }
    @Test fun dedicatedSettingsCategoryHasBothConnectionsAndThreeChoices() {
        val s = source("ui/screens/SettingsSheet.kt")
        assertTrue(s.contains("Triple(\"video_quality\""))
        assertTrue(s.contains("VideoQuality.entries.forEach"))
        assertTrue(s.contains("AppSettings.setVideoQualityWifi(quality)"))
        assertTrue(s.contains("AppSettings.setVideoQualityCellular(quality)"))
    }
    @Test fun playbackUsesNetworkAndSettingsChangesWithoutLosingPlayhead() {
        val s = source("ui/player/InlineMusicVideo.kt")
        val owner = source("ui/player/InlineVideoService.kt")
        assertTrue(owner.contains("VideoQuality.forNetwork(connection.wifi,"))
        assertTrue(owner.contains("session.load(song, quality, network)"))
        assertTrue(s.contains("previousId -> handoverPosition"))
        assertTrue(owner.contains("setTargetBufferBytes(32 * 1024 * 1024)"))
        assertTrue(s.contains("session.attempt != session.loadedAttempt"))
    }
    @Test fun videoIsCenteredAndBothAxesFeatherIntoBackground() {
        val s = source("ui/player/InlineMusicVideo.kt")
        val player = File("../sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/player/NowPlayingScreen.kt").readText()
        val layout = File("src/main/res/layout/inline_music_video.xml").readText()
        assertTrue(s.contains("contentAlignment = Alignment.Center"))
        assertTrue(s.contains("InlineVideoPolicy.fitFrame"))
        assertTrue(s.contains("drawRect(horizontal, blendMode = BlendMode.DstIn)"))
        assertTrue(s.contains("drawRect(vertical, blendMode = BlendMode.DstIn)"))
        assertTrue(s.contains("drawWithCache"))
        assertTrue(player.contains("val heroMode = musicVideoArtwork == null &&"))
        assertTrue(player.contains("clip = musicVideoArtwork == null"))
        assertTrue(layout.contains("@android:color/transparent"))
    }
    @Test fun videoSessionOutlivesCollapsiblePlayerSheet() {
        val s = source("MainActivity.kt")
        val owner = s.indexOf("val inlineVideo = retainedInlineVideo")
        val sheet = s.indexOf("val nowPlaying: @Composable")
        assertTrue(owner >= 0 && owner < sheet)
        assertTrue(s.contains("InlineVideoPlayback.session.collectAsStateWithLifecycle()"))
        val mini = s.substring(s.indexOf("                    GlassNavBar("), s.indexOf("                    FloatingBottomBar("))
        assertEquals(2, mini.split("isPlaying = inlineVideo?.isPlaying ?: player.isPlaying").size - 1)
        assertEquals(2, mini.split("toggleActivePlayback()").size - 1)
        assertEquals(2, mini.split("onNext = { leaveVideoForQueue").size - 1)
    }
    @Test fun videoChoiceIsExplicitAndClearedForNextTrack() {
        val s = source("MainActivity.kt")
        assertTrue(s.contains("mutableStateOf<Song?>(null)"))
        assertTrue(s.contains("musicVideoRequest?.takeIf { it.videoId == player.song?.videoId }"))
        assertTrue(s.contains("musicVideoRequest?.videoId != current"))
        val session = source("ui/player/InlineMusicVideo.kt")
        assertTrue(source("ui/player/InlineVideoService.kt").contains("item?.mediaId != session.originalId"))
        assertTrue(session.contains("onRelease = { session.unbindVideoView(it) }"))
    }
    @Test fun miniVideoUsesOnlyCoverSlotAndOneDecoder() {
        val main = source("MainActivity.kt")
        val session = source("ui/player/InlineMusicVideo.kt")
        assertEquals(2, main.split("InlineMusicVideoThumbnail(inlineVideo, coverModifier)").size - 1)
        for (component in listOf("MiniPlayer.kt", "GlassNavBar.kt")) {
            val bar = source("ui/components/$component")
            assertTrue(bar.contains("videoArtwork?.invoke(Modifier.fillMaxSize())"))
        }
        assertEquals(1, source("ui/player/InlineVideoService.kt").split("ExoPlayer.Builder(").size - 1)
        assertFalse(session.contains("ExoPlayer.Builder("))
        assertTrue(session.contains("PlayerView.switchTargetView(video, attachedView, target)"))
        assertTrue(session.contains("videoViews.entries.lastOrNull { it.value }"))
        assertTrue(session.contains("session.bindVideoView(this, expanded = false)"))
        assertTrue(session.contains("session.bindVideoView(this, expanded = true)"))
    }
}

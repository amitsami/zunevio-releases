package com.music.bitchord

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Offline contract checks. These do not claim rendered/device acceptance. */
class Zunevio123RegressionTest {
    private fun src(path: String) = File("src/main/java/com/music/bitchord/$path").readText()
    @Test fun videoLookupDoesNotStoreNullInConcurrentMap() {
        val repository = File("../shared/src/jvmSharedMain/kotlin/com/music/bitchord/data/YtMusicRepository.kt").readText()
        assertFalse(repository.contains("videoVersionCache[song.videoId] = null"))
        assertTrue(repository.contains("videoVersionCache.remove(song.videoId)"))
    }
    @Test fun searchAndPlaylistCopyUsesAppBrand() {
        val android = File("src/main/res/values/strings.xml").readText()
        val shared = File("../sharedUi/src/commonMain/composeResources/values/strings.xml").readText()
        for (text in listOf(android, shared)) {
            assertTrue(text.contains("Search millions of songs on Zunéviu."))
            assertTrue(text.contains("Saved to Zunéviu"))
            assertFalse(text.contains(">Sign in to Zunéviu<"))
        }
    }
    @Test fun videoIsRenderedAndNotJustAnAudioVersionSwap() {
        val screen = src("ui/player/InlineMusicVideo.kt")
        assertTrue(screen.contains("R.layout.inline_music_video"))
        assertTrue(screen.contains("StreamResolver.resolveVideoPlayback"))
        assertTrue(screen.contains("MergingMediaSource(visual, source(it))"))
        assertTrue(src("ui/player/InlineVideoService.kt").contains("queue.pause()"))
        assertTrue(screen.contains("video.release()"))
        assertTrue(screen.contains("currentMediaItem?.mediaId == originalId"))
        assertFalse(screen.contains("Lifecycle.Event.ON_STOP"))
        assertTrue(src("ui/player/InlineVideoService.kt").contains("MediaSessionService()"))
        assertFalse(screen.contains("WebView"))
    }
    @Test fun videoUsesExistingArtworkAndTransportWithoutDialog() {
        val screen = src("ui/player/InlineMusicVideo.kt")
        val main = src("MainActivity.kt")
        val player = File("../sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/player/NowPlayingScreen.kt").readText()
        val layout = File("src/main/res/layout/inline_music_video.xml").readText()
        assertFalse(screen.contains("import androidx.compose.ui.window.Dialog"))
        assertFalse(main.contains("MusicVideoDialog("))
        assertTrue(layout.contains("app:surface_type=\"texture_view\""))
        assertTrue(layout.contains("app:use_controller=\"false\""))
        assertTrue(player.contains("musicVideoArtwork?.invoke(Modifier.fillMaxSize())"))
        assertTrue(player.contains("Text(\"Back to audio\""))
        assertTrue(main.contains("inlineVideo?.position ?: player.position"))
        assertTrue(main.contains("inlineVideo?.durationMs ?: player.durationMs"))
        assertTrue(main.contains("inlineVideo.togglePlayPause()"))
        assertTrue(main.contains("inlineVideo.seekFraction(fraction)"))
        assertTrue(main.contains("inlineVideo.seek(target)"))
        assertTrue(player.contains("if (musicVideoArtwork == null) rememberCanvasArtwork(song) else null"))
    }
    @Test fun videoStopsBeforeQueueTransitionsAndReleasesOnce() {
        val screen = src("ui/player/InlineMusicVideo.kt")
        val main = src("MainActivity.kt")
        assertTrue(screen.contains("if (closed) return"))
        assertTrue(src("ui/player/InlineVideoService.kt").contains("queue.removeListener(audioListener)"))
        assertTrue(screen.contains("onPlaybackInterrupted()"))
        assertTrue(main.contains("inlineVideo?.close(resumeAudio = false)"))
        assertTrue(main.contains("showNowPlaying = true"))
    }
    @Test fun categoryIsAFirstClassRouteWithoutAsyncResetRace() {
        val main = src("MainActivity.kt")
        assertTrue(main.contains("showSettings -> \"settings:\${settingsCategory.orEmpty()}\""))
        assertTrue(main.contains("key.substringAfter(\"settings:\")"))
        assertTrue(main.contains("settingsCategorySwap"))
        assertFalse(main.contains("LaunchedEffect(showSettings) { if (!showSettings) settingsCategory = null }"))
    }
    @Test fun releaseNotesAndLegalHaveExplicitThemeColors() {
        val card = src("ui/components/UpdateSettingsCard.kt")
        assertTrue(card.contains("styled(info.notes.orEmpty()), color = colors.onSurface"))
        assertTrue(card.contains("Text(notices, color = MaterialTheme.colorScheme.onSurface"))
        assertTrue(src("ui/screens/SettingsGlass.kt").contains("LocalContentColor provides scheme.onBackground"))
    }
    @Test fun songSheetPaintExtendsBehindSafeNavigationInset() {
        assertTrue(src("MainActivity.kt").contains("contentWindowInsets = { WindowInsets(0, 0, 0, 0) }"))
        assertTrue(src("ui/components/SongActionsSheet.kt").contains("windowInsetsPadding(WindowInsets.navigationBars)"))
    }
}

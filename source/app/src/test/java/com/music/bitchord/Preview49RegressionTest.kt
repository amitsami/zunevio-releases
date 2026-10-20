package com.music.bitchord

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Static wiring contracts, NOT a substitute for WebView/device/account acceptance. */
class Preview49RegressionTest {
    private fun source(path: String) = File("src/main/java/com/music/bitchord/$path").readText()

    @Test fun missingCatalogueVideoNotifiesBeforeChangingPlayback() {
        val main = source("MainActivity.kt")
        val start = main.indexOf("val requestedFromId = controller?.currentMediaItem?.mediaId")
        assertTrue(start >= 0)
        val block = main.substring(start, main.indexOf("// The library toggle needs tokens", start))
        val resolve = block.indexOf("YtMusicRepository.resolveVideo(song)")
        val missing = block.indexOf("if (target == null)")
        val notify = block.indexOf("\"Video unavailable\"")
        val request = block.indexOf("setMusicVideoRequest(song)")
        assertTrue(resolve >= 0 && resolve < missing && missing < notify && notify < request)
        assertTrue(block.substring(missing, request).contains("return@launch"))
        assertTrue(block.contains("currentMediaItem?.mediaId != requestedFromId"))
        assertTrue(block.contains("catch (cancelled: kotlinx.coroutines.CancellationException)"))
    }

    @Test fun missingPlayableStreamNotifiesAndRestoresAudio() {
        val service = source("ui/player/InlineVideoService.kt")
        val start = service.indexOf("session.load(song, quality, network)")
        val block = service.substring(start, service.indexOf("delay(200L)", start))
        assertTrue(block.contains("session.error != null && openingVideo"))
        assertTrue(block.contains("\"Video unavailable\""))
        assertTrue(block.contains("session.close()"))
    }

    @Test fun videoLyricsEntryHasDisclosureWithoutChangingAudioLine() {
        val lyrics = File("../sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/player/PlayerLyrics.kt").readText()
        val start = lyrics.indexOf("if (labelOnly)")
        val block = lyrics.substring(start, lyrics.indexOf("} else if (lines.isNotEmpty())", start))
        assertTrue(block.contains("Res.string.open_lyrics"))
        assertTrue(block.contains("BitChordIcons.ChevronRight"))
        assertFalse(block.contains("CurrentLyricLine("))
        assertTrue(lyrics.substring(start).contains("CurrentLyricLine("))
    }

    @Test fun spotifyUsesDedicatedNativeLoginWithoutAnimatedWebView() {
        val screen = source("ui/screens/SpotifyLibraryScreen.kt")
        val login = source("auth/SpotifyLoginActivity.kt")
        assertTrue(screen.contains("SpotifyLoginActivity::class.java"))
        assertFalse(screen.contains("AndroidView("))
        assertFalse(login.contains("LOGIN_USER_AGENT"))
        assertFalse(login.contains("LOGIN_LAYOUT_FIX"))
        assertFalse(login.contains("evaluateJavascript("))
        assertTrue(login.contains("if (!request.isForMainFrame) return false"))
        assertTrue(login.contains("SpotifyLoginPolicy.allowed(request.url.toString())"))
        assertTrue(login.contains("settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW"))
        assertTrue(login.contains("WebSettings.LOAD_DEFAULT"))
        assertTrue(login.contains("webChromeClient = object : WebChromeClient()"))
    }

    @Test fun spotifyProgressAndCookiePollingNeverReloadTheDocument() {
        val login = source("auth/SpotifyLoginActivity.kt")
        val poll = login.substring(login.indexOf("private val poll"), login.indexOf("private val slowPage"))
        assertFalse(poll.contains("loadUrl("))
        assertFalse(poll.contains("reload("))
        val progress = login.substring(login.indexOf("override fun onProgressChanged"), login.indexOf("webViewClient ="))
        assertFalse(progress.contains("loadUrl("))
        assertFalse(progress.contains("reload("))
        assertFalse(progress.contains("View.GONE"))
        assertTrue(login.contains("if (restored == null) web.loadUrl(LOGIN_URL)"))
        assertTrue(login.contains("SpotifyLoginPolicy.completedLanding(web.url)"))
        assertTrue(login.contains("override fun onReceivedError"))
        assertTrue(login.contains("override fun onReceivedHttpError"))
        assertTrue(login.contains("web.clearCache(true)"))
        assertFalse(login.contains("handler.proceed()"))
    }
}

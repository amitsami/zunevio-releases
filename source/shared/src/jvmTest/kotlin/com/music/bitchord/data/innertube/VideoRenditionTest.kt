package com.music.bitchord.data.innertube

import kotlin.test.*

class VideoRenditionTest {
    private fun v(height: Int, muxed: Boolean = true, mime: String = "video/mp4", url: String = "https://media.example/$height") = VideoRendition(url, height, muxed, mime)
    @Test fun completeAudioVideoPreferredToSilentHighResolution() {
        assertEquals(360, chooseVideoRendition(listOf(v(1080, false), v(360)))?.height)
    }
    @Test fun chooses720Not4kForSmoothDefault() {
        assertEquals(720, chooseVideoRendition(listOf(v(2160), v(360), v(720)))?.height)
    }
    @Test fun adaptiveVideoWorksWhenNoMuxedStreamExists() {
        assertEquals(v(720, false), chooseVideoRendition(listOf(v(1080, false), v(720, false))))
    }
    @Test fun choosesLowestWhenAllStreamsExceedCap() {
        assertEquals(1080, chooseVideoRendition(listOf(v(2160), v(1080)))?.height)
    }
    @Test fun rejectsBlankUnencryptedAndUnknownResolution() {
        assertNull(chooseVideoRendition(listOf(v(720, url = ""), v(720, url = "http://media"), v(0))))
    }
    @Test fun mp4PreferredAtEqualResolution() {
        assertEquals("video/mp4", chooseVideoRendition(listOf(v(720, mime = "video/webm"), v(720)))?.mimeType)
    }
}

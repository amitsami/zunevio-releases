package com.music.bitchord.data.innertube

import kotlin.test.*

class VideoQualityTest {
    private fun v(height: Int, muxed: Boolean = true) = VideoRendition("https://media/$height/$muxed", height, muxed, "video/mp4")
    private val levels = listOf(v(144), v(360), v(720, false), v(1080, false), v(2160, false))
    @Test fun bothNetworkDefaultsAreAuto() {
        assertEquals(VideoQuality.AUTO, VideoQuality.forNetwork(true))
        assertEquals(VideoQuality.AUTO, VideoQuality.forNetwork(false))
    }
    @Test fun wifiAndMobileChoicesAreIndependent() {
        assertEquals(VideoQuality.LOW, VideoQuality.forNetwork(true, VideoQuality.LOW, VideoQuality.HIGH))
        assertEquals(VideoQuality.HIGH, VideoQuality.forNetwork(false, VideoQuality.LOW, VideoQuality.HIGH))
    }
    @Test fun allThreeRungsHaveDistinctActualStreamCeilings() {
        assertEquals(360, chooseVideoRendition(levels, VideoQuality.LOW.maxHeight)?.height)
        assertEquals(720, chooseVideoRendition(levels, VideoQuality.MEDIUM.maxHeight)?.height)
        assertEquals(1080, chooseVideoRendition(levels, VideoQuality.HIGH.maxHeight)?.height)
    }
    @Test fun muxed360DoesNotSilentlyDefeatHigh1080() {
        assertEquals(v(1080, false), chooseVideoRendition(listOf(v(360), v(1080, false)), 1080))
    }
    @Test fun sameResolutionPrefersCompleteAv() {
        assertEquals(v(720), chooseVideoRendition(listOf(v(720, false), v(720)), 720))
    }
    @Test fun unavailableRungChoosesSupportedLower() {
        assertEquals(480, chooseVideoRendition(listOf(v(240), v(480), v(1080)), 720)?.height)
    }
    @Test fun invalidStoredValuesGetPerNetworkDefaults() {
        assertEquals(VideoQuality.HIGH, VideoQuality.read("broken", VideoQuality.HIGH))
        assertEquals(VideoQuality.LOW, VideoQuality.read(null, VideoQuality.LOW))
        assertEquals(VideoQuality.MEDIUM, VideoQuality.read("MEDIUM", VideoQuality.HIGH))
    }
    @Test fun hardwareFriendlyH264WinsAtEqualResolution() {
        val av1 = v(1080, false).copy(codec = "av01", fps = 30)
        val avc = v(1080, false).copy(codec = "avc1", fps = 30)
        assertEquals(avc, chooseVideoRendition(listOf(av1, avc), 1080))
    }
    @Test fun avoidsExtra60FpsDecodeCostWhenSameResolutionExists() {
        val thirty = v(1080, false).copy(codec = "avc1", fps = 30)
        assertEquals(thirty, chooseVideoRendition(listOf(thirty.copy(fps = 60), thirty), 1080))
    }
    @Test fun mobileAudioBudgetIsLowerToo() { assertTrue(VideoQuality.LOW.audioKbps < VideoQuality.HIGH.audioKbps) }
    @Test fun autoStartsConservativelyForEachConnection() {
        assertEquals(VideoQuality.MEDIUM, AutoVideoPolicy.initial(true))
        assertEquals(VideoQuality.LOW, AutoVideoPolicy.initial(false))
    }
    @Test fun autoUsesActualSpeed() {
        assertEquals(VideoQuality.LOW, AutoVideoPolicy.tier(800_000))
        assertEquals(VideoQuality.MEDIUM, AutoVideoPolicy.tier(2_000_000))
        assertEquals(VideoQuality.HIGH, AutoVideoPolicy.tier(8_000_000))
    }
    @Test fun autoDowngradesWithoutRapidReloads() {
        assertEquals(VideoQuality.MEDIUM, AutoVideoPolicy.next(VideoQuality.HIGH, 8_000_000, 0, 3_000, 20_000))
        assertEquals(VideoQuality.MEDIUM, AutoVideoPolicy.next(VideoQuality.HIGH, 8_000_000, 0, 3_000, 5_000))
    }
    @Test fun autoUpgradesOnlyWithStableBuffer() {
        assertEquals(VideoQuality.LOW, AutoVideoPolicy.next(VideoQuality.LOW, 8_000_000, 9_000, 0, 20_000))
        assertEquals(VideoQuality.HIGH, AutoVideoPolicy.next(VideoQuality.LOW, 8_000_000, 9_000, 0, 50_000))
        assertEquals(VideoQuality.LOW, AutoVideoPolicy.next(VideoQuality.LOW, 8_000_000, 1_000, 0, 50_000))
    }
    @Test fun autoRequiresMeasurement() {
        assertEquals(VideoQuality.LOW, AutoVideoPolicy.next(VideoQuality.LOW, 0, 20_000, 0, 60_000))
    }
    @Test fun autoKeepsBufferHeadroom() {
        assertEquals(VideoQuality.LOW, AutoVideoPolicy.next(VideoQuality.HIGH, 400_000, 1_000, 0, 20_000))
        assertEquals(VideoQuality.HIGH, AutoVideoPolicy.next(VideoQuality.HIGH, 400_000, 20_000, 0, 20_000))
    }
}

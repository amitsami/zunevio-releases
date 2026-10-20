package com.music.bitchord.data

import com.music.bitchord.data.model.Song
import kotlin.test.*

class LocalDiscoveryPolicyTest {
    private fun song(id: String, title: String = id) = Song(id, title, "Artist", null)
    private fun seed(id: String) = TasteSeed(id, id, "Artist")
    @Test fun oneListenCanSeedDiscovery() { assertNotNull(LocalDiscoveryPolicy.seed(song("ABCDEFGHIJK"))) }
    @Test fun localPathsAreNeverSentAsRadioSeeds() { assertNull(LocalDiscoveryPolicy.seed(song("content://private/song"))) }
    @Test fun mostRecentSeedWinsWithoutDuplicates() {
        assertEquals(listOf("B","A"),LocalDiscoveryPolicy.remember(listOf(seed("A"),seed("B")),seed("B")).map { it.id })
    }
    @Test fun historyIsBoundedToEight() {
        assertEquals(8,LocalDiscoveryPolicy.remember((1..20).map { seed("$it") },seed("new")).size)
    }
    @Test fun recommendationsInterleaveTwoSeedsAndNeverRepeatHeardSongs() {
        val result=LocalDiscoveryPolicy.candidates(listOf(seed("A")),listOf(listOf(song("A"),song("B"),song("C")),listOf(song("D"),song("B"))))
        assertEquals(listOf("D","B","C"),result.map { it.videoId })
    }
    @Test fun emptyProvidersDoNotInventRecommendations() { assertTrue(LocalDiscoveryPolicy.candidates(listOf(seed("A")),listOf(emptyList())).isEmpty()) }
    @Test fun recommendationsAreBounded() { assertEquals(20,LocalDiscoveryPolicy.candidates(emptyList(),listOf((1..100).map { song("$it") })).size) }
    @Test fun twentySecondsAreRequiredNotOneTap() { assertEquals(20_000L,LocalDiscoveryPolicy.QUALIFY_MS) }
}
class RenderPolicyTest {
    @Test fun android10HasGlassStyleNotApi31Blur() { assertTrue(RenderPolicy.glassStyle(29));assertFalse(RenderPolicy.backdropBlur(29,false,false)) }
    @Test fun android11HasGlassStyleNotApi31Blur() { assertTrue(RenderPolicy.glassStyle(30));assertFalse(RenderPolicy.backdropBlur(30,false,false)) }
    @Test fun oldAndroidKeepsFlatFallback() { assertFalse(RenderPolicy.glassStyle(28)) }
    @Test fun modernAndroidCanUseGpuBlur() { assertTrue(RenderPolicy.backdropBlur(31,false,false)) }
    @Test fun lowRamAvoidsBackdropCapture() { assertFalse(RenderPolicy.backdropBlur(35,true,false)) }
    @Test fun reduceBlurWins() { assertFalse(RenderPolicy.backdropBlur(35,false,true)) }
    @Test fun directionalTabsAreSymmetric() { assertEquals(1,RenderPolicy.tabDirection(0,2));assertEquals(-1,RenderPolicy.tabDirection(2,0));assertEquals(0,RenderPolicy.tabDirection(1,1)) }
}

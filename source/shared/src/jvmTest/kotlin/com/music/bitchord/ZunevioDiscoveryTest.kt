package com.music.bitchord

import com.music.bitchord.data.ZunevioDiscovery
import org.junit.Assert.*
import org.junit.Test

class ZunevioDiscoveryTest {
    @Test fun localExploreHasSixteenUniqueNavigableTopics() {
        val items = ZunevioDiscovery.sections().flatMap { it.items }
        assertEquals(16, items.size)
        assertEquals(16, items.map { it.browseId }.toSet().size)
        items.forEach {
            assertNotNull(ZunevioDiscovery.topicTitle(it.browseId))
            assertTrue(ZunevioDiscovery.allowedDiscoveryLabel(it.title))
        }
    }
    @Test fun everyTopicHasAnOriginalLocalBannerKey() {
        val items = ZunevioDiscovery.sections().flatMap { it.items }
        assertEquals(16, items.map { ZunevioDiscovery.artworkKey(it.browseId) }.toSet().size)
    }
    @Test fun excludesRequestedIndianLanguageAndGenreCategories() {
        listOf("Bollywood hits", "Indian pop", "Hindi", "Punjabi", "Tamil", "Telugu hits", "Desi dance", "Bhangra")
            .forEach { assertFalse(it, ZunevioDiscovery.allowedDiscoveryLabel(it)) }
    }
    @Test fun doesNotConfuseCountryTagsWithUnrelatedEnglishWords() {
        listOf("English pop", "Focus", "Independent rock", "Jazz", "Classical")
            .forEach { assertTrue(it, ZunevioDiscovery.allowedDiscoveryLabel(it)) }
    }
    @Test fun unknownTopicDoesNotInventABrowseDestination() {
        assertNull(ZunevioDiscovery.topicTitle("zunevio:mood:unknown"))
    }
}

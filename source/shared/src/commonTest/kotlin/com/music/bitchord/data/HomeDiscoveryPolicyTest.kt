package com.music.bitchord.data

import com.music.bitchord.data.model.HomeShelf
import com.music.bitchord.data.model.ShelfItem
import com.music.bitchord.data.model.Song
import kotlin.test.*

class HomeDiscoveryPolicyTest {
    private fun item(id: String) = ShelfItem(id, "Artist", null, id, null)
    @Test fun discoveryIsSongFirstAndNotKeywordPlaylistSpam() {
        assertTrue(HomeDiscoveryPolicy.sections.none { it.playlists })
        assertFalse(HomeDiscoveryPolicy.qualityLabel("Top 100 hits full playlist"))
        assertFalse(HomeDiscoveryPolicy.qualityLabel("2 hours non-stop music"))
        assertTrue(HomeDiscoveryPolicy.qualityLabel("Night Changes"))
    }
    @Test fun twelveDistinctDiscoveryQueries() {
        assertEquals(12, HomeDiscoveryPolicy.sections.size)
        assertEquals(12, HomeDiscoveryPolicy.sections.map { it.key }.toSet().size)
        assertEquals(12, HomeDiscoveryPolicy.sections.map { it.query }.toSet().size)
    }
    @Test fun fourInitialSectionsAndEightPagedSections() {
        val found = HomeDiscoveryPolicy.sections.take(HomeDiscoveryPolicy.INITIAL_COUNT).toMutableList()
        var token = HomeDiscoveryPolicy.continuation(HomeDiscoveryPolicy.INITIAL_COUNT)
        while (token != null) {
            val offset = HomeDiscoveryPolicy.pageOffset(token)!!
            val page = HomeDiscoveryPolicy.page(offset)
            assertTrue(page.size in 1..3)
            found.addAll(page)
            token = HomeDiscoveryPolicy.continuation(offset + page.size)
        }
        assertEquals(HomeDiscoveryPolicy.sections, found)
    }
    @Test fun invalidCursorsCannotSelectArbitraryRequests() {
        for (token in listOf("anything4", "4", "zunevio:home:page:-1", "zunevio:home:page:0", "zunevio:home:page:999", "zunevio:home:page:x"))
            assertNull(HomeDiscoveryPolicy.pageOffset(token))
        assertTrue(HomeDiscoveryPolicy.page(-1).isEmpty())
    }
    @Test fun virtualSupplementIdsResolveKnownSectionsOnly() {
        HomeDiscoveryPolicy.sections.forEach { assertEquals(it, HomeDiscoveryPolicy.section(HomeDiscoveryPolicy.PREFIX + it.key)) }
        assertNull(HomeDiscoveryPolicy.section("zunevio:home:unknown"))
    }
    @Test fun discoveryKeepsExistingCategoryPolicy() {
        HomeDiscoveryPolicy.sections.forEach { assertTrue(ZunevioDiscovery.allowedDiscoveryLabel(it.title + " " + it.query)) }
    }
    @Test fun duplicateItemsDoNotBecomeFakeDifferentSections() {
        val shelves = HomeDiscoveryPolicy.uniqueShelves(listOf(HomeShelf("A", listOf(item("1"),item("2"))),HomeShelf("B", listOf(item("1"),item("3")))))
        assertEquals(listOf("1","2","3"), shelves.flatMap { it.items }.map { it.videoId })
    }
    @Test fun emptyAndDuplicateOnlyRowsAreOmitted() {
        assertEquals(1, HomeDiscoveryPolicy.uniqueShelves(listOf(HomeShelf("A",listOf(item("1"))),HomeShelf("B",listOf(item("1"))),HomeShelf("C",emptyList()))).size)
    }
    @Test fun unrenderedOverflowRemainsAvailableForAnotherSection() {
        val shelves = HomeDiscoveryPolicy.uniqueShelves(listOf(HomeShelf("A", (1..30).map { item("$it") }), HomeShelf("B",listOf(item("25")))))
        assertEquals(18,shelves[0].items.size)
        assertEquals("25",shelves[1].items.single().videoId)
    }
    @Test fun historyIsNotTruncatedOrRemovedByRecommendations() {
        val shelves = HomeDiscoveryPolicy.uniqueShelves(listOf(HomeShelf("Mix",listOf(item("1"))),HomeShelf("Recents",(1..30).map { item("$it") })))
        assertEquals(30,shelves[1].items.size)
    }
    @Test fun invalidUnplayableCardsAreOmitted() {
        assertTrue(HomeDiscoveryPolicy.uniqueShelves(listOf(HomeShelf("A",listOf(ShelfItem("No endpoint","",null,null,null))))).isEmpty())
    }
}

class PersonalDiscoveryPolicyTest {
    private fun seed(i: Int, artist: String = "Artist $i") = TasteSeed("seed$i", "Track $i", artist)
    private fun song(id: String) = Song(id,id,"Artist",null)
    @Test fun mixGivesOtherArtistsSpaceBeforeFilling() {
        val tracks = (1..10).map { song("a$it").copy(artist = "A") } +
            (1..5).map { song("b$it").copy(artist = "B") }
        val mixed = PersonalDiscoveryPolicy.diverseMix(tracks)
        assertEquals(listOf("A", "A", "A", "B", "B", "B"), mixed.take(6).map { it.artist })
        assertEquals(15, mixed.size)
    }
    @Test fun personalMixDropsCompilationsAndDuplicateRecordings() {
        val tracks = listOf(song("a").copy(title = "Real song"), song("b").copy(title = "Real song"),
            song("c").copy(title = "Top 100 non-stop hits"))
        val shelves = PersonalDiscoveryPolicy.shelves(listOf(seed(1)), listOf(tracks))
        assertEquals(listOf("a"), shelves.flatMap { it.items }.map { it.videoId })
    }
    @Test fun personalArtistRanksAheadOfUnrelatedRadioFiller() {
        val tracks = listOf(song("a").copy(artist = "Other"), song("b").copy(artist = "Artist 1"))
        assertEquals("b", PersonalDiscoveryPolicy.shelves(listOf(seed(1)), listOf(tracks)).first().items.first().videoId)
    }
    @Test fun fourDistinctArtistsAtMost() {
        val seeds = listOf(seed(1,"Same"),seed(2," same "))+(3..10).map { seed(it) }
        assertEquals(listOf("seed1","seed3","seed4","seed5"),PersonalDiscoveryPolicy.requestSeeds(seeds).map { it.id })
    }
    @Test fun richResultsCreateFiveGenuinelyDifferentRows() {
        val shelves = PersonalDiscoveryPolicy.shelves((1..4).map { seed(it) }, (1..4).map { i -> (1..30).map { song("$i-$it") } })
        assertEquals(5,shelves.size)
        assertEquals("Made for you",shelves.first().title)
        assertEquals(12,shelves.first().items.size)
        assertTrue(shelves.drop(1).all { it.items.size==10 })
        val ids=shelves.flatMap { it.items }.map { it.videoId }
        assertEquals(ids.size,ids.toSet().size)
    }
    @Test fun heardTracksAreNeverRediscoveredAsNew() {
        val shelves = PersonalDiscoveryPolicy.shelves(listOf(seed(1)),listOf(listOf(song("seed1"),song("new"))))
        assertEquals(listOf("new"),shelves.flatMap { it.items }.map { it.videoId })
    }
    @Test fun emptyProvidersDoNotInventRows() {
        assertTrue(PersonalDiscoveryPolicy.shelves(listOf(seed(1)),listOf(emptyList())).isEmpty())
    }
    @Test fun repeatedRadioResponsesAreNotRelabelledToFillRows() {
        val radio=(1..10).map { song("$it") }
        assertEquals(1,PersonalDiscoveryPolicy.shelves(listOf(seed(1),seed(2)),listOf(radio,radio)).size)
    }
    @Test fun offAndResetCanIdentifyEveryLocalPersonalRow() {
        assertTrue(PersonalDiscoveryPolicy.isPersonalTitle("Made for you"))
        assertTrue(PersonalDiscoveryPolicy.isPersonalTitle("More like · Track"))
        assertFalse(PersonalDiscoveryPolicy.isPersonalTitle("Chill & unwind"))
    }
}

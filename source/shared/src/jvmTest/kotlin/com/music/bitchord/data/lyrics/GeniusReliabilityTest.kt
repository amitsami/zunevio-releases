package com.music.bitchord.data.lyrics

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlin.test.*

class GeniusReliabilityTest {
    private val page = """<div data-lyrics-container="true">[Verse]<br>Fixture first line<br>Fixture second line</div>"""
    private val song = """{"title":"Bohemian Rhapsody","artist_names":"Queen","url":"https://genius.com/Queen-bohemian-rhapsody-lyrics"}"""
    private val search = """{"response":{"sections":[{"type":"song","hits":[{"result":$song}]}]}}"""

    @Test fun blockedResponsesAreNotMissingLyrics() {
        for (code in listOf(401, 403, 429, 500, 503)) {
            val error = assertFailsWith<LyricsProviderUnavailableException> { Genius.responseBody(code, "blocked") }
            assertEquals(code, error.httpStatus)
        }
        assertNull(Genius.responseBody(404, "not found"))
        assertEquals(search, Genius.responseBody(200, search))
    }
    @Test fun htmlChallengeAndMalformedSearchNeverBecomeCatalogueMisses() {
        assertFailsWith<LyricsProviderUnavailableException> { Genius.responseBody(200, "challenge", true) }
        assertFailsWith<LyricsProviderUnavailableException> { Genius.parseSearchResponse("<html>challenge</html>", "Song", "Artist") }
        assertFailsWith<LyricsProviderUnavailableException> { Genius.parseSearchResponse("{}", "Song", "Artist") }
    }
    @Test fun supportsBothDocumentedSearchShapesAndGenuineEmptyResults() {
        assertEquals("https://genius.com/Queen-bohemian-rhapsody-lyrics", Genius.parseSearchResponse(search, "Bohemian Rhapsody", "Queen"))
        val flat = """{"response":{"hits":[{"result":$song}]}}"""
        assertEquals("https://genius.com/Queen-bohemian-rhapsody-lyrics", Genius.parseSearchResponse(flat, "Bohemian Rhapsody", "Queen"))
        assertNull(Genius.parseSearchResponse("""{"response":{"sections":[]}}""", "Missing", "Artist"))
    }
    @Test fun noisyTitleIsCleanedBeforeSearchingAndRealPageIsParsed() = runBlocking<Unit> {
        val requested = mutableListOf<String>()
        val lines = Genius.fetchLyrics("Bohemian Rhapsody (Official Video)", "Queen") { url ->
            requested += url
            if (url.contains("/api/search/")) search else page
        }
        assertEquals(2, requested.size)
        assertFalse(requested.first().contains("Official"))
        assertEquals(listOf("[Verse]", "Fixture first line", "Fixture second line"), lines!!.map { it.text })
    }
    @Test fun blockStopsAllRepeatedSpellingsAfterOneRequest() = runBlocking<Unit> {
        var calls = 0
        assertFailsWith<LyricsProviderUnavailableException> {
            Genius.fetchLyrics("Queen - Bohemian Rhapsody (Official Video)", "Queen") {
                calls++
                Genius.responseBody(403, "challenge")
            }
        }
        assertEquals(1, calls)
    }
    @Test fun cancellationIsNeverSwallowedAsMissingLyrics() = runBlocking<Unit> {
        assertFailsWith<CancellationException> {
            Genius.fetchLyrics("Song", "Artist") { throw CancellationException("track changed") }
        }
    }
    @Test fun neverFetchesUntrustedSongUrlFromSearchResult() {
        val unsafe = search.replace("https://genius.com/Queen-bohemian-rhapsody-lyrics", "https://example.com/Queen-bohemian-rhapsody-lyrics")
        assertNull(Genius.parseSearchResponse(unsafe, "Bohemian Rhapsody", "Queen"))
    }
    @Test fun knownSongWithUnrecognizedPageIsUnavailableNotMissing() = runBlocking<Unit> {
        assertFailsWith<LyricsProviderUnavailableException> {
            Genius.fetchLyrics("Bohemian Rhapsody", "Queen") { if (it.contains("/api/search/")) search else "<html>unexpected</html>" }
        }
    }
}

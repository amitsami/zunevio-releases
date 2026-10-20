package com.music.bitchord.data.innertube

import com.music.bitchord.data.model.Song
import com.music.bitchord.data.sources.TrackMatcher
import kotlin.test.*

class VideoMatchTest {
    private val target = TrackMatcher.Target("River", "Aster", 180)
    private fun song(title: String = "River", artist: String = "Aster", length: String = "5:20") =
        Song(videoId = "abcdefghijk", title = title, artist = artist, thumbnailUrl = null, durationText = length, isVideo = true)
    @Test fun officialFilmIntroDoesNotRejectTheVisualCounterpart() {
        val video = song()
        assertEquals(video, TrackMatcher.bestOfficialVideoForAudio(listOf(video), target))
    }
    @Test fun unrelatedArtistIsNeverSubstitutedAsVideo() {
        assertNull(TrackMatcher.bestOfficialVideoForAudio(listOf(song(artist = "Someone Else")), target))
    }
    @Test fun remixIsNotTheOriginalMusicVideo() {
        assertNull(TrackMatcher.bestOfficialVideoForAudio(listOf(song(title = "River (Remix)")), target))
    }
}

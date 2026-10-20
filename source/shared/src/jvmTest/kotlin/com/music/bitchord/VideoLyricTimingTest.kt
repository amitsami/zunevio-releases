package com.music.bitchord

import com.music.bitchord.data.lyrics.*
import org.junit.Assert.*
import org.junit.Test

class VideoLyricTimingTest {
    private val lines = listOf(
        LyricLine(15_000L, "first", words = listOf(LyricWord(15_000L, 16_000L, "first"))),
        LyricLine(23_000L, "second"),
    )

    @Test fun audioRetainsOriginalTimings() {
        for (source in LyricsSource.entries) {
            assertSame(lines, LyricsRepository.videoTimingLines(source, lines, false))
        }
    }
    @Test fun captionsRetainTheirActualVideoTimestamps() {
        assertSame(lines, LyricsRepository.videoTimingLines(LyricsSource.YOUTUBE_TRANSCRIPT, lines, true))
    }
    @Test fun videoFallbackIsReadableButNeverPretendsToBeSynchronized() {
        for (source in LyricsSource.entries.filter { it != LyricsSource.YOUTUBE_TRANSCRIPT }) {
            val result = LyricsRepository.videoTimingLines(source, lines + LyricLine(25_000L, ""), true)
            assertEquals(listOf("first", "second"), result.map { it.text })
            assertTrue(result.all { it.timeMs == 0L && it.words.isEmpty() })
            assertEquals(15_000L, lines[0].timeMs) // No mutation of cached audio lyrics.
        }
    }
    @Test fun videoCaptionsArePreferredOverAlbumFallback() {
        val sequence = LyricsRepository.sourceSequence(
            setOf(LyricsSource.LRCLIB, LyricsSource.YOUTUBE_TRANSCRIPT),
            listOf(LyricsSource.LRCLIB), true,
        )
        assertEquals(listOf(LyricsSource.YOUTUBE_TRANSCRIPT, LyricsSource.LRCLIB), sequence)
    }
    @Test fun audioKeepsUserSourcePriority() {
        assertEquals(
            listOf(LyricsSource.LRCLIB, LyricsSource.YOUTUBE_TRANSCRIPT),
            LyricsRepository.sourceSequence(
                setOf(LyricsSource.LRCLIB, LyricsSource.YOUTUBE_TRANSCRIPT),
                listOf(LyricsSource.LRCLIB, LyricsSource.YOUTUBE_TRANSCRIPT), false,
            ),
        )
    }
    @Test fun disabledAndHiddenSourcesStayDisabledInVideoMode() {
        assertEquals(
            listOf(LyricsSource.LRCLIB),
            LyricsRepository.sourceSequence(
                setOf(LyricsSource.LRCLIB, LyricsSource.SIMP_MUSIC),
                LyricsSource.entries, true,
            ),
        )
        assertTrue(LyricsRepository.sourceSequence(emptySet(), LyricsSource.entries, true).isEmpty())
    }
    @Test fun switchingToVideoDoesNotMutateCachedAudioCues() {
        LyricsRepository.videoTimingLines(LyricsSource.LRCLIB, lines, true)
        assertSame(lines, LyricsRepository.videoTimingLines(LyricsSource.LRCLIB, lines, false))
        assertEquals(15_000L, lines[0].timeMs)
    }
}

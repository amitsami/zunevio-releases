package com.music.bitchord.ui.player

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class LyricsOperationTest {
    @Test fun providerExceptionShowsUnavailableRatherThanCrashingUi() = runBlocking {
        val result = lyricsOperationOrDefault<LyricsTranslationResult>(LyricsTranslationResult.Unavailable) {
            throw IllegalArgumentException("invalid header")
        }
        assertSame(LyricsTranslationResult.Unavailable, result)
    }
    @Test fun romanizationExceptionAlsoShowsUnavailable() = runBlocking {
        val result = lyricsOperationOrDefault<LyricsRomanizationResult>(LyricsRomanizationResult.Unavailable) {
            throw IllegalStateException("provider failed")
        }
        assertSame(LyricsRomanizationResult.Unavailable, result)
    }
    @Test fun changingTracksStillCancelsTheOldOperation() {
        assertThrows(CancellationException::class.java) {
            runBlocking { lyricsOperationOrDefault("unavailable") { throw CancellationException("changed") } }
        }
    }
    @Test fun successfulResultIsPreserved() = runBlocking {
        assertEquals("translation", lyricsOperationOrDefault("unavailable") { "translation" })
    }
    @Test fun fatalErrorIsNotHidden() {
        assertThrows(AssertionError::class.java) {
            runBlocking { lyricsOperationOrDefault("unavailable") { throw AssertionError("fatal") } }
        }
    }
}

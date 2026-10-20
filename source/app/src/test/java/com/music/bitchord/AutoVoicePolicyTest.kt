package com.music.bitchord

import com.music.bitchord.ui.components.AutoVoicePolicy as P
import org.junit.Assert.*
import org.junit.Test

/** Pure policy tests, not microphone or speech-provider integration tests. */
class AutoVoicePolicyTest {
    @Test fun olderAndroidIsNotPretendedToSupportAutoSwitch() {
        for (api in listOf(26, 28, 30, 31, 32, 33)) assertFalse(P.supportsApi(api))
    }
    @Test fun modernAndroidMayRequestAutoDetection() { for (api in 34..37) assertTrue(P.supportsApi(api)) }
    @Test fun blankResultsNeverSubmit() { assertNull(P.best(listOf("", "  "), null)) }
    @Test fun highestValidConfidenceWins() {
        assertEquals("best", P.best(listOf("first", "best", "last"), floatArrayOf(0.2f, 0.9f, 0.3f))?.text)
    }
    @Test fun blankAlternativesDoNotShiftConfidenceIndices() {
        assertEquals("right", P.best(listOf("", "wrong", "right"), floatArrayOf(1f, 0.1f, 0.8f))?.text)
    }
    @Test fun unavailableScoresKeepProviderOrder() {
        assertEquals("first", P.best(listOf(" first ", "second"), floatArrayOf(-1f, Float.NaN))?.text)
    }
    @Test fun shortConfidenceArrayIsSafe() {
        assertEquals("first", P.best(listOf("first", "second"), floatArrayOf(0.9f))?.text)
    }
    @Test fun noFixedAlphabetOrThreeLanguageRestriction() {
        for (s in listOf("আমার গান", "मेरे गाने", "أغنية", "노래", "歌曲", "canción", "песня"))
            assertEquals(s, P.best(listOf(s), null)?.text)
    }
    @Test fun missingDetectionRequiresReviewEvenAtHighConfidence() {
        assertTrue(P.needsReview(P.Candidate("words", 1f), false))
    }
    @Test fun lowConfidenceNeedsReview() { assertTrue(P.needsReview(P.Candidate("words", 0.54f), true)) }
    @Test fun verifiedLanguageAndAdequateScoreMaySubmit() {
        assertFalse(P.needsReview(P.Candidate("words", 0.55f), true))
        assertFalse(P.needsReview(P.Candidate("words", null), true))
    }
    @Test fun invalidConfidenceDoesNotBecomeFalseCertainty() {
        val c=P.best(listOf("words"), floatArrayOf(20f))!!
        assertNull(c.confidence)
        assertTrue(P.needsReview(c, false))
    }
}

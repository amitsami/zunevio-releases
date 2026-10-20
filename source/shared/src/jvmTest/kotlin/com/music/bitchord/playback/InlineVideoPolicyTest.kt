package com.music.bitchord.playback

import kotlin.test.*

class InlineVideoPolicyTest {
    @Test fun seekUsesVideoDurationNotPausedAudioDuration() {
        assertEquals(90_000L, InlineVideoPolicy.fractionTarget(0.5f, 180_000L))
    }
    @Test fun seekGuardsEndToAvoidAccidentalTrackAdvance() {
        assertEquals(179_750L, InlineVideoPolicy.seekTarget(Long.MAX_VALUE, 180_000L))
    }
    @Test fun negativeSeekClampedToStart() { assertEquals(0L, InlineVideoPolicy.seekTarget(-900L, 1000L)) }
    @Test fun unknownDurationCannotBeSought() {
        assertNull(InlineVideoPolicy.seekTarget(100L, -1L))
        assertNull(InlineVideoPolicy.fractionTarget(0.5f, 0L))
    }
    @Test fun invalidFractionsIgnored() {
        assertNull(InlineVideoPolicy.fractionTarget(Float.NaN, 1000L))
        assertNull(InlineVideoPolicy.fractionTarget(Float.POSITIVE_INFINITY, 1000L))
    }
    @Test fun fractionClampsBothEnds() {
        assertEquals(0L, InlineVideoPolicy.fractionTarget(-1f, 1000L))
        assertEquals(750L, InlineVideoPolicy.fractionTarget(2f, 1000L))
    }
    @Test fun tinyClipDoesNotCreateNegativeSeek() { assertEquals(0L, InlineVideoPolicy.seekTarget(100L, 50L)) }
    @Test fun onlyIdenticalRecordingCopiesPositionBack() {
        assertTrue(InlineVideoPolicy.canTransferPosition("same", "same"))
        assertFalse(InlineVideoPolicy.canTransferPosition("musicVideoCut", "albumCut"))
        assertFalse(InlineVideoPolicy.canTransferPosition(null, null))
        assertFalse(InlineVideoPolicy.canTransferPosition("", ""))
    }
}

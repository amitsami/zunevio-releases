package com.music.bitchord
import com.music.bitchord.ui.player.MediaLyricClock
import org.junit.Assert.*
import org.junit.Test

class MediaLyricClockTest {
    private val clock = MediaLyricClock()
    private fun at(now: Double, position: Long = 10_000L, stamp: Double = 1_000.0,
                   speed: Float = 1f, advancing: Boolean = true, seek: Long = 0L) =
        clock.frame(now, position, stamp, seek, speed, advancing)
    @Test fun interpolatesStampedPlayback() { assertEquals(10_200L, at(1_200.0)) }
    @Test fun usesActualSpeedNotPollingJitter() { assertEquals(10_300L, at(1_200.0, speed = 1.5f)) }
    @Test fun bufferingDoesNotAdvanceLyrics() { assertEquals(10_000L, at(1_600.0, advancing = false)) }
    @Test fun staleSampleCannotRunAway() { assertEquals(10_750L, at(100_000.0)) }
    @Test fun missingTimestampDoesNotExtrapolate() { assertEquals(10_000L, at(1_500.0, stamp = Double.NaN)) }
    @Test fun futureStampDoesNotMoveBackwards() { assertEquals(10_000L, at(500.0)) }
    @Test fun pausedSeekUsesExactPosition() { assertEquals(60_000L, at(2_000.0, 60_000L, advancing = false, seek = 1L)) }
    @Test fun backwardsSeekIsNotSmoothedForSeconds() {
        at(2_000.0, 50_000L)
        assertEquals(5_050L, at(3_050.0, 5_000L, 3_000.0, seek = 1L))
    }
    @Test fun invalidRateFallsBackToNormal() { assertEquals(10_200L, at(1_200.0, speed = Float.NaN)) }
    @Test fun pauseThenResumeDoesNotAccumulateWallTime() {
        assertEquals(10_000L, at(90_000.0, advancing = false))
        assertEquals(10_100L, at(91_100.0, stamp = 91_000.0))
    }
}

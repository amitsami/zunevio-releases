package com.music.bitchord

import com.music.bitchord.ui.player.LyricClock
import com.music.bitchord.ui.player.MediaLyricClock
import com.music.bitchord.ui.player.PlaybackLyricClock
import com.music.bitchord.ui.player.backingLyricDue
import com.music.bitchord.ui.player.animateCurrentLyricTransition
import com.music.bitchord.ui.player.lyricFocusTime
import org.junit.Assert.assertEquals
import org.junit.Test

/** Clock and presentation checks, not a claim of real-device acoustic synchronization. */
class AudioLyrics123RegressionTest {
    private fun compareAudio(rate: Double, pollMs: Int, deliveryDelayMs: Int) {
        val baseline = LyricClock(0L)
        val routed = PlaybackLyricClock(0L, videoTiming = false)
        var report = 0L
        var stamp = 0.0
        var next = 0
        for (now in 0..120_000 step 16) {
            if (now >= next + deliveryDelayMs) {
                stamp = next.toDouble()
                report = (stamp * rate).toLong()
                next += pollMs
            }
            assertEquals(
                "audio baseline differs at $now ms",
                baseline.frame(now.toDouble(), report, stamp, 0L),
                routed.frame(now.toDouble(), report, stamp, 0L, rate.toFloat(), true),
            )
        }
    }

    @Test fun audioMatches123AtNormalSpeed() = compareAudio(1.0, 200, 0)
    @Test fun audioMatches123WithOldPollingInterval() = compareAudio(1.0, 500, 0)
    @Test fun audioMatches123WithDelayedSamples() = compareAudio(1.0, 200, 650)
    @Test fun audioMatches123AtSlowSpeed() = compareAudio(0.75, 200, 40)
    @Test fun audioMatches123AtFastSpeed() = compareAudio(1.5, 200, 40)

    @Test fun audioPauseResumeAndSeekMatch123() {
        val baseline = LyricClock(10_000)
        val routed = PlaybackLyricClock(10_000, videoTiming = false)
        fun frame(now: Double, position: Long, stamp: Double, jump: Long) {
            assertEquals(
                baseline.frame(now, position, stamp, jump),
                routed.frame(now, position, stamp, jump, 1f, true),
            )
        }
        frame(0.0, 10_000, 0.0, 0)
        frame(200.0, 10_200, 200.0, 0)
        baseline.hold(10_200, 210.0)
        routed.hold(10_200, 210.0)
        baseline.restart()
        routed.restart()
        frame(50_000.0, 10_200, 200.0, 0)
        frame(50_200.0, 10_400, 50_200.0, 0)
        frame(50_216.0, 60_000, 50_216.0, 1)
        frame(50_232.0, 5_000, 50_232.0, 2)
    }

    @Test fun signedOffsetChangesMatch123WithoutAnExtraDelay() {
        val baseline = LyricClock(10_000)
        val routed = PlaybackLyricClock(10_000, videoTiming = false)
        listOf(0L, 250L, -250L, 0L).forEachIndexed { index, offset ->
            val now = index * 500.0
            val position = (10_000 + now - offset).toLong()
            assertEquals(
                baseline.frame(now, position, now, index.toLong()),
                routed.frame(now, position, now, index.toLong(), 1f, true),
            )
        }
    }

    @Test fun videoClockRetainsBuild44Behavior() {
        val baseline = MediaLyricClock()
        val routed = PlaybackLyricClock(0, videoTiming = true)
        listOf(0.0, 200.0, 800.0, 60_000.0).forEach { now ->
            for (moving in listOf(true, false)) {
                assertEquals(
                    baseline.frame(now, 15_000, 0.0, 1, 1.5f, moving),
                    routed.frame(now, 15_000, 0.0, 1, 1.5f, moving),
                )
            }
        }
    }

    @Test fun audioFocusStartsBeforeTheLyricWithoutChangingItsTimestamp() {
        val lyricTime = 10_000L
        assertEquals(lyricTime, lyricFocusTime(9_700, 300, videoTiming = false))
        assertEquals(9_700L, lyricFocusTime(9_700, 300, videoTiming = true))
        assertEquals(10_000L, lyricTime)
    }

    @Test fun zeroLeadDoesNotShiftEitherMode() {
        assertEquals(12_000L, lyricFocusTime(12_000, 0, videoTiming = false))
        assertEquals(12_000L, lyricFocusTime(12_000, 0, videoTiming = true))
    }

    @Test fun audioBackingLeadRestores123Boundaries() {
        for (now in 9_000L..12_500L) {
            assertEquals(
                now >= 10_000L - 250L && now < 12_000L,
                backingLyricDue(now, 10_000, 12_000, 250, videoTiming = false),
            )
        }
    }

    @Test fun videoBackingBoundariesAreUnchanged() {
        for (now in 9_000L..12_500L) {
            assertEquals(
                now >= 10_000L && now < 12_000L,
                backingLyricDue(now, 10_000, 12_000, 250, videoTiming = true),
            )
        }
    }

    @Test fun audioCaptionNeverWaitsForAnEntranceAnimation() {
        assertEquals(false, animateCurrentLyricTransition(false, false))
        assertEquals(false, animateCurrentLyricTransition(false, true))
    }

    @Test fun videoCaptionKeepsItsExistingAnimationPreference() {
        assertEquals(true, animateCurrentLyricTransition(true, false))
        assertEquals(false, animateCurrentLyricTransition(true, true))
    }
}
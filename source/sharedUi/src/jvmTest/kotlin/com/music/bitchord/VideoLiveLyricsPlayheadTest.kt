package com.music.bitchord

import androidx.compose.runtime.mutableStateOf
import com.music.bitchord.playback.PlaybackPosition
import com.music.bitchord.ui.player.LyricPlayhead
import com.music.bitchord.ui.player.MediaLyricClock
import com.music.bitchord.ui.player.adjustedLyricsSeekTarget
import org.junit.Assert.*
import org.junit.Test

/** Actual playhead/clock objects, without claiming to exercise an Android decoder or rendered UI. */
class VideoLiveLyricsPlayheadTest {
    @Test fun videoReadsVideoPositionNotAudioCalibration() {
        val position = PlaybackPosition().apply { report(12_000L, 1_000_000_000L) }
        val video = LyricPlayhead(position, mutableStateOf(0), videoTiming = true)
        val audio = LyricPlayhead(position, mutableStateOf(-400), videoTiming = false)
        assertEquals(12_000L, video.positionMs)
        assertEquals(12_400L, audio.positionMs)
    }
    @Test fun videoManualOffsetIsAppliedOnceAndSeekIsItsInverse() {
        val position = PlaybackPosition().apply { report(12_000L) }
        val offset = mutableStateOf(-200)
        val video = LyricPlayhead(position, offset, videoTiming = true)
        assertEquals(12_200L, video.positionMs)
        assertEquals(12_000L, adjustedLyricsSeekTarget(video.positionMs, offset.value))
        offset.value = 300
        assertEquals(11_700L, video.positionMs)
        assertEquals(12_000L, adjustedLyricsSeekTarget(video.positionMs, offset.value))
    }
    @Test fun pausedVideoAndSeekDoNotAccumulateWallTime() {
        val position = PlaybackPosition().apply {
            report(10_000L, 1_000_000_000L)
            advancing = false
        }
        val video = LyricPlayhead(position, mutableStateOf(0), videoTiming = true)
        val clock = MediaLyricClock()
        assertEquals(10_000L, clock.frame(90_000.0, video.positionMs, 1_000.0, video.discontinuity, 1f, video.advancing))
        position.seeks++
        position.report(35_000L, 90_000_000_000L)
        assertEquals(35_000L, clock.frame(90_100.0, video.positionMs, 90_000.0, video.discontinuity, 1f, video.advancing))
    }
    @Test fun videoOffsetChangeSignalsADiscontinuityWithoutChangingAudio() {
        val position = PlaybackPosition().apply { report(8_000L) }
        val audioOffset = mutableStateOf(-400)
        val videoOffset = mutableStateOf(0)
        val audio = LyricPlayhead(position, audioOffset, videoTiming = false)
        val video = LyricPlayhead(position, videoOffset, videoTiming = true)
        val before = video.discontinuity
        videoOffset.value = -600
        assertNotEquals(before, video.discontinuity)
        assertEquals(8_600L, video.positionMs)
        assertEquals(8_400L, audio.positionMs)
        assertEquals(-400, audioOffset.value)
    }
}

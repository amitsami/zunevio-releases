package com.music.bitchord.ui.player

/**
 * Keep the v1.2.3 audio clock independent of the newer inline-video timeline.
 * Presentation lead belongs to scrolling, never to lyric timestamps or seeks.
 */
internal class PlaybackLyricClock(startMs: Long, private val videoTiming: Boolean) {
    private val audio = LyricClock(startMs)
    private val video = MediaLyricClock()

    fun restart() {
        audio.restart()
    }

    fun hold(positionMs: Long, nowMs: Double) {
        audio.hold(positionMs, nowMs)
    }

    fun frame(
        nowMs: Double,
        reportedMs: Long,
        sampledAtMs: Double,
        discontinuity: Long,
        playbackSpeed: Float,
        advancing: Boolean,
    ): Long = if (videoTiming) {
        video.frame(nowMs, reportedMs, sampledAtMs, discontinuity, playbackSpeed, advancing)
    } else {
        audio.frame(nowMs, reportedMs, sampledAtMs, discontinuity)
    }
}

/** Restore audio's look-ahead layout without advancing the highlight clock. */
internal fun lyricFocusTime(nowMs: Long, leadMs: Long, videoTiming: Boolean): Long =
    if (videoTiming) nowMs else nowMs + leadMs

internal fun backingLyricDue(
    nowMs: Long,
    startMs: Long,
    endMs: Long,
    leadMs: Long,
    videoTiming: Boolean,
): Boolean = nowMs >= startMs - (if (videoTiming) 0L else leadMs) && nowMs < endMs

/** A one-line audio caption must be readable at its timestamp, not after a fade-in. */
internal fun animateCurrentLyricTransition(videoTiming: Boolean, reduceAnimation: Boolean): Boolean =
    videoTiming && !reduceAnimation
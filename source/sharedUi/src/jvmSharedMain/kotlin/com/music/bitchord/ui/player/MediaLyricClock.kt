package com.music.bitchord.ui.player

/** Engine-authoritative lyric time. Never learn a playback rate from polling jitter or run
 * indefinitely past a stale sample. Seeks/rebuffer corrections are accepted immediately. */
internal class MediaLyricClock {
    @Suppress("UNUSED_PARAMETER")
    fun frame(nowMs: Double, reportedMs: Long, sampledAtMs: Double, discontinuity: Long,
              playbackSpeed: Float, advancing: Boolean): Long {
        val speed = playbackSpeed.takeIf { it.isFinite() && it > 0f } ?: 1f
        val elapsed = if (advancing && sampledAtMs.isFinite())
            (nowMs - sampledAtMs).coerceIn(0.0, 750.0) else 0.0
        return (reportedMs.coerceAtLeast(0L) + elapsed * speed).toLong()
    }
}

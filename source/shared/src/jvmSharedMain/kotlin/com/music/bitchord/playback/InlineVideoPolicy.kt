package com.music.bitchord.playback

/** Transport rules independent of Android/rendering; exercised by offline tests. */
object InlineVideoPolicy {
    data class Frame(val width: Float, val height: Float)
    fun fitFrame(width: Float, height: Float, aspect: Float, inset: Float = 12f): Frame {
        val w = (width - 2f * inset).coerceAtLeast(0f)
        val h = (height - 2f * inset).coerceAtLeast(0f)
        val ratio = aspect.takeIf { it.isFinite() && it > 0f } ?: (16f / 9f)
        if (w <= 0f || h <= 0f) return Frame(0f, 0f)
        return if (w / h > ratio) Frame(h * ratio, h) else Frame(w, w / ratio)
    }

    fun seekTarget(targetMs: Long, durationMs: Long): Long? =
        if (durationMs <= 0L) null else targetMs.coerceIn(0L, (durationMs - 250L).coerceAtLeast(0L))
    fun fractionTarget(fraction: Float, durationMs: Long): Long? =
        if (!fraction.isFinite() || durationMs <= 0L) null
        else seekTarget((fraction.coerceIn(0f, 1f) * durationMs).toLong(), durationMs)
    fun canTransferPosition(videoId: String?, originalId: String?): Boolean =
        !originalId.isNullOrBlank() && originalId == videoId
}

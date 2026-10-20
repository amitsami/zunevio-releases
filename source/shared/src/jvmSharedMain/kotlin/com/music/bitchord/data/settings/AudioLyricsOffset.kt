package com.music.bitchord.data.settings

/**
 * User-confirmed audio calibration for this build, not an automatic measurement.
 * Stored as the actual offset, never stacked on top of a manual offset.
 */
object AudioLyricsOffset {
    const val DEFAULT_MS = -400

    fun initial(savedAudioMs: Int?, legacyMs: Int): Int =
        savedAudioMs ?: legacyMs.takeIf { it != 0 } ?: DEFAULT_MS
}
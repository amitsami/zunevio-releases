package com.music.bitchord.data

/** Style support is distinct from RenderEffect support. Never invoke API-31 effects on API 29. */
object RenderPolicy {
    fun glassStyle(sdk: Int): Boolean = sdk >= 29
    fun backdropBlur(sdk: Int, lowRam: Boolean, reduceBlur: Boolean): Boolean =
        sdk >= 31 && !lowRam && !reduceBlur
    fun tabDirection(from: Int, to: Int): Int = when { to > from -> 1; to < from -> -1; else -> 0 }
}

package com.music.bitchord.data.innertube

/** Saved separately for Wi-Fi and mobile data. Heights are provider-dependent ceilings. */
enum class VideoQuality(val maxHeight: Int, val audioKbps: Int) {
    AUTO(720, 128), LOW(360, 96), MEDIUM(720, 128), HIGH(1080, 192);
    companion object {
        fun read(value: String?, default: VideoQuality): VideoQuality = entries.firstOrNull { it.name == value } ?: default
        fun forNetwork(wifi: Boolean, wifiQuality: VideoQuality = AUTO, mobileQuality: VideoQuality = AUTO) =
            if (wifi) wifiQuality else mobileQuality
    }
}

data class VideoConnection(val wifi: Boolean, val handle: Long)
data class VideoRendition(val url: String, val height: Int, val muxed: Boolean, val mimeType: String,
    val codec: String = "", val fps: Int = 0)

/** Resolution first: a 360p muxed stream must not hide an available 1080p adaptive stream. */
fun chooseVideoRendition(candidates: List<VideoRendition>, maxHeight: Int = 720): VideoRendition? {
    val usable = candidates.filter { it.url.startsWith("https://") && it.height > 0 }
    val bounded = usable.filter { it.height <= maxHeight }.ifEmpty {
        val lowest = usable.minOfOrNull { it.height }
        usable.filter { it.height == lowest }
    }
    return bounded.sortedWith(compareByDescending<VideoRendition> { it.height }
        .thenByDescending { it.codec.startsWith("avc", true) || it.codec.startsWith("h264", true) }
        .thenByDescending { it.mimeType == "video/mp4" }
        .thenByDescending { it.fps <= 30 }
        .thenByDescending { it.muxed }).firstOrNull()
}

/** Bounded progressive quality changes, measured from actual media transfers. */
object AutoVideoPolicy {
    fun initial(wifi: Boolean) = if (wifi) VideoQuality.MEDIUM else VideoQuality.LOW
    fun tier(bitsPerSecond: Long): VideoQuality = when {
        bitsPerSecond >= 4_500_000L -> VideoQuality.HIGH
        bitsPerSecond >= 1_500_000L -> VideoQuality.MEDIUM
        else -> VideoQuality.LOW
    }
    fun next(current: VideoQuality, bitsPerSecond: Long, bufferedMs: Long,
        bufferingForMs: Long, sinceChangeMs: Long): VideoQuality {
        // Emergency downshift must not wait out the normal quality cooldown.
        // Keep a short guard so an initial prepare cannot repeatedly restart itself.
        if (bufferingForMs >= 2_500L && sinceChangeMs >= 3_000L) return when (current) {
            VideoQuality.HIGH -> VideoQuality.MEDIUM
            else -> VideoQuality.LOW
        }
        if (sinceChangeMs < 15_000L || bitsPerSecond <= 0L) return current
        val target = tier(bitsPerSecond)
        if (target.maxHeight < current.maxHeight && bufferedMs < 4_000L) return target
        if (target.maxHeight > current.maxHeight && bufferedMs >= 8_000L && sinceChangeMs >= 45_000L) return target
        return current
    }
}

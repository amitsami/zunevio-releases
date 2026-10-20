package com.music.bitchord.ui.player

import kotlinx.coroutines.CancellationException

internal suspend fun <T> lyricsOperationOrDefault(fallback: T, operation: suspend () -> T): T = try {
    operation()
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (_: Exception) {
    fallback
}

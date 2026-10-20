package com.music.bitchord.data.lyrics

import java.io.IOException

/** A provider outage or access failure is not evidence that a song has no lyrics. */
class LyricsProviderUnavailableException(
    message: String,
    val httpStatus: Int? = null,
    cause: Throwable? = null,
) : IOException(message, cause)

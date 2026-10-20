package com.music.bitchord.data.spotify

import java.net.URI

/** Login navigation rules; never accept a credential from an unrelated origin. */
object SpotifyLoginPolicy {
    private val loginHosts = listOf("spotify.com", "scdn.co", "google.com", "gstatic.com", "facebook.com", "apple.com")
    private fun host(url: String?): String? = runCatching {
        val uri = URI(url ?: return null)
        if (!uri.scheme.equals("https", ignoreCase = true) || uri.rawUserInfo != null || (uri.port != -1 && uri.port != 443)) null
        else uri.host?.lowercase()
    }.getOrNull()
    fun allowed(url: String?): Boolean = host(url)?.let { h -> loginHosts.any { h == it || h.endsWith(".$it") } } == true
    // A pre-existing sp_dc on the login form does not prove this login succeeded.
    fun completedLanding(url: String?): Boolean = host(url) == "open.spotify.com"
    fun sessionCookie(header: String?): String? = header?.split(';')?.asSequence()
        ?.map { it.trim() }?.firstOrNull { it.substringBefore('=') == "sp_dc" }
        ?.substringAfter('=', "")?.takeIf { it.isNotBlank() && !it.equals("deleted", true) && it != "null" }
}

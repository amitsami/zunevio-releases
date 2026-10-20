package com.music.bitchord.data.spotify

import kotlin.test.*

class SpotifyLoginPolicyTest {
    @Test fun onlyHttpsTrustedOriginsCanNavigate() {
        assertTrue(SpotifyLoginPolicy.allowed("https://accounts.spotify.com/login"))
        assertTrue(SpotifyLoginPolicy.allowed("https://accounts.google.com/signin"))
        for (u in listOf("http://accounts.spotify.com", "https://spotify.com.evil.test", "https://evilspotify.com", "javascript:alert(1)", "https://user@spotify.com", "https://spotify.com:444", "intent://spotify.com")) assertFalse(SpotifyLoginPolicy.allowed(u), u)
        assertFalse(SpotifyLoginPolicy.allowed(null))
    }
    @Test fun oldLoginPageCookieCannotCompleteNewLogin() {
        assertFalse(SpotifyLoginPolicy.completedLanding("https://accounts.spotify.com/login"))
        assertFalse(SpotifyLoginPolicy.completedLanding("https://accounts.google.com/"))
        assertFalse(SpotifyLoginPolicy.completedLanding("https://open.spotify.com.evil.test/"))
        assertTrue(SpotifyLoginPolicy.completedLanding("https://open.spotify.com/"))
        assertTrue(SpotifyLoginPolicy.completedLanding("https://open.spotify.com/collection/playlists"))
    }
    @Test fun extractExactNonemptyCookieOnly() {
        assertEquals("abc=123", SpotifyLoginPolicy.sessionCookie("sp_t=other; sp_dc=abc=123; extra=x"))
        for (h in listOf(null, "", "other_sp_dc=wrong", "sp_dc=", "sp_dc=deleted", "sp_dc=null")) assertNull(SpotifyLoginPolicy.sessionCookie(h))
    }
}

package com.music.bitchord.data

/** Settings preserves the list below a sub-page. Both markers must pop together. */
data class SettingsOverlayState(
    val page: String?,
    val account: Boolean = false,
    val sources: Boolean = false,
    val equalizer: Boolean = false,
    val listenTogether: Boolean = false,
    val replay: Boolean = false,
) {
    fun dismiss(): SettingsOverlayState = copy(
        page = null,
        account = account && page != "account_scrobbling",
        sources = sources && page != "sources",
        equalizer = equalizer && page != "equalizer",
        listenTogether = listenTogether && page != "listen_together",
        replay = replay && page != "replay",
    )
}

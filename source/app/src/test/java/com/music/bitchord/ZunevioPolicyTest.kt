package com.music.bitchord

import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.data.presence.Presence
import com.music.bitchord.data.sources.SourceConfig
import com.music.bitchord.data.sources.SourceKind
import com.music.bitchord.data.sources.SourceRegistry
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class ZunevioPolicyTest {
    @Test fun noBundledPresenceOrPartyEndpoint() {
        assertEquals("", Presence.ENDPOINT)
        assertEquals("", BuildConfig.LISTEN_TOGETHER_SERVER)
    }
    @Test fun scrobblingAndDiscordAreOff() {
        assertFalse(AppSettings.scrobblingAvailable)
        assertFalse(AppSettings.discordRpcEnabled.value)
    }
    @Test fun retiredSourceCannotReturnFromSavedEnabledConfiguration() {
        val saved = listOf(SourceConfig(kind = SourceKind.JIOSAAVN, enabled = true))
        assertTrue(SourceRegistry.sourcesForInit(saved, false).none { it.kind == SourceKind.JIOSAAVN })
    }
    @Test fun applicationDoesNotInstallUpstreamPresence() {
        val text = File("src/main/java/com/music/bitchord/BitChordApplication.kt").readText()
        assertFalse(text.contains("AndroidPresence.install(this)"))
    }
    @Test fun accountScreenContainsNoRemovedIntegrationControls() {
        val text = File("src/main/java/com/music/bitchord/ui/screens/AccountAndScrobblingScreen.kt").readText()
        assertFalse(text.contains("title = \"Discord\""))
        assertFalse(text.contains("title = \"Last.fm\""))
        assertFalse(text.contains("title = \"ListenBrainz\""))
        assertFalse(text.contains("R.string.scrobbling)"))
    }
}

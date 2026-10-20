package com.music.bitchord.data

import kotlin.test.*

class SettingsOverlayStateTest {
    @Test fun accountClosesVisibilityAndOverlayInOneAction() {
        assertEquals(SettingsOverlayState(null), SettingsOverlayState("account_scrobbling", account = true).dismiss())
    }
    @Test fun sourcesCloseVisibilityAndOverlayInOneAction() {
        assertEquals(SettingsOverlayState(null), SettingsOverlayState("sources", sources = true).dismiss())
    }
    @Test fun equalizerClosesVisibilityAndOverlayInOneAction() {
        assertEquals(SettingsOverlayState(null), SettingsOverlayState("equalizer", equalizer = true).dismiss())
    }
    @Test fun partyClosesVisibilityAndOverlayInOneAction() {
        assertEquals(SettingsOverlayState(null), SettingsOverlayState("listen_together", listenTogether = true).dismiss())
    }
    @Test fun replayClosesVisibilityAndOverlayInOneAction() {
        assertEquals(SettingsOverlayState(null), SettingsOverlayState("replay", replay = true).dismiss())
    }
    @Test fun settingsOpenedOverReplayDoesNotDestroyUnderlyingReplay() {
        val state = SettingsOverlayState("sources", sources = true, replay = true).dismiss()
        assertNull(state.page); assertFalse(state.sources); assertTrue(state.replay)
    }
    @Test fun repeatedDismissIsIdempotent() {
        val first = SettingsOverlayState("sources", sources = true).dismiss()
        assertEquals(first, first.dismiss())
    }
    @Test fun restoredOverlayWithoutVisibilityFlagStillCloses() {
        assertNull(SettingsOverlayState("equalizer").dismiss().page)
    }
}

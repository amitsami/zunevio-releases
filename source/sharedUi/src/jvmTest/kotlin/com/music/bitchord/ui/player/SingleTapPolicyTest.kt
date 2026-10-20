package com.music.bitchord.ui.player

import org.junit.Assert.*
import org.junit.Test

class SingleTapPolicyTest {
    @Test fun stationaryFirstTapIsAccepted() = assertTrue(isLyricsTap(0f, 8f, 1))
    @Test fun normalFingerWobbleIsAccepted() = assertTrue(isLyricsTap(7f, 8f, 1))
    @Test fun scrollingIsNotATap() = assertFalse(isLyricsTap(9f, 8f, 1))
    @Test fun multiTouchDoesNotRevealOrSeek() = assertFalse(isLyricsTap(0f, 8f, 2))
}

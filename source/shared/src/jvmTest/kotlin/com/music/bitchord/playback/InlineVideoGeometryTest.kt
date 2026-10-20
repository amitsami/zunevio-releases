package com.music.bitchord.playback

import kotlin.test.*

class InlineVideoGeometryTest {
    @Test fun wideVideoIsAspectFitWithEqualOppositeInsets() {
        val f = InlineVideoPolicy.fitFrame(360f, 360f, 16f / 9f)
        assertEquals(336f, f.width, .01f)
        assertEquals(189f, f.height, .01f)
        assertEquals(16f / 9f, f.width / f.height, .001f)
        assertEquals(12f, (360f - f.width) / 2f, .01f)
        assertEquals(85.5f, (360f - f.height) / 2f, .01f)
    }
    @Test fun portraitVideoIsNotStretchedOrCropped() {
        val f = InlineVideoPolicy.fitFrame(360f, 360f, 9f / 16f)
        assertEquals(189f, f.width, .01f)
        assertEquals(336f, f.height, .01f)
    }
    @Test fun squareKeepsAllFourMarginsEqual() {
        val f = InlineVideoPolicy.fitFrame(360f, 360f, 1f)
        assertEquals(336f, f.width); assertEquals(f.width, f.height)
    }
    @Test fun unknownRatioHasSafeDefault() {
        assertEquals(InlineVideoPolicy.fitFrame(360f,360f,16f/9f), InlineVideoPolicy.fitFrame(360f,360f,Float.NaN))
    }
    @Test fun tinyWindowNeverCreatesNegativeSize() {
        assertEquals(InlineVideoPolicy.Frame(0f,0f), InlineVideoPolicy.fitFrame(10f,10f,1f))
    }
}

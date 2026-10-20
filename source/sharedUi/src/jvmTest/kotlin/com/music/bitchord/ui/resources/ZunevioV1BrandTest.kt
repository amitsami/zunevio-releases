package com.music.bitchord.ui.resources

import com.music.bitchord.ui.icons.ZunevioBrand
import com.music.bitchord.ui.icons.ZunevioIcons
import com.music.bitchord.ui.icons.BitChordIcons
import androidx.compose.ui.graphics.vector.ImageVector
import org.junit.Assert.*
import org.junit.Test

class ZunevioV1BrandTest {
    @Test fun onlyInitialIsRed() {
        val mark = ZunevioBrand.wordmark
        assertEquals("Zunéviu", mark.text)
        assertEquals(1, mark.spanStyles.size)
        val red = mark.spanStyles.single()
        assertEquals(0, red.start)
        assertEquals(1, red.end)
        assertEquals(ZunevioBrand.Red, red.item.color)
    }
    @Test fun sentenceBrandingOnlyColorsEachInitial() {
        val text = "Open Zunévio. Zunévio update."
        val mark = ZunevioBrand.styled(text)
        assertEquals(ZunevioBrand.normalize(text), mark.text)
        assertEquals(2, mark.spanStyles.size)
        mark.spanStyles.forEach {
            assertEquals("Z", text.substring(it.start, it.end))
            assertEquals(ZunevioBrand.Red, it.item.color)
        }
        assertTrue(ZunevioBrand.styled("Ordinary text").spanStyles.isEmpty())
    }
    @Test fun replayUppercaseAndUnaccentedBrandOnlyColorTheZ() {
        val text = "ZUNÉVIO LISTENER · Zunevio · zunévio"
        val mark = ZunevioBrand.styled(text)
        assertEquals(ZunevioBrand.normalize(text), mark.text)
        assertEquals(3, mark.spanStyles.size)
        mark.spanStyles.forEach {
            assertEquals(1, it.end - it.start)
            assertEquals(ZunevioBrand.Red, it.item.color)
        }
        assertTrue(ZunevioBrand.contains("ZUNÉVIO LISTENER"))
    }
    @Test fun legacySpellingsNormalizeToAccentedCurrentName() {
        for (name in listOf("Zunevio", "Zunévio", "Zuneviu", "Zunéviu")) {
            assertEquals("Zunéviu", ZunevioBrand.normalize(name))
            assertEquals(1, ZunevioBrand.styled(name).spanStyles.size)
        }
    }
    @Test fun everyApprovedVectorBuilds() {
        val getters = ZunevioIcons::class.java.methods.filter {
            it.parameterCount == 0 && it.returnType == ImageVector::class.java
        }
        assertTrue(getters.size >= 25)
        getters.forEach {
            val vector = it.invoke(ZunevioIcons) as ImageVector
            assertEquals(24f, vector.viewportWidth, 0f)
            assertEquals(24f, vector.viewportHeight, 0f)
            assertTrue(vector.name, vector.root.size > 0)
            assertSame(vector, it.invoke(ZunevioIcons))
        }
    }
    @Test fun footerExploreUsesApprovedCompass() {
        assertSame(ZunevioIcons.Explore, BitChordIcons.TabExplore)
    }
}

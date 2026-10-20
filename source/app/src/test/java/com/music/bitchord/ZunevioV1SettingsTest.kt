package com.music.bitchord

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Source-level guards, not device UI or smoothness tests. */
class ZunevioV1SettingsTest {
    private fun source(path: String) = File("src/main/java/com/music/bitchord/$path").readText()
    @Test fun contactLinksAndDeveloperCreditStayPresent() {
        val settings = source("ui/screens/SettingsSheet.kt")
        assertTrue(settings.contains("Amit Hasan Sami"))
        assertTrue(settings.contains("mailto:amitsami110@gmail.com"))
        assertTrue(settings.contains("https://github.com/amitsami"))
    }
    @Test fun toolbarIsSettingsNotProfilePhoto() {
        val source = source("ui/components/FrostedTopBar.kt")
        val button = source.substringAfter("fun TopBarAccountButton(").substringBefore("The refresh indicator")
        assertTrue(button.contains("ZunevioIcons.Settings"))
        assertFalse(button.contains("AsyncImage("))
    }
    @Test fun settingsHasNoPerCardGpuEffects() {
        val glass = source("ui/screens/SettingsGlass.kt")
        for (effect in listOf("Modifier.liquidGlass(", "drawBackdrop(", "layerBackdrop(",
            "rememberLayerBackdrop(", "RenderEffect", "rememberInfiniteTransition")) {
            assertFalse("Settings must not use $effect", glass.contains(effect))
        }
        assertTrue(glass.contains("!glass || reduceBlur"))
        assertEquals(1, Regex("collectAsStateWithLifecycle\\(").findAll(glass).count())
    }
    @Test fun settingsObservesOnlyAtmosFlagNotPlaybackTelemetry() {
        val settings = source("ui/screens/SettingsSheet.kt")
        assertTrue(settings.contains("NerdStats.current.mapPlaybackStatus { it?.isDolbyAtmos == true }.distinctUntilChanged()"))
        assertFalse(settings.contains("val playingFormat by NerdStats.current"))
    }
}

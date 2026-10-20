package com.music.bitchord
import java.io.File
import org.junit.Assert.*
import org.junit.Test
class SettingsNavigationTest {
    @Test fun eachCategoryIsIndependentlyGatedBeforeComposition() {
        val source = File("src/main/java/com/music/bitchord/ui/screens/SettingsSheet.kt").readText()
        for (key in listOf("audio_quality", "appearance", "downloads", "playback", "performance", "local_music", "storage", "your_data", "language", "advanced_options", "miscellaneous", "local_recommendations"))
            assertTrue(key, source.contains("if (selectedCategory == \"$key\""))
        val main = File("src/main/java/com/music/bitchord/MainActivity.kt").readText()
        assertTrue(main.contains("showSettings && settingsCategory != null"))
        assertTrue(main.contains("if (settingsCategory != null) { settingsCategory = null; return@BackHandler }"))
        // A mounted settings page must not steal Back from Sources/account/update overlays.
        assertFalse(source.contains("BackHandler(enabled = selectedCategory"))
        assertEquals(2, Regex("AppUpdateChecker.consumeIntent\\(intent\\)").findAll(main).count())
    }
}

package com.music.bitchord

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Source-level layout guards; emulator QA separately checks rendered screens. */
class Zunevio122UiTest {
    private fun source(path: String) = File("src/main/java/com/music/bitchord/$path").readText()
    @Test fun searchAppearsOnlyOnSettingsRoot() {
        val settings = source("ui/screens/SettingsSheet.kt")
        assertTrue(settings.contains("if (selectedCategory == null) SearchField("))
        assertTrue(settings.contains("SettingsSearch(if (selectedCategory == null) searchQuery else \"\")"))
        assertFalse(settings.contains("title = lyricsSourcesTitle"))
        assertFalse(settings.contains("Back to settings"))
    }
    @Test fun primarySearchHasNoSourceSwitcher() {
        assertTrue(source("MainActivity.kt").contains("sourceSwitcher = null"))
    }
    @Test fun shuffleUsesVisibleTracksAndGuardsSelection() {
        val local = source("ui/screens/LocalMusicScreen.kt")
        assertTrue(local.contains("shuffleEnabled = !selectingDownloads"))
        assertEquals(2, Regex("TextButton\\(enabled = songs.isNotEmpty\\(\\) && shuffleEnabled").findAll(local).count())
        assertTrue(local.contains("onClick = { onShuffle(songs) }"))
    }
    @Test fun updatesHasExplicitReadableContentColors() {
        val updates = source("ui/components/UpdateSettingsCard.kt")
        assertTrue(updates.contains("LocalContentColor provides colors.onBackground"))
        assertTrue(updates.contains("contentColor = colors.onSurface"))
        assertTrue(updates.contains("AppUpdateChecker.installApk(context, state.file)"))
        assertTrue(updates.contains("AppUpdateChecker::cancelDownload"))
    }
}

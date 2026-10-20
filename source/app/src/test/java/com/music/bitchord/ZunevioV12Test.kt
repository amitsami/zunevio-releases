package com.music.bitchord

import com.music.bitchord.data.model.Song
import com.music.bitchord.data.spotify.DeviceFavorites
import com.music.bitchord.data.spotify.LocalPlaylist
import com.music.bitchord.data.spotify.LocalPlaylistStore
import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Pure model behavior plus explicitly source-level guards; no device performance claim. */
class ZunevioV12Test {
    private val song = Song("one", "Title", "Artist", null)
    private fun source(path: String) = File("src/main/java/com/music/bitchord/$path").readText()

    @Test fun guestFavoriteCreatesBrowsableCollection() {
        val result = DeviceFavorites.toggle(emptyList(), song)
        assertEquals("local:playlist:device_favorites", result.single().browseId)
        assertEquals(listOf(song), result.single().songs)
    }
    @Test fun togglingAgainRemovesSongWithoutDuplicatingCollection() {
        val once = DeviceFavorites.toggle(emptyList(), song)
        val twice = DeviceFavorites.toggle(once, song)
        assertEquals(1, twice.size)
        assertTrue(twice.single().songs.isEmpty())
    }
    @Test fun favoritesKeepNewestFirstAndLeaveOtherPlaylistsUntouched() {
        val old = LocalPlaylist("imported", "Imported", listOf(song))
        val first = DeviceFavorites.toggle(listOf(old), song)
        val secondSong = song.copy(videoId = "two")
        val result = DeviceFavorites.toggle(first, secondSong)
        assertEquals(listOf(secondSong, song), result.first().songs)
        assertEquals(old, result.last())
        assertEquals(2, result.size)
    }
    @Test fun localFavoriteRetainsItsPlayableUri() {
        val local = song.copy(localUri = "content://media/external/audio/media/123")
        assertEquals(local, DeviceFavorites.toggle(emptyList(), local).single().songs.single())
        val store = source("data/spotify/LocalPlaylistStore.kt")
        assertTrue(store.contains("sObj.put(\"localUri\", song.localUri)"))
        assertTrue(store.contains("localUri = sObj.optString(\"localUri\""))
        assertTrue(store.contains("writer.execute"))
    }
    @Test fun removingOneFavoriteDoesNotRemoveOthers() {
        val other = song.copy(videoId = "two")
        val list = DeviceFavorites.toggle(DeviceFavorites.toggle(emptyList(), song), other)
        val result = DeviceFavorites.toggle(list, song)
        assertEquals(listOf(other), result.single().songs)
    }
    @Test fun stripRemainsSingleLineAndClickableButIsUnboxed() {
        val lyrics = File("../sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/player/PlayerLyrics.kt").readText()
        val strip = lyrics.substringAfter("fun CurrentLyricStrip(").substringBefore("\n@Composable", "")
        assertTrue(strip.contains("CurrentLyricLine("))
        assertTrue(strip.contains("heightIn(min = 44.dp)"))
        assertTrue(strip.contains(".clickable(onClick = onClick)"))
        assertFalse(strip.contains(".zunevioPlayerGlass("))
    }
    @Test fun portraitAndLandscapeHeartAreNotHiddenFromGuests() {
        for (name in listOf("NowPlayingScreen", "LandscapePlayer")) {
            val player = File("../sharedUi/src/jvmSharedMain/kotlin/com/music/bitchord/ui/player/$name.kt").readText()
            assertFalse(player.contains("if (signedIn && song.localUri == null)"))
            assertTrue(player.contains("BitChordIcons.HeartFilled else BitChordIcons.Heart"))
        }
        val main = source("MainActivity.kt")
        assertEquals(2, Regex("onToggleLike = \\{ viewModel.togglePlayerLike\\(song\\) \\}").findAll(main).count())
    }
    @Test fun refreshDoesNotRejectGuestLibraryAndAlwaysClearsBusyState() {
        val vm = source("ui/MainViewModel.kt")
        val refresh = vm.substringAfter("fun refresh(feed: Feed)").substringBefore("fun ensureExploreLoaded")
        assertFalse(refresh.contains("feed == Feed.LIBRARY && !_signedIn.value"))
        assertTrue(refresh.contains("Downloads.getDownloadedSongs"))
        assertTrue(refresh.contains("refreshPersonalDiscovery(force = true)"))
        assertTrue(refresh.contains("finally {"))
        assertTrue(refresh.contains("_refreshing.update { it - feed }"))
        assertTrue(refresh.contains("withTimeout(35_000L)"))
    }
    @Test fun offThumbAndTrackHaveAtLeastThreeToOneContrastInBothThemes() {
        fun luminance(rgb: Int): Double {
            fun channel(shift: Int): Double {
                val v = ((rgb shr shift) and 255) / 255.0
                return if (v <= 0.04045) v / 12.92 else Math.pow((v + 0.055) / 1.055, 2.4)
            }
            return 0.2126 * channel(16) + 0.7152 * channel(8) + 0.0722 * channel(0)
        }
        for ((thumb, track) in listOf(0xD4D7DD to 0x30343B, 0x44484F to 0xD8DCE2,
            0xA4ABB6 to 0x282C33, 0x646B76 to 0xDDE0E5)) {
            val a = luminance(thumb); val b = luminance(track)
            assertTrue((maxOf(a, b) + 0.05) / (minOf(a, b) + 0.05) >= 3.0)
        }
    }
    @Test fun legacyGradientIsRemovedAndOldPreferenceCannotRestoreIt() {
        val settings = source("ui/screens/SettingsSheet.kt")
        assertFalse(settings.contains("R.string.legacy_mesh_gradient"))
        assertFalse(settings.contains("setLegacyMeshGradient"))
        val preferences = source("data/settings/AppSettings.kt")
        assertTrue(preferences.contains("prefs.edit().remove(KEY_LEGACY_MESH_GRADIENT).apply()"))
        assertFalse(preferences.contains("prefs.getBoolean(KEY_LEGACY_MESH_GRADIENT"))
        assertFalse(com.music.bitchord.data.settings.AppSettings.legacyMeshGradient.value)
        assertFalse(preferences.contains("fun setLegacyMeshGradient("))
    }
    @Test fun backFromCategoryRestoresRootOffsetInsteadOfZero() {
        val positions = com.music.bitchord.ui.screens.SettingsScrollPositions()
        val root = positions.stateFor(null)
        root.dispatchRawDelta(420f)
        positions.stateFor("appearance").dispatchRawDelta(180f)
        assertSame(root, positions.stateFor(null))
        assertEquals(420, positions.stateFor(null).value)
        assertEquals(180, positions.stateFor("appearance").value)
    }
    @Test fun categoriesDoNotClampOrReuseEachOthersOffsets() {
        val positions = com.music.bitchord.ui.screens.SettingsScrollPositions()
        positions.stateFor("playback").dispatchRawDelta(650f)
        assertEquals(0, positions.stateFor("storage").value)
        assertEquals(650, positions.stateFor("playback").value)
    }
    @Test fun searchHasOneIndependentSlotAndDoesNotLoseRootPosition() {
        val positions = com.music.bitchord.ui.screens.SettingsScrollPositions()
        positions.stateFor(null).dispatchRawDelta(420f)
        val search = positions.stateFor(null, searching = true)
        search.dispatchRawDelta(75f)
        assertSame(search, positions.stateFor(null, searching = true))
        assertEquals(420, positions.stateFor(null).value)
        assertEquals(75, search.value)
        assertEquals(2, positions.snapshot().size)
    }
    @Test fun savedPositionsRestoreAfterHostRecreation() {
        val positions = com.music.bitchord.ui.screens.SettingsScrollPositions()
        positions.stateFor(null).dispatchRawDelta(420f)
        positions.stateFor("appearance").dispatchRawDelta(180f)
        val restored = com.music.bitchord.ui.screens.SettingsScrollPositions.restore(positions.snapshot())
        assertEquals(420, restored.stateFor(null).value)
        assertEquals(180, restored.stateFor("appearance").value)
    }
    @Test fun scrollOwnershipSurvivesSettingsUnmountAndBackDoesNotResetIt() {
        val main = source("MainActivity.kt")
        assertTrue(main.contains("saver = com.music.bitchord.ui.screens.SettingsScrollPositions.Saver"))
        assertTrue(main.contains("scrollPositions = settingsScrollPositions"))
        val settings = source("ui/screens/SettingsSheet.kt")
        assertFalse(settings.contains("LaunchedEffect(searchQuery, selectedCategory)"))
        assertTrue(settings.contains("scrollPositions.stateFor(selectedCategory, selectedCategory == null && searchQuery.isNotBlank())"))
    }
    @Test fun allSettingsSwitchesHaveExplicitVisibleOffAndDisabledColors() {
        val palette = source("ui/components/SettingsSwitchColors.kt")
        for (key in listOf("uncheckedThumbColor", "uncheckedTrackColor", "uncheckedBorderColor", "disabledUncheckedThumbColor", "disabledUncheckedTrackColor", "disabledUncheckedBorderColor")) {
            assertTrue(key, palette.contains("$key ="))
        }
        val settings = source("ui/screens/SettingsSheet.kt")
        assertEquals(25, Regex("settingsSwitchColors\\(\\)").findAll(settings).count())
        assertTrue(source("ui/components/UpdateSettingsCard.kt").contains("colors = settingsSwitchColors()"))
        assertFalse(palette.contains("rememberInfiniteTransition"))
    }
}

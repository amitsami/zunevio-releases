package com.music.bitchord.data

import kotlin.test.*

class CacheBudgetsTest {
    @Test fun disposableBudgetsAreBoundedAndAudioIs128MiB() {
        assertEquals(128L * 1024 * 1024, CacheBudgets.DEFAULT_AUDIO_BYTES)
        assertEquals(64L * 1024 * 1024, CacheBudgets.IMAGE_BYTES)
        assertEquals(64L * 1024 * 1024, CacheBudgets.CANVAS_BYTES)
        assertEquals(32L * 1024 * 1024, CacheBudgets.REMOTE_ART_BYTES)
        assertTrue(CacheBudgets.DEFAULT_AUDIO_BYTES + CacheBudgets.IMAGE_BYTES + CacheBudgets.CANVAS_BYTES + CacheBudgets.REMOTE_ART_BYTES <= 288L * 1024 * 1024)
    }
    @Test fun installedUpdateCleanupKeepsNewerAndUnknownPackages() {
        assertTrue(CacheBudgets.obsoleteInstaller("com.zunevio.music", "com.zunevio.music", 49, 50))
        assertTrue(CacheBudgets.obsoleteInstaller("com.zunevio.music", "com.zunevio.music", 50, 50))
        assertFalse(CacheBudgets.obsoleteInstaller("com.zunevio.music", "com.zunevio.music", 51, 50))
        assertFalse(CacheBudgets.obsoleteInstaller("other.app", "com.zunevio.music", 49, 50))
        assertFalse(CacheBudgets.obsoleteInstaller(null, "com.zunevio.music", null, 50))
    }
    @Test fun partialCleanupNeverTargetsDownloadsOrRecentInstaller() {
        assertTrue(CacheBudgets.abandonedPartial("download.apk", 1_000, 86_402_000))
        assertFalse(CacheBudgets.abandonedPartial("download.apk", 1_000, 2_000))
        assertFalse(CacheBudgets.abandonedPartial("song.mp3", 1_000, 86_402_000))
        assertFalse(CacheBudgets.abandonedPartial("update.apk", 1_000, 86_402_000))
        assertFalse(CacheBudgets.abandonedPartial("download.apk", 3_000, 2_000))
    }
}

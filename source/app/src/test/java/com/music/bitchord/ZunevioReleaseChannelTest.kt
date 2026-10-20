package com.music.bitchord

import java.io.File
import org.junit.Assert.*
import org.junit.Test

class ZunevioReleaseChannelTest {
    private fun source(path: String) = File("src/main/java/com/music/bitchord/$path").readText()
    @Test fun officialChannelIsConfiguredWithoutAnAccountOrToken() {
        val checker = source("data/AppUpdateChecker.kt")
        assertTrue(checker.contains("const val OFFICIAL_REPOSITORY = \"amitsami/zunevio-releases\""))
        assertTrue(checker.contains("fun repository(): String = OFFICIAL_REPOSITORY"))
        assertFalse(checker.contains("zunevio-development"))
        assertFalse(checker.contains("Authorization"))
    }
    @Test fun updaterDoesNotNavigateToGitHub() {
        val main = source("MainActivity.kt")
        assertFalse(main.contains("Uri.parse(update.releaseUrl)"))
        val settings = source("ui/components/UpdateSettingsCard.kt")
        assertFalse(settings.contains("View GitHub release"))
        assertFalse(settings.contains("Set repository"))
        assertTrue(settings.contains("Download update"))
        assertTrue(settings.contains("Install update"))
    }
    @Test fun permissionIsContextualAndBackgroundPollingIsNotInstantPush() {
        val checker = source("data/AppUpdateChecker.kt")
        assertTrue(checker.contains("PeriodicWorkRequestBuilder<ReleaseCheckWorker>(12, TimeUnit.HOURS)"))
        assertTrue(checker.contains("POST_NOTIFICATIONS"))
        assertTrue(source("MainActivity.kt").contains("AppUpdateChecker.markNotificationPrompted()"))
    }
}

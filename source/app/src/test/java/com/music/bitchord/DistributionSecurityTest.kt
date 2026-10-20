package com.music.bitchord

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Static hardening guards, not an antivirus verdict or Google certification. */
class DistributionSecurityTest {
    private fun app(path: String) = File("src/$path").readText()
    @Test fun appDoesNotRequestHighRiskDataAccess() {
        val manifest=app("main/AndroidManifest.xml")
        for (permission in listOf("READ_SMS","RECEIVE_SMS","READ_CONTACTS","QUERY_ALL_PACKAGES","SYSTEM_ALERT_WINDOW","BIND_ACCESSIBILITY_SERVICE"))
            assertFalse(permission,manifest.contains("android.permission.$permission"))
    }
    @Test fun microphoneIsExplicitAndForegroundOnly() {
        val code=app("main/java/com/music/bitchord/ui/components/VoiceSearch.kt")
        assertTrue(app("main/AndroidManifest.xml").contains("android.permission.RECORD_AUDIO"))
        assertTrue(code.contains("ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)"))
        assertTrue(code.contains("permission.launch(Manifest.permission.RECORD_AUDIO)"))
        assertTrue(code.contains("Lifecycle.Event.ON_STOP"))
        assertTrue(code.contains("old?.cancel(); old?.destroy()"))
        assertFalse(code.contains("MediaRecorder("))
    }
    @Test fun developmentFlavorCannotEnableGlobalCleartextTraffic() {
        assertTrue(app("main/AndroidManifest.xml").contains("android:usesCleartextTraffic=\"false\""))
        assertFalse(app("dev/AndroidManifest.xml").contains("usesCleartextTraffic=\"true\""))
    }
    @Test fun explicitUpdaterVerifiesHashPackageVersionAndCertificate() {
        val code=app("main/java/com/music/bitchord/data/AppUpdateChecker.kt")
        for (text in listOf("SHA-256 mismatch", "apk.packageName == context.packageName", "nextCode > currentCode", "installed == certs(apk)", "FLAG_DEBUGGABLE", "canRequestPackageInstalls", "ACTION_MANAGE_UNKNOWN_APP_SOURCES"))
            assertTrue(text,code.contains(text))
        // Failures before streaming (missing digest/invalid size) must also be visible, not silently idle.
        assertTrue(code.contains("if (token == generation.get()) _download.value = DownloadState.Failed"))
        assertFalse(code.contains("EXTRA_NOT_UNKNOWN_SOURCE"))
        assertFalse(code.contains("ACTION_INSTALL_PACKAGE"))
    }
    @Test fun fileProviderSharesOnlyNarrowCacheDirectories() {
        val paths=app("main/res/xml/file_paths.xml")
        assertTrue(paths.contains("path=\"updates/\""))
        assertFalse(paths.contains("<root-path"))
        assertFalse(paths.contains("<external-path"))
        assertFalse(paths.contains("path=\".\""))
    }
    @Test fun credentialBearingPreferencesAndWebviewDataAreNotAutoBackedUp() {
        for (path in listOf("main/res/xml/backup_rules.xml","main/res/xml/data_extraction_rules.xml")) {
            val rules=app(path)
            assertTrue(path,rules.contains("path=\"bitchord_settings.xml\""))
            assertTrue(path,rules.contains("path=\"app_webview/\""))
            assertTrue(path,rules.contains("path=\"bitchord_auth.xml\""))
        }
    }
}

package com.music.bitchord
import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Source architecture guards; real Android speech input must still be phone-tested. */
class AutoVoiceIntegrationGuardTest {
    private val s=File("src/main/java/com/music/bitchord/ui/components/VoiceSearch.kt").readText()
    @Test fun autoLanguageRequestDoesNotRestrictToThreeOrAnyFixedLanguages() {
        for (forbidden in listOf("bn-BD", "hi-IN", "en-IN", "EXTRA_LANGUAGE_DETECTION_ALLOWED_LANGUAGES", "EXTRA_LANGUAGE_SWITCH_ALLOWED_LANGUAGES", "FilterChip(")) assertFalse(forbidden,s.contains(forbidden))
        assertTrue(s.contains("LANGUAGE_SWITCH_HIGH_PRECISION"))
    }
    @Test fun providersThatIgnoreAutoFlagsCannotSilentlyAutoSubmit() {
        assertTrue(s.contains("onLanguageDetection"))
        assertTrue(s.contains("LANGUAGE_DETECTION_CONFIDENCE_LEVEL_CONFIDENT"))
        assertTrue(s.contains("AutoVoicePolicy.needsReview(candidate, languageVerified)"))
        assertTrue(s.contains("did not confirm automatic language recognition"))
    }
    @Test fun capabilityChecksDoNotClaimOptionalFlagsAreSupported() {
        assertTrue(s.contains("engine.checkRecognitionSupport"))
        assertTrue(s.contains("ERROR_CANNOT_CHECK_SUPPORT"))
        assertTrue(s.contains("LANGUAGE_SWITCH_RESULT_SKIPPED_NO_MODEL"))
        assertTrue(s.contains("if (begun || recognizer !== engine || !open) return"))
    }
    @Test fun pendingRequestsHaveTimeoutAndStaleCallbackProtection() {
        assertTrue(s.contains("delay(30_000L)"))
        assertTrue(s.contains("delay(4_000L)"))
        assertTrue(s.contains("watchdog?.cancel()"))
        assertTrue(s.contains("recognizer !== engine || !open"))
    }
}

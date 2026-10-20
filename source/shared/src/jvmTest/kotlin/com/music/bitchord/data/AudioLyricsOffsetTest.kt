package com.music.bitchord.data

import com.music.bitchord.data.settings.AudioLyricsOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class AudioLyricsOffsetTest {
    @Test fun freshAudioDefaultsToConfirmedCalibration() {
        assertEquals(-400, AudioLyricsOffset.initial(null, 0))
    }
    @Test fun existingMinus400IsNotDoubled() {
        assertEquals(-400, AudioLyricsOffset.initial(null, -400))
    }
    @Test fun otherNegativeManualOffsetIsPreserved() {
        assertEquals(-700, AudioLyricsOffset.initial(null, -700))
    }
    @Test fun positiveManualOffsetIsPreserved() {
        assertEquals(250, AudioLyricsOffset.initial(null, 250))
    }
    @Test fun explicitAudioZeroSurvivesRestart() {
        assertEquals(0, AudioLyricsOffset.initial(0, -400))
    }
    @Test fun savedAudioMinus400IsNotDoubled() {
        assertEquals(-400, AudioLyricsOffset.initial(-400, -400))
    }
    @Test fun audioAndLegacyVideoPreferencesRemainIndependent() {
        assertEquals(-300, AudioLyricsOffset.initial(-300, 800))
    }
    @Test fun repeatedMigrationIsIdempotent() {
        val migrated = AudioLyricsOffset.initial(null, 0)
        repeat(10) { assertEquals(-400, AudioLyricsOffset.initial(migrated, 0)) }
    }
}
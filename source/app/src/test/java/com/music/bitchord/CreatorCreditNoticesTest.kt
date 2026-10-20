package com.music.bitchord

import java.io.File
import org.junit.Assert.*
import org.junit.Test

/** Asset/source guards, not a claim of rendered-dialog or real browser testing. */
class CreatorCreditNoticesTest {
    private val ui = File("src/main/java/com/music/bitchord/ui/components/UpdateSettingsCard.kt").readText()
    private val notice = File("src/main/assets/legal/NOTICE.md").readText()

    @Test fun mainDialogCreditsMaintainerAndHisGithub() {
        assertTrue(ui.contains("Developed and maintained by Amit Hasan Sami."))
        assertTrue(ui.contains("https://github.com/amitsami\""))
        assertTrue(ui.contains("GitHub · @amitsami"))
        assertFalse(ui.contains("modified distribution"))
        assertFalse(ui.contains("kushagrasinghx"))
    }
    @Test fun sourceDownloadAndOpenSourceDisclosureRemainAccessible() {
        assertTrue(ui.contains("https://github.com/amitsami/zunevio-releases/releases"))
        assertTrue(ui.contains("showOpenSourceNotices = true"))
        assertTrue(ui.contains("Open-source notices"))
        assertTrue(ui.contains("showOpenSourceNotices = false"))
        for (name in listOf("NOTICE.md", "SOURCE.txt", "THIRD_PARTY_NOTICES.md", "LICENSE", "AGPL-3.0.txt", "Apache-2.0.txt", "Beat-This-MIT.txt", "Open-Unmix-LICENSE.txt", "LGPL-2.1.txt")) {
            assertTrue(name, ui.contains("\"$name\""))
            assertTrue(name, File("src/main/assets/legal/$name").length() > 0)
        }
    }
    @Test fun upstreamAttributionAndCopyleftTermsAreRetained() {
        assertTrue(notice.contains("Amit Hasan Sami"))
        assertTrue(notice.contains("https://github.com/amitsami"))
        assertTrue(notice.contains("BitChord"))
        assertTrue(notice.contains("https://github.com/kushagrasinghx/BitChord"))
        assertTrue(notice.contains("GNU GPL version 3"))
        assertTrue(notice.contains("AGPL-3.0-or-later"))
        assertTrue(notice.contains("There is no warranty"))
        assertEquals(File("../NOTICE.md").readText(), notice)
    }
}

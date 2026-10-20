package com.music.bitchord.ui.resources

import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.unit.Density
import java.io.File
import org.jetbrains.compose.resources.decodeToImageVector
import org.junit.Assert.*
import org.junit.Test

/** Exercises Compose XML vector decoding for shared branding resources. */
class VectorResourceRegressionTest {
    private val drawables = File("src/commonMain/composeResources/drawable")
    private fun logo() = File(drawables, "ic_logo.xml").readBytes()

    @Test fun homeLogoDecodesWithoutAndroidResourceResolution() {
        val image = logo().decodeToImageVector(Density(1f))
        assertEquals(48f, image.viewportWidth, 0f)
        assertEquals(48f, image.viewportHeight, 0f)
    }

    @Test fun homeLogoRemainsTransparentWithVisibleStroke() {
        val path = logo().decodeToImageVector(Density(1f)).root[0] as VectorPath
        assertEquals(0f, (path.fill as SolidColor).value.alpha, 0f)
        assertEquals(1f, (path.stroke as SolidColor).value.alpha, 0f)
        assertEquals(2.484375f, path.strokeLineWidth, 0f)
    }

    @Test fun everySharedVectorDecodesAtMultipleDensities() {
        val vectors = drawables.listFiles { f -> f.extension == "xml" }!!.toList()
        assertTrue("Shared vector resources must be present", vectors.isNotEmpty())
        for (file in vectors) for (density in listOf(1f, 2f, 3.5f)) {
            try { file.readBytes().decodeToImageVector(Density(density)) }
            catch (e: Exception) { throw AssertionError("Cannot decode ${file.name} at $density", e) }
        }
    }

    @Test fun oldAndroidColorReferenceReproducesReportedCrash() {
        val old = logo().toString(Charsets.UTF_8)
            .replace("#00000000", "@android:color/transparent").toByteArray()
        val error = assertThrows(IllegalArgumentException::class.java) {
            old.decodeToImageVector(Density(1f))
        }
        assertTrue(error.message!!.contains("Invalid color value @android:color/transparent"))
    }
}

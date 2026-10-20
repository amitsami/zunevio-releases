package com.music.bitchord.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.bitchord.data.settings.AppSettings
import com.music.bitchord.ui.components.LocalLiquidGlassEnabled
import com.music.bitchord.ui.components.isGlassSupported

private data class SettingsSurfaceStyle(val glass: Boolean = false, val reduceBlur: Boolean = false)
private val LocalSettingsSurfaceStyle = staticCompositionLocalOf { SettingsSurfaceStyle() }

/**
 * Settings uses cached glass-style paint, never a sampled backdrop. A scrolling
 * page with many full-width groups must not allocate a blur/shadow pass per card.
 * Only this page collects Reduce blur; individual cards receive a stable style.
 */
@Composable
internal fun SettingsGlassPage(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val reduceBlur by AppSettings.reduceDynamicBlur.collectAsStateWithLifecycle()
    val enabled = LocalLiquidGlassEnabled.current && isGlassSupported()
    val style = remember(enabled, reduceBlur) { SettingsSurfaceStyle(enabled, reduceBlur) }
    val base = if (dark) Color.Black else Color.White
    val scheme = MaterialTheme.colorScheme.copy(
        primary = if (dark) Color.White else Color.Black,
        onPrimary = if (dark) Color.Black else Color.White,
        background = base,
        onBackground = if (dark) Color.White else Color.Black,
        surface = if (dark) Color(0xFF0D0D0D) else Color(0xFFF7F7F7),
        onSurface = if (dark) Color.White else Color.Black,
        surfaceVariant = if (dark) Color(0xFF1C1C1C) else Color(0xFFF2F2F2),
        onSurfaceVariant = if (dark) Color(0xFFB0B0B0) else Color(0xFF606060),
        outline = if (dark) Color(0xFF303030) else Color(0xFFDEDEDE),
    )
    MaterialTheme(colorScheme = scheme) {
        Box(modifier = modifier.fillMaxSize()) {
            Box(
                Modifier.matchParentSize()
                    .drawWithCache {
                        val light = if (dark) Color.White else Color.Black
                        // Cached, stationary ambient light; never follows scroll or recomposition.
                        val upper = Brush.radialGradient(
                            listOf(light.copy(alpha = if (dark) 0.065f else 0.025f), Color.Transparent),
                            center = Offset(size.width * 0.05f, size.height * 0.25f),
                            radius = size.width * 0.92f,
                        )
                        val lower = Brush.radialGradient(
                            listOf(light.copy(alpha = if (dark) 0.10f else 0.045f), Color.Transparent),
                            center = Offset(size.width * 0.95f, size.height * 0.70f),
                            radius = size.width * 1.1f,
                        )
                        onDrawBehind {
                            drawRect(base)
                            if (enabled && !reduceBlur) {
                                drawRect(upper)
                                drawRect(lower)
                            }
                        }
                    },
            )
            CompositionLocalProvider(
                LocalSettingsSurfaceStyle provides style,
                androidx.compose.material3.LocalContentColor provides scheme.onBackground,
            ) {
                content()
            }
        }
    }
}

/** Opaque-backed glass-style paint: no backdrop capture, GPU blur or soft shadow. */
@Composable
internal fun Modifier.settingsGlassSurface(shape: CornerBasedShape): Modifier {
    val dark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val style = LocalSettingsSurfaceStyle.current
    val glass = style.glass
    val reduceBlur = style.reduceBlur
    val fallback = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.88f)
    // Keep brushes and modifier nodes stable while switches/sliders update.
    val paint = remember(dark, style, shape, fallback) {
        val ink = if (dark) Color.White else Color.Black
        val edge = Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = if (glass) 0.42f else 0.10f),
                ink.copy(alpha = if (glass) 0.11f else 0.045f),
            ),
        )
        val surface = when {
            !glass || reduceBlur -> Modifier.background(fallback, shape)
            else -> Modifier.background(
                Brush.linearGradient(
                    if (dark) listOf(Color(0xFF232323), Color(0xFF121212))
                    else listOf(Color(0xFFFCFCFC), Color(0xFFF0F0F0)),
                ),
                shape,
            )
        }
        Modifier.clip(shape).then(surface).border(0.65.dp, edge, shape)
    }
    return then(paint)
}

package com.music.bitchord.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.music.bitchord.ui.components.optimizedHazeEffect
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint

/**
 * Zunévio's readable lyric capsule, sampling the existing player artwork.
 * No new player, timing loop, or network provider: the upstream lyric renderer
 * and motion remain the source of truth on both Android and desktop.
 *
 * Disabling dynamic blur, an older Android device, or a missing backdrop uses
 * an opaque scrim instead. Text must remain legible without the effect.
 */
@Composable
internal fun Modifier.zunevioPlayerGlass(haze: HazeState?): Modifier {
    val reduceBlur by PlayerSettings.reduceDynamicBlur.collectAsStateWithLifecycle()
    val shape = RoundedCornerShape(20.dp)
    val base = clip(shape)
    val material = if (haze != null && !reduceBlur && renderEffectBlurSupported) {
        base.optimizedHazeEffect(
            state = haze,
            style = HazeStyle(
                backgroundColor = Color(0xFF121216),
                tints = listOf(HazeTint(Color.Black.copy(alpha = 0.55f))),
                blurRadius = 16.dp,
                noiseFactor = 0f,
                fallbackTint = HazeTint(Color(0xE6121216)),
            ),
        )
    } else {
        base.background(Color(0xE6121216))
    }
    return material.border(
        width = 0.5.dp,
        brush = Brush.verticalGradient(
            listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.08f)),
        ),
        shape = shape,
    )
}
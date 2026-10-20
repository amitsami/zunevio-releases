package com.music.bitchord.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** Opaque, theme-aware tracks and visible thumbs, without blur or a new animation. */
@Composable
fun settingsSwitchColors(): SwitchColors {
    val dark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return SwitchDefaults.colors(
        checkedThumbColor = if (dark) Color.Black else Color.White,
        checkedTrackColor = if (dark) Color.White else Color(0xFF17191C),
        checkedBorderColor = if (dark) Color.White else Color(0xFF17191C),
        uncheckedThumbColor = if (dark) Color(0xFFD4D7DD) else Color(0xFF44484F),
        uncheckedTrackColor = if (dark) Color(0xFF30343B) else Color(0xFFD8DCE2),
        uncheckedBorderColor = if (dark) Color(0xFF9299A4) else Color(0xFF707781),
        disabledCheckedThumbColor = if (dark) Color(0xFF191C21) else Color(0xFFE3E6EA),
        disabledCheckedTrackColor = Color(0xFF9299A4),
        disabledCheckedBorderColor = Color(0xFF9299A4),
        disabledUncheckedThumbColor = if (dark) Color(0xFFA4ABB6) else Color(0xFF646B76),
        disabledUncheckedTrackColor = if (dark) Color(0xFF282C33) else Color(0xFFDDE0E5),
        disabledUncheckedBorderColor = if (dark) Color(0xFF757D89) else Color(0xFF969DA7),
    )
}

package com.music.bitchord.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

object ZunevioBrand {
    val Red = Color(0xFFE43E45)
    val pattern = Regex("Zun[ée]vi[ou]", RegexOption.IGNORE_CASE)
    fun normalize(text: String): String = pattern.replace(text, "Zunéviu")
    fun contains(text: String): Boolean = pattern.containsMatchIn(text)
    fun styled(text: String): AnnotatedString = buildAnnotatedString {
        append(normalize(text))
        pattern.findAll(text).forEach {
            addStyle(SpanStyle(color = Red), it.range.first, it.range.first + 1)
        }
    }
    val wordmark: AnnotatedString = buildAnnotatedString {
        withStyle(SpanStyle(color = Red)) { append("Z") }
        append("unéviu")
    }
}

package com.music.bitchord.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/** Original, user-approved matte soft-solid glyphs. No tile, sheen or bitmap. */
object ZunevioIcons {
    private val ink = SolidColor(Color.Black)

    val Account: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Account", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M7.75 7.0a4.25 4.25 0 1 0 8.5 0a4.25 4.25 0 1 0 -8.5 0Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
            addPath(pathData = addPathNodes("M3 21c0-5.4 3.6-8.1 9-8.1s9 2.7 9 8.1a1 1 0 0 1-1 1H4a1 1 0 0 1-1-1Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val Recommendations: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Recommendations", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M10.6 3.1q.5-1.3 1 0l2.4 6.3 6.3 2.4q1.3.5 0 1l-6.3 2.4-2.4 6.3q-.5 1.3-1 0l-2.4-6.3-6.3-2.4q-1.3-.5 0-1l6.3-2.4Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
            addPath(pathData = addPathNodes("M20 1q.5 0 .7.6l.6 1.7 1.7.6q1.2.5 0 1l-1.7.6-.6 1.7q-.5 1.2-1 0l-.6-1.7-1.7-.6q-1.2-.5 0-1l1.7-.6.6-1.7q.1-.6.3-.6Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val Audioquality: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Audioquality", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M2 12a10 10 0 0 1 20 0v5.5a3.5 3.5 0 0 1-7 0v-3a3.5 3.5 0 0 1 4-3.45 7 7 0 0 0-14 0 3.5 3.5 0 0 1 4 3.45v3a3.5 3.5 0 0 1-7 0Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val Downloads: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Downloads", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M10.5 3a1.5 1.5 0 0 1 3 0v10.2l2.8-2.8a1.5 1.5 0 0 1 2.1 2.1l-5.35 5.35a1.5 1.5 0 0 1-2.1 0L5.6 12.5a1.5 1.5 0 0 1 2.1-2.1l2.8 2.8Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
            addPath(pathData = addPathNodes("M4.0 20.0H20.0Q21.5 20.0 21.5 21.5V21.5Q21.5 23.0 20.0 23.0H4.0Q2.5 23.0 2.5 21.5V21.5Q2.5 20.0 4.0 20.0Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val Playback: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Playback", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M5 4.7c0-2.1 1.8-3.1 3.6-2l12 7.1c1.9 1.1 1.9 3.3 0 4.4l-12 7.1c-1.8 1.1-3.6.1-3.6-2Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val Appearance: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Appearance", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M12 1a11 11 0 1 0 0 22 11 11 0 0 0 0-22Zm0 3v16a8 8 0 0 1 0-16Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.EvenOdd)
        }.build()
    }

    val Performance: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Performance", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M13.5 1.4c.8-1 2-.5 1.8.8l-1 7.1h5.1c1.4 0 1.9 1.1 1 2.1L10.5 22.6c-.8 1-2 .5-1.8-.8l1-7.1H4.6c-1.4 0-1.9-1.1-1-2.1Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val Localmusic: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Localmusic", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M4 3h4.5c1 0 1.5.2 2.2.9L12.8 6H20a3 3 0 0 1 3 3v11a3 3 0 0 1-3 3H4a3 3 0 0 1-3-3V6a3 3 0 0 1 3-3Zm10 6v7.1a3 3 0 1 0 2 2.8V12l3-1V8Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.EvenOdd)
        }.build()
    }

    val Storage: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Storage", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M10.5 1.8a3 3 0 0 1 3 0l8 4.5c1.8 1 1.8 2.7 0 3.7l-8 4.5a3 3 0 0 1-3 0l-8-4.5c-1.8-1-1.8-2.7 0-3.7Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
            addPath(pathData = addPathNodes("m1.8 13 8.7 4.9a3 3 0 0 0 3 0l8.7-4.9c1.2 1.2 1 2.4-.7 3.4l-8 4.5a3 3 0 0 1-3 0l-8-4.5c-1.7-1-1.9-2.2-.7-3.4Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val Yourdata: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Yourdata", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M4 8h15m-4-4 4 4-4 4M20 17H5m4-4-4 4 4 4"), fill = null, stroke = ink, strokeLineWidth = 3.2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val Miscellaneous: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Miscellaneous", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M2 6h5m6 0h9M2 18h9m6 0h5"), fill = null, stroke = ink, strokeLineWidth = 2.6f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
            addPath(pathData = addPathNodes("M7.0 6.0a3.0 3.0 0 1 0 6.0 0a3.0 3.0 0 1 0 -6.0 0Z"), fill = null, stroke = ink, strokeLineWidth = 2.6f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
            addPath(pathData = addPathNodes("M11.0 18.0a3.0 3.0 0 1 0 6.0 0a3.0 3.0 0 1 0 -6.0 0Z"), fill = null, stroke = ink, strokeLineWidth = 2.6f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val Language: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Language", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M2 5h12M8 2v3M5 5c1 5 4 8 8 10M12 5C11 10 8 13 3 15M13 22l4.5-11L22 22m-7-4h5"), fill = null, stroke = ink, strokeLineWidth = 2.6f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val Advanced: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Advanced", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("m8 5-7 7 7 7m8-14 7 7-7 7"), fill = null, stroke = ink, strokeLineWidth = 3.2f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val Explore: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Explore", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M2.3000000000000007 12.0a9.7 9.7 0 1 0 19.4 0a9.7 9.7 0 1 0 -19.4 0Z"), fill = null, stroke = ink, strokeLineWidth = 2.4f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
            addPath(pathData = addPathNodes("m16.4 7.6-2.6 6.2-6.2 2.6 2.6-6.2Z"), fill = null, stroke = ink, strokeLineWidth = 2.4f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val Settings: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Settings", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M19.10 7.90Q19.10 7.90 19.31 8.34 L19.44 8.62Q19.66 9.06 20.65 9.06 L21.29 9.05Q22.29 9.05 22.29 11.29 L22.29 12.71Q22.29 14.95 21.29 14.95 L20.65 14.94Q19.66 14.94 19.44 15.38 L19.31 15.66Q19.10 16.10 19.10 16.10 L19.10 16.10Q19.10 16.10 18.82 16.50 L18.65 16.76Q18.37 17.16 18.88 18.02 L19.19 18.57Q19.70 19.43 17.76 20.55 L16.53 21.26Q14.59 22.38 14.09 21.51 L13.78 20.97Q13.28 20.10 12.80 20.14 L12.49 20.16Q12.00 20.20 12.00 20.20 L12.00 20.20Q12.00 20.20 11.51 20.16 L11.20 20.14Q10.72 20.10 10.22 20.97 L9.91 21.51Q9.41 22.38 7.47 21.26 L6.24 20.55Q4.30 19.43 4.81 18.57 L5.12 18.02Q5.63 17.16 5.35 16.76 L5.18 16.50Q4.90 16.10 4.90 16.10 L4.90 16.10Q4.90 16.10 4.69 15.66 L4.56 15.38Q4.34 14.94 3.35 14.94 L2.71 14.95Q1.71 14.95 1.71 12.71 L1.71 11.29Q1.71 9.05 2.71 9.05 L3.35 9.06Q4.34 9.06 4.56 8.62 L4.69 8.34Q4.90 7.90 4.90 7.90 L4.90 7.90Q4.90 7.90 5.18 7.50 L5.35 7.24Q5.63 6.84 5.12 5.98 L4.81 5.43Q4.30 4.57 6.24 3.45 L7.47 2.74Q9.41 1.62 9.91 2.49 L10.22 3.03Q10.72 3.90 11.20 3.86 L11.51 3.84Q12.00 3.80 12.00 3.80 L12.00 3.80Q12.00 3.80 12.49 3.84 L12.80 3.86Q13.28 3.90 13.78 3.03 L14.09 2.49Q14.59 1.62 16.53 2.74 L17.76 3.45Q19.70 4.57 19.19 5.43 L18.88 5.98Q18.37 6.84 18.65 7.24 L18.82 7.50Q19.10 7.90 19.10 7.90Z M12 7.5a4.5 4.5 0 1 0 0 9 4.5 4.5 0 0 0 0-9Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.EvenOdd)
        }.build()
    }

    val Reset: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Reset", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M3 10a9 9 0 1 1 1 8M3 3v7h7"), fill = null, stroke = ink, strokeLineWidth = 2.8f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val WiFi: ImageVector by lazy {
        ImageVector.Builder("Zunevio.WiFi", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M2 8a16 16 0 0 1 20 0M5.5 12a10.5 10.5 0 0 1 13 0M9 16a5 5 0 0 1 6 0"), fill = null, stroke = ink, strokeLineWidth = 2.8f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
            addPath(pathData = addPathNodes("M10.5 20.0a1.5 1.5 0 1 0 3.0 0a1.5 1.5 0 1 0 -3.0 0Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val Mobile: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Mobile", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M3.2 15.0H4.8Q6.0 15.0 6.0 16.2V20.8Q6.0 22.0 4.8 22.0H3.2Q2.0 22.0 2.0 20.8V16.2Q2.0 15.0 3.2 15.0Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
            addPath(pathData = addPathNodes("M9.2 11.0H10.8Q12.0 11.0 12.0 12.2V20.8Q12.0 22.0 10.8 22.0H9.2Q8.0 22.0 8.0 20.8V12.2Q8.0 11.0 9.2 11.0Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
            addPath(pathData = addPathNodes("M15.2 6.0H16.8Q18.0 6.0 18.0 7.2V20.8Q18.0 22.0 16.8 22.0H15.2Q14.0 22.0 14.0 20.8V7.2Q14.0 6.0 15.2 6.0Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
            addPath(pathData = addPathNodes("M21.2 2.0H21.8Q23.0 2.0 23.0 3.2V20.8Q23.0 22.0 21.8 22.0H21.2Q20.0 22.0 20.0 20.8V3.2Q20.0 2.0 21.2 2.0Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val Blur: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Blur", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M3.0 12.0a9.0 9.0 0 1 0 18.0 0a9.0 9.0 0 1 0 -18.0 0Z"), fill = null, stroke = ink, strokeLineWidth = 2.8f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
            addPath(pathData = addPathNodes("M8.0 12.0a4.0 4.0 0 1 0 8.0 0a4.0 4.0 0 1 0 -8.0 0Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val Motion: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Motion", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M2 7h5M1 12h4M2 17h5"), fill = null, stroke = ink, strokeLineWidth = 2.8f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
            addPath(pathData = addPathNodes("M8.0 12.0a7.0 7.0 0 1 0 14.0 0a7.0 7.0 0 1 0 -14.0 0Z"), fill = null, stroke = ink, strokeLineWidth = 2.8f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
            addPath(pathData = addPathNodes("m12 9 6 6"), fill = null, stroke = ink, strokeLineWidth = 2.8f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val Lyrics: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Lyrics", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M4.5 3.0H19.5Q21.0 3.0 21.0 4.5V4.5Q21.0 6.0 19.5 6.0H4.5Q3.0 6.0 3.0 4.5V4.5Q3.0 3.0 4.5 3.0Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
            addPath(pathData = addPathNodes("M4.5 8.0H15.5Q17.0 8.0 17.0 9.5V9.5Q17.0 11.0 15.5 11.0H4.5Q3.0 11.0 3.0 9.5V9.5Q3.0 8.0 4.5 8.0Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
            addPath(pathData = addPathNodes("M4.5 13.0H19.5Q21.0 13.0 21.0 14.5V14.5Q21.0 16.0 19.5 16.0H4.5Q3.0 16.0 3.0 14.5V14.5Q3.0 13.0 4.5 13.0Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
            addPath(pathData = addPathNodes("M4.5 18.0H12.5Q14.0 18.0 14.0 19.5V19.5Q14.0 21.0 12.5 21.0H4.5Q3.0 21.0 3.0 19.5V19.5Q3.0 18.0 4.5 18.0Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val Cover: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Cover", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M6 2h12a4 4 0 0 1 4 4v12a4 4 0 0 1-4 4H6a4 4 0 0 1-4-4V6a4 4 0 0 1 4-4Zm0 14 4-4 3 3 4-6 3 9H6Zm2-8a2 2 0 1 0 0-4 2 2 0 0 0 0 4Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.EvenOdd)
        }.build()
    }

    val Source: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Source", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M10 3a3 3 0 0 1 6 0v2h3a2 2 0 0 1 2 2v3h-2a3 3 0 0 0 0 6h2v3a2 2 0 0 1-2 2h-3v-2a3 3 0 0 0-6 0v2H7a2 2 0 0 1-2-2v-3H3a3 3 0 0 1 0-6h2V7a2 2 0 0 1 2-2h3Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val Equalizer: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Equalizer", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M4 9v6M9 4v16M15 7v10M20 10v4"), fill = null, stroke = ink, strokeLineWidth = 3.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val Volume: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Volume", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M2 9h4l6-6v18l-6-6H2a1 1 0 0 1-1-1v-4a1 1 0 0 1 1-1Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
            addPath(pathData = addPathNodes("M16 8q4 4 0 8M20 4q7 8 0 16"), fill = null, stroke = ink, strokeLineWidth = 2.5f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val Hidden: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Hidden", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M1 12q11-17 22 0-11 17-22 0Zm11-5a5 5 0 1 0 0 10 5 5 0 0 0 0-10Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.EvenOdd)
            addPath(pathData = addPathNodes("M10.0 12.0a2.0 2.0 0 1 0 4.0 0a2.0 2.0 0 1 0 -4.0 0Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
            addPath(pathData = addPathNodes("m3 2 19 19"), fill = null, stroke = ink, strokeLineWidth = 2.5f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val Delete: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Delete", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M8 1h8v3h5v3H3V4h5ZM5 9h14l-1 12a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val Upload: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Upload", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M12 18V2m-5 5 5-5 5 5M3 18v4h18v-4"), fill = null, stroke = ink, strokeLineWidth = 2.8f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val Filter: ImageVector by lazy {
        ImageVector.Builder("Zunevio.Filter", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M2 2h20a1 1 0 0 1 .7 1.7L15 12v8l-6 3V12L1.3 3.7A1 1 0 0 1 2 2Z"), fill = ink, stroke = null, strokeLineWidth = 0.0f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    val History: ImageVector by lazy {
        ImageVector.Builder("Zunevio.History", 24.dp, 24.dp, 24f, 24f).apply {
            addPath(pathData = addPathNodes("M3 10a9 9 0 1 1 1 8M3 3v7h7M12 7v6l4 2"), fill = null, stroke = ink, strokeLineWidth = 2.8f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round, pathFillType = PathFillType.NonZero)
        }.build()
    }

    /** Pure name lookup, not reflection; no row identity or click behavior is changed. */
    fun forSettings(original: ImageVector): ImageVector = when (original.name.substringAfterLast('.')) {
        "Person" -> Account
        "Refresh" -> Reset
        "AutoAwesome" -> Recommendations
        "Brightness4" -> Appearance
        "MotionPhotosOff" -> Motion
        "BlurOff" -> Blur
        "BlurOn" -> Blur
        "Fullscreen" -> Cover
        "Gradient" -> Cover
        "Animation" -> Motion
        "Notes" -> Lyrics
        "Language" -> Language
        "Translate" -> Language
        "Tune" -> Miscellaneous
        "GraphicEq" -> Equalizer
        "SurroundSound" -> Audioquality
        "Waves" -> Audioquality
        "Wifi" -> WiFi
        "SignalCellularAlt" -> Mobile
        "Download" -> Downloads
        "FileDownload" -> Downloads
        "FileUpload" -> Upload
        "Folder" -> Localmusic
        "LibraryMusic" -> Localmusic
        "Cached" -> Storage
        "Storage" -> Storage
        "DeleteSweep" -> Delete
        "FilterAlt" -> Filter
        "BarChart" -> Performance
        "LocalOffer" -> Recommendations
        "PlaylistPlay" -> Playback
        "History" -> History
        "MusicOff" -> Audioquality
        "VolumeOff" -> Volume
        "VolumeUp" -> Volume
        "VisibilityOff" -> Hidden
        "Extension" -> Source
        "Cloud" -> Source
        "Dns" -> Source
        "SmartDisplay" -> Cover
        "Groups" -> Account
        "Check" -> Playback
        else -> original
    }
}

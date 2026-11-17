package com.cs364.mooddiary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * ชุดสีที่ทุกหน้าจอใช้ — เรียกผ่าน MoodTheme.colors.xxx
 * มี 2 ชุด: DayColors และ NightColors (ข้อ 3.4)
 */
@Immutable
data class MoodColors(
    val isNight: Boolean,
    val background: Color,   // พื้นลายไม้
    val grain: Color,        // ลายเสี้ยนไม้
    val plank: Color,        // ไม้เข้ม
    val onPlank: Color,
    val card: Color,         // การ์ดกระดาษ
    val crate: Color,        // ด้านในลังไม้
    val line: Color,         // ขอบการ์ด + เงาทึบ
    val edge: Color,         // ขอบของชิ้นไม้
    val text: Color,
    val textSub: Color,
    val dash: Color,
    val accent: Color,       // ชมพูซากุระ
    val onAccent: Color,
    val primary: Color,      // ปุ่มบันทึก (มัทฉะ)
    val onPrimary: Color,
    val danger: Color,       // ปุ่มลบ
    val onDanger: Color,
    val dangerText: Color,   // ข้อความสีแดงบนการ์ด
    val success: Color,
    val scrim: Color,
)

val DayColors = MoodColors(
    isNight = false,
    background = WoodPalette.Hinoki,
    grain = Color(0x12965F2D),
    plank = WoodPalette.Plank,
    onPlank = WoodPalette.Cream,
    card = WoodPalette.Washi,
    crate = WoodPalette.Crate,
    line = WoodPalette.Line,
    edge = WoodPalette.Bark,
    text = WoodPalette.Bark,
    textSub = WoodPalette.Sub,
    dash = WoodPalette.Dash,
    accent = WoodPalette.Sakura,
    onAccent = WoodPalette.Ink,
    primary = WoodPalette.Matcha,
    onPrimary = WoodPalette.Ink,
    danger = WoodPalette.Coral,
    onDanger = WoodPalette.Ink,
    dangerText = Color(0xFFA8322A),
    success = Color(0xFF3F6B22),
    scrim = Color(0x733C230F),
)

val NightColors = MoodColors(
    isNight = true,
    background = WoodPalette.HinokiNight,
    grain = Color(0x0AFFD7AA),
    plank = WoodPalette.PlankNight,
    onPlank = WoodPalette.Cream,
    card = WoodPalette.WashiNight,
    crate = WoodPalette.CrateNight,
    line = WoodPalette.LineNight,
    edge = WoodPalette.EdgeNight,
    text = WoodPalette.TextNight,
    textSub = WoodPalette.SubNight,
    dash = WoodPalette.DashNight,
    accent = WoodPalette.Sakura,
    onAccent = WoodPalette.Ink,
    primary = WoodPalette.Matcha,
    onPrimary = WoodPalette.Ink,
    danger = WoodPalette.Coral,
    onDanger = WoodPalette.Ink,
    dangerText = Color(0xFFFFA59A),
    success = Color(0xFFB5DE8C),
    scrim = Color(0x8C1E1208),
)

/** ขนาดมาตรฐานของธีม: ขอบหนา มุมโค้ง เงาทึบ */
object MoodDimens {
    val border: Dp = 3.dp
    val shadow: Dp = 4.dp
    val radiusCard: Dp = 22.dp
    val radiusButton: Dp = 18.dp
    val radiusSmall: Dp = 14.dp
    val touch: Dp = 48.dp
}

val LocalMoodColors = staticCompositionLocalOf { DayColors }

object MoodTheme {
    val colors: MoodColors
        @Composable get() = LocalMoodColors.current
}

@Composable
fun MoodDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) NightColors else DayColors
    // map ไปที่ Material3 ด้วย เผื่อใช้คอมโพเนนต์มาตรฐาน (TextField, Switch ฯลฯ)
    val scheme = if (darkTheme) {
        darkColorScheme(
            primary = colors.primary, onPrimary = colors.onPrimary,
            secondary = colors.accent, onSecondary = colors.onAccent,
            background = colors.background, onBackground = colors.text,
            surface = colors.card, onSurface = colors.text,
            surfaceVariant = colors.crate, onSurfaceVariant = colors.textSub,
            error = colors.danger, onError = colors.onDanger, outline = colors.line,
        )
    } else {
        lightColorScheme(
            primary = colors.primary, onPrimary = colors.onPrimary,
            secondary = colors.accent, onSecondary = colors.onAccent,
            background = colors.background, onBackground = colors.text,
            surface = colors.card, onSurface = colors.text,
            surfaceVariant = colors.crate, onSurfaceVariant = colors.textSub,
            error = colors.danger, onError = colors.onDanger, outline = colors.line,
        )
    }
    CompositionLocalProvider(LocalMoodColors provides colors) {
        MaterialTheme(colorScheme = scheme, typography = MoodTypography, content = content)
    }
}

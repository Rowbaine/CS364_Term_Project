package com.cs364.mooddiary.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.cs364.mooddiary.R

/** หัวข้อ: Mitr (กลม หนา อ่านง่าย) */
val Mitr = FontFamily(
    Font(R.font.mitr_regular, FontWeight.Normal),
    Font(R.font.mitr_medium, FontWeight.Medium),
)

/** เนื้อหา: Mali (ลายมือน่ารัก เหมือนเขียนสมุดจริง) */
val Mali = FontFamily(
    Font(R.font.mali_regular, FontWeight.Normal),
    Font(R.font.mali_semibold, FontWeight.SemiBold),
    Font(R.font.mali_bold, FontWeight.Bold),
)

val MoodTypography = Typography(
    headlineSmall = TextStyle(fontFamily = Mitr, fontWeight = FontWeight.Medium, fontSize = 26.sp, lineHeight = 34.sp),
    titleLarge = TextStyle(fontFamily = Mitr, fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 30.sp),
    titleMedium = TextStyle(fontFamily = Mitr, fontWeight = FontWeight.Medium, fontSize = 18.sp, lineHeight = 26.sp),
    titleSmall = TextStyle(fontFamily = Mitr, fontWeight = FontWeight.Medium, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = Mali, fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 28.sp),
    bodyMedium = TextStyle(fontFamily = Mali, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontFamily = Mali, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = Mali, fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Mali, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontFamily = Mali, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, lineHeight = 16.sp),
)

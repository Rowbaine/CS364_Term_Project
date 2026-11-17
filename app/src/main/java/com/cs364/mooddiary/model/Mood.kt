package com.cs364.mooddiary.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.cs364.mooddiary.R
import com.cs364.mooddiary.ui.theme.WoodPalette

/**
 * หน้าตาของตัวละครผลไม้ — path ใช้ระบบพิกัด 100×100
 * (ตัวกลมอยู่ที่ (50,56) รัศมี 38 เหมือนในไฟล์ออกแบบ)
 */
data class FaceSpec(
    val color: Color,
    val eyeL: String,
    val eyeR: String,
    val mouth: String,
    val dotEyes: Boolean,
)

private const val DOT_L = "M37 55 L37 55.1"
private const val DOT_R = "M63 55 L63 55.1"

enum class Mood(
    val key: String,
    @StringRes val label: Int,
    @StringRes val hint: Int,
    val face: FaceSpec,
) {
    HAPPY("happy", R.string.mood_happy, R.string.hint_happy,
        FaceSpec(WoodPalette.Peach, "M31 55 Q37 48 43 55", "M57 55 Q63 48 69 55", "M42 66 Q50 76 58 66", false)),
    CALM("calm", R.string.mood_calm, R.string.hint_calm,
        FaceSpec(WoodPalette.Orange, DOT_L, DOT_R, "M44 66 Q50 72 56 66", true)),
    MEH("meh", R.string.mood_meh, R.string.hint_meh,
        FaceSpec(WoodPalette.Melon, DOT_L, DOT_R, "M43 68 L57 68", true)),
    SAD("sad", R.string.mood_sad, R.string.hint_sad,
        FaceSpec(WoodPalette.Blueberry, "M37 57 L37 57.1", "M63 57 L63 57.1", "M43 71 Q50 64 57 71", true)),
    ANGRY("angry", R.string.mood_angry, R.string.hint_angry,
        FaceSpec(WoodPalette.Apple, "M31 50 L42 55", "M69 50 L58 55", "M43 71 Q50 65 57 71", false)),
    TIRED("tired", R.string.mood_tired, R.string.hint_tired,
        FaceSpec(WoodPalette.Coconut, "M32 56 L42 56", "M58 56 L68 56", "M46 68 Q50 71 54 68", false));

    companion object {
        fun fromKey(key: String?): Mood = entries.firstOrNull { it.key == key } ?: HAPPY
    }
}

/** หน้าพิเศษสำหรับ Empty state และหน้าต่างยืนยัน */
object SpecialFaces {
    val Sleepy = FaceSpec(WoodPalette.Melon, "M38 58 Q44 63 50 58", "M62 58 Q68 63 74 58", "M52 70 Q56 73 60 70", false)
    val Worried = FaceSpec(WoodPalette.Blueberry, "M31 53 Q37 59 43 53", "M57 53 Q63 59 69 53", "M43 71 Q50 64 57 71", false)
    val Nervous = FaceSpec(WoodPalette.Orange, "M37 51 L37 58", "M63 51 L63 58", "M45 69 Q50 64 55 69 Q50 74 45 69", false)
}

data class DiaryEntry(
    val id: Long,
    val mood: Mood,
    val text: String,
    val createdAt: Long,
    val updatedAt: Long,
)

/** ร่างบันทึกที่ยังไม่ได้กดบันทึก (ข้อ 4.4) */
data class Draft(
    val mood: Mood,
    val text: String,
    val entryId: Long?,   // null = บันทึกใหม่, มีค่า = กำลังแก้ไขบันทึกเดิม
    val updatedAt: Long,
)

const val MAX_NOTE_LENGTH = 200

package com.cs364.mooddiary.data

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * ข้อ 3.3 (ภาษา) และ 3.4 (ธีม Day/Night)
 * - ภาษา: ใช้ per-app language ของ AppCompat (Android จำค่าให้เอง)
 * - ธีม: เก็บใน SharedPreferences แล้วตั้งผ่าน AppCompatDelegate ตอนเปิดแอป
 */
class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var nightMode: Int
        get() = prefs.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        set(value) { prefs.edit().putInt("night_mode", value).apply() }

    var reminder: Boolean
        get() = prefs.getBoolean("reminder", true)
        set(value) { prefs.edit().putBoolean("reminder", value).apply() }

    /** เวลาเตือน (นาทีนับจากเที่ยงคืน) ค่าเริ่มต้น 20:00 */
    var reminderMinutes: Int
        get() = prefs.getInt("reminder_minutes", 20 * 60)
        set(value) { prefs.edit().putInt("reminder_minutes", value).apply() }

    /** วันที่เตือน 1=จันทร์ … 7=อาทิตย์ (ค่าเริ่มต้นทุกวัน) */
    var reminderDays: Set<Int>
        get() = prefs.getString("reminder_days", "1234567")!!.mapNotNull { it.digitToIntOrNull() }.toSet()
        set(value) { prefs.edit().putString("reminder_days", value.sorted().joinToString("")).apply() }

    fun applyNightMode() = AppCompatDelegate.setDefaultNightMode(nightMode)

    fun setNight(night: Boolean) {
        nightMode = if (night) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        applyNightMode()
    }

    companion object {
        /** "th" หรือ "en" (ถ้ายังไม่เคยเลือก ใช้ภาษาของเครื่อง) */
        fun currentLanguage(): String {
            val tags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
            val lang = if (tags.isNotEmpty()) tags else java.util.Locale.getDefault().toLanguageTag()
            return if (lang.startsWith("th")) "th" else "en"
        }

        fun setLanguage(tag: String) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))
        }
    }
}

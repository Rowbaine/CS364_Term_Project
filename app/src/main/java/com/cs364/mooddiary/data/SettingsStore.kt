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

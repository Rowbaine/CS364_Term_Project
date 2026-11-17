package com.cs364.mooddiary

import android.app.Application
import com.cs364.mooddiary.data.DiaryRepository
import com.cs364.mooddiary.data.DraftStore
import com.cs364.mooddiary.data.SettingsStore

class MoodDiaryApp : Application() {
    lateinit var repository: DiaryRepository
        private set
    lateinit var drafts: DraftStore
        private set
    lateinit var settings: SettingsStore
        private set

    override fun onCreate() {
        super.onCreate()
        repository = DiaryRepository(this)
        drafts = DraftStore(this)
        settings = SettingsStore(this)
        settings.applyNightMode()   // ใช้ธีมที่ผู้ใช้เลือกไว้ตั้งแต่เปิดแอป
    }
}

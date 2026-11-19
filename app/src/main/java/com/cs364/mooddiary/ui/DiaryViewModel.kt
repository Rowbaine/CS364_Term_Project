package com.cs364.mooddiary.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.cs364.mooddiary.MoodDiaryApp
import com.cs364.mooddiary.model.DiaryEntry
import com.cs364.mooddiary.model.Draft
import com.cs364.mooddiary.model.Mood
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

class DiaryViewModel(app: Application) : AndroidViewModel(app) {
    private val moodApp = app as MoodDiaryApp
    private val repo = moodApp.repository
    private val drafts = moodApp.drafts
    val settings = moodApp.settings

    val entries: StateFlow<List<DiaryEntry>> = repo.entries
    val draft: StateFlow<Draft?> = drafts.draft

    fun entry(id: Long): DiaryEntry? = repo.get(id)

    /** [day] = วันที่ต้องการบันทึกย้อนหลัง (null = วันนี้) */
    fun save(mood: Mood, text: String, entryId: Long?, day: LocalDate? = null) {
        if (entryId == null) {
            val at = day?.takeIf { it != LocalDate.now() }
                ?.atTime(LocalTime.now())?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
            repo.add(mood, text, at)
        } else repo.update(entryId, mood, text)
        drafts.clear()
    }

    fun delete(id: Long) = repo.delete(id)
    fun deleteAll() { repo.clear(); drafts.clear() }

    fun saveDraft(mood: Mood, text: String, entryId: Long?) = drafts.save(mood, text, entryId)
    fun clearDraft() = drafts.clear()
}

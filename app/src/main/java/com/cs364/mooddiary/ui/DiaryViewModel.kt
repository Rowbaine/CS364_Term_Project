package com.cs364.mooddiary.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.cs364.mooddiary.MoodDiaryApp
import com.cs364.mooddiary.model.DiaryEntry
import com.cs364.mooddiary.model.Draft
import com.cs364.mooddiary.model.Mood
import kotlinx.coroutines.flow.StateFlow

class DiaryViewModel(app: Application) : AndroidViewModel(app) {
    private val moodApp = app as MoodDiaryApp
    private val repo = moodApp.repository
    private val drafts = moodApp.drafts
    val settings = moodApp.settings

    val entries: StateFlow<List<DiaryEntry>> = repo.entries
    val draft: StateFlow<Draft?> = drafts.draft

    fun entry(id: Long): DiaryEntry? = repo.get(id)

    fun save(mood: Mood, text: String, entryId: Long?) {
        if (entryId == null) repo.add(mood, text) else repo.update(entryId, mood, text)
        drafts.clear()
    }

    fun delete(id: Long) = repo.delete(id)
    fun deleteAll() { repo.clear(); drafts.clear() }

    fun saveDraft(mood: Mood, text: String, entryId: Long?) = drafts.save(mood, text, entryId)
    fun clearDraft() = drafts.clear()
}

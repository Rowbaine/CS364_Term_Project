package com.cs364.mooddiary.data

import android.content.Context
import com.cs364.mooddiary.model.DiaryEntry
import com.cs364.mooddiary.model.Draft
import com.cs364.mooddiary.model.Mood
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

/**
 * เก็บบันทึกแบบง่ายใน SharedPreferences (JSON)
 * ถ้าทีมจะเปลี่ยนเป็น Room ให้คงฟังก์ชันเดิม (add / update / delete / clear / entries)
 * หน้าจอทุกหน้าจะใช้ต่อได้ทันทีโดยไม่ต้องแก้ UI
 */
class DiaryRepository(context: Context) {
    private val prefs = context.getSharedPreferences("diary_entries", Context.MODE_PRIVATE)
    private val _entries = MutableStateFlow(load())
    val entries: StateFlow<List<DiaryEntry>> = _entries.asStateFlow()

    fun get(id: Long): DiaryEntry? = _entries.value.firstOrNull { it.id == id }

    fun add(mood: Mood, text: String): DiaryEntry {
        val now = System.currentTimeMillis()
        val entry = DiaryEntry(id = now, mood = mood, text = text.trim(), createdAt = now, updatedAt = now)
        commit(listOf(entry) + _entries.value)
        return entry
    }

    fun update(id: Long, mood: Mood, text: String) {
        val now = System.currentTimeMillis()
        commit(_entries.value.map { if (it.id == id) it.copy(mood = mood, text = text.trim(), updatedAt = now) else it })
    }

    fun delete(id: Long) = commit(_entries.value.filterNot { it.id == id })

    fun clear() = commit(emptyList())

    private fun commit(list: List<DiaryEntry>) {
        val sorted = list.sortedByDescending { it.createdAt }
        _entries.value = sorted
        val arr = JSONArray()
        sorted.forEach {
            arr.put(JSONObject()
                .put("id", it.id).put("mood", it.mood.key).put("text", it.text)
                .put("createdAt", it.createdAt).put("updatedAt", it.updatedAt))
        }
        prefs.edit().putString(KEY, arr.toString()).apply()
    }

    private fun load(): List<DiaryEntry> {
        val raw = prefs.getString(KEY, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                DiaryEntry(
                    id = o.getLong("id"),
                    mood = Mood.fromKey(o.optString("mood")),
                    text = o.optString("text"),
                    createdAt = o.getLong("createdAt"),
                    updatedAt = o.optLong("updatedAt", o.getLong("createdAt")),
                )
            }.sortedByDescending { it.createdAt }
        }.getOrDefault(emptyList())
    }

    private companion object { const val KEY = "entries" }
}

/**
 * ข้อ 4.4: เก็บร่างทันทีที่พิมพ์ และตอนแอปถูกสลับไปทำงานเบื้องหลัง
 * กลับมาเปิดแอปใหม่ก็เขียนต่อจากเดิมได้
 */
class DraftStore(context: Context) {
    private val prefs = context.getSharedPreferences("diary_draft", Context.MODE_PRIVATE)
    private val _draft = MutableStateFlow(load())
    val draft: StateFlow<Draft?> = _draft.asStateFlow()

    fun save(mood: Mood, text: String, entryId: Long?) {
        if (text.isBlank()) { clear(); return }
        val d = Draft(mood, text, entryId, System.currentTimeMillis())
        _draft.value = d
        prefs.edit()
            .putString("mood", mood.key).putString("text", text)
            .putLong("entryId", entryId ?: -1L).putLong("updatedAt", d.updatedAt)
            .apply()
    }

    fun clear() {
        _draft.value = null
        prefs.edit().clear().apply()
    }

    private fun load(): Draft? {
        val text = prefs.getString("text", null) ?: return null
        val id = prefs.getLong("entryId", -1L)
        return Draft(
            mood = Mood.fromKey(prefs.getString("mood", null)),
            text = text,
            entryId = if (id == -1L) null else id,
            updatedAt = prefs.getLong("updatedAt", 0L),
        )
    }
}

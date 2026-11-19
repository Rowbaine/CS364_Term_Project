package com.cs364.mooddiary.data

import android.content.Context
import com.cs364.mooddiary.model.DiaryEntry
import com.cs364.mooddiary.model.Draft
import com.cs364.mooddiary.model.Mood
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.time.LocalDate
import java.time.ZoneId

/**
 * ตัวกลางระหว่าง ViewModel กับฐานข้อมูล Room (เก็บในเครื่อง)
 *
 * ฟังก์ชันสาธารณะชุดเดิม (entries / get / add / update / delete / clear) ยังเรียกแบบปกติได้
 * ViewModel และ UI จึงไม่ต้องแก้: ค่าใน [entries] อัปเดตทันที แล้วจึงเขียนลง Room เบื้องหลัง
 * จากนั้น Flow ของ Room จะส่งข้อมูลจริงกลับมาซ้ำอีกครั้ง
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DiaryRepository(
    context: Context,
    private val dao: DiaryDao = DiaryDatabase.get(context).diaryDao(),
) {
    private val legacyPrefs = context.getSharedPreferences("diary_entries", Context.MODE_PRIVATE)

    // limitedParallelism(1) = งานเขียนทำทีละงานตามลำดับที่เรียก (เพิ่มแล้วลบทันทีจะไม่สลับกัน)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    private val _entries = MutableStateFlow(loadLegacy())
    val entries: StateFlow<List<DiaryEntry>> = _entries.asStateFlow()

    init {
        scope.launch {
            migrateFromPrefs()
            dao.observeAll().collect { list -> _entries.value = list.map { it.toModel() } }
        }
    }

    fun get(id: Long): DiaryEntry? = _entries.value.firstOrNull { it.id == id }

    /** ดึงบันทึกของวันที่กำหนด (ตามเขตเวลาของเครื่อง) */
    fun entriesForDay(date: LocalDate, zone: ZoneId = ZoneId.systemDefault()): Flow<List<DiaryEntry>> {
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return dao.observeBetween(start, end).map { list -> list.map { it.toModel() } }
    }

    /** [createdAt] ใส่เวลาเองได้ (ใช้ตอนเขียนย้อนหลังจากหน้าปฏิทิน) ถ้าไม่ใส่ = ตอนนี้ */
    fun add(mood: Mood, text: String, createdAt: Long? = null): DiaryEntry {
        val now = System.currentTimeMillis()
        val entry = DiaryEntry(id = now, mood = mood, text = text.trim(), createdAt = createdAt ?: now, updatedAt = now)
        _entries.update { (listOf(entry) + it).sortedByDescending { e -> e.createdAt } }
        scope.launch { dao.insert(entry.toEntity()) }
        return entry
    }

    fun update(id: Long, mood: Mood, text: String) {
        val old = get(id) ?: return
        val updated = old.copy(mood = mood, text = text.trim(), updatedAt = System.currentTimeMillis())
        _entries.update { list -> list.map { if (it.id == id) updated else it } }
        scope.launch { dao.update(updated.toEntity()) }
    }

    fun delete(id: Long) {
        _entries.update { list -> list.filterNot { it.id == id } }
        scope.launch { dao.deleteById(id) }
    }

    fun clear() {
        _entries.value = emptyList()
        scope.launch { dao.deleteAll() }
    }

    /** ย้ายบันทึกเก่าจาก SharedPreferences เข้า Room ครั้งเดียว แล้วลบของเก่าทิ้ง */
    private suspend fun migrateFromPrefs() {
        val old = loadLegacy()
        if (old.isNotEmpty()) dao.insertAll(old.map { it.toEntity() })
        if (legacyPrefs.contains(KEY)) legacyPrefs.edit().remove(KEY).commit()
    }

    private fun loadLegacy(): List<DiaryEntry> {
        val raw = legacyPrefs.getString(KEY, null) ?: return emptyList()
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

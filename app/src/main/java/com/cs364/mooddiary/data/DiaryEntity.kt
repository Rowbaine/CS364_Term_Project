package com.cs364.mooddiary.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.cs364.mooddiary.model.DiaryEntry
import com.cs364.mooddiary.model.Mood

/**
 * ตาราง diary_entries ในฐานข้อมูล Room
 * - created_at  = วันเวลาที่เขียน (ใช้ดึงตามวัน จึงทำ index ไว้)
 * - mood        = key ของอีโมจิ เช่น "happy" (เก็บเป็นข้อความ เพื่อไม่ผูกกับลำดับของ enum)
 * - text        = ข้อความบันทึก
 */
@Entity(
    tableName = "diary_entries",
    indices = [Index(value = ["created_at"])],
)
data class DiaryEntity(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "mood") val moodKey: String,
    @ColumnInfo(name = "text") val text: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)

fun DiaryEntity.toModel(): DiaryEntry = DiaryEntry(
    id = id,
    mood = Mood.fromKey(moodKey),
    text = text,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun DiaryEntry.toEntity(): DiaryEntity = DiaryEntity(
    id = id,
    moodKey = mood.key,
    text = text,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

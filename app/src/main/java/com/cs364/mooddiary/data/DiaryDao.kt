package com.cs364.mooddiary.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    /** ทุกบันทึก เรียงจากใหม่ไปเก่า — Flow จะส่งค่าใหม่ทุกครั้งที่ตารางเปลี่ยน */
    @Query("SELECT * FROM diary_entries ORDER BY created_at DESC")
    fun observeAll(): Flow<List<DiaryEntity>>

    /** ดึงตามวัน: ส่งช่วงเวลา [startMillis, endMillis) ของวันนั้น */
    @Query(
        "SELECT * FROM diary_entries " +
            "WHERE created_at >= :startMillis AND created_at < :endMillis " +
            "ORDER BY created_at DESC"
    )
    fun observeBetween(startMillis: Long, endMillis: Long): Flow<List<DiaryEntity>>

    @Query("SELECT * FROM diary_entries WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): DiaryEntity?

    @Query("SELECT COUNT(*) FROM diary_entries")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: DiaryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<DiaryEntity>)

    @Update
    suspend fun update(entry: DiaryEntity)

    @Delete
    suspend fun delete(entry: DiaryEntity)

    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM diary_entries")
    suspend fun deleteAll()
}

package com.cs364.mooddiary.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/** ไฟล์ฐานข้อมูลเก็บในเครื่อง: /data/data/com.cs364.mooddiary/databases/mood_diary.db */
@Database(entities = [DiaryEntity::class], version = 1, exportSchema = false)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao

    companion object {
        @Volatile private var instance: DiaryDatabase? = null

        fun get(context: Context): DiaryDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DiaryDatabase::class.java,
                    "mood_diary.db",
                ).build().also { instance = it }
            }
    }
}

package com.cs364.mooddiary.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.cs364.mooddiary.data.DiaryDatabase
import com.cs364.mooddiary.data.SettingsStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/**
 * ถูกเรียกเมื่อถึงเวลาเตือน:
 * - ถ้าวันนี้เขียนบันทึกไปแล้ว → ไม่รบกวน
 * - จากนั้นตั้งปลุกรอบถัดไปทันที
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext
        val pending = goAsync()   // ให้เวลาอ่านฐานข้อมูลได้โดยระบบไม่ปิด receiver ก่อน
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (SettingsStore(app).reminder && !hasEntryToday(app)) {
                    ReminderNotifier.show(app)
                }
            } finally {
                ReminderScheduler.sync(app)
                pending.finish()
            }
        }
    }

    private suspend fun hasEntryToday(context: Context): Boolean {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val start = today.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = today.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return DiaryDatabase.get(context).diaryDao().observeBetween(start, end).first().isNotEmpty()
    }

    companion object {
        const val ACTION_REMIND = "com.cs364.mooddiary.action.REMIND"
    }
}

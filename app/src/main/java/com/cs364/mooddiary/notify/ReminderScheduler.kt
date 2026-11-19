package com.cs364.mooddiary.notify

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.cs364.mooddiary.data.SettingsStore
import java.time.ZonedDateTime

/**
 * ตั้งเวลาเตือนให้เขียนบันทึก (ฟีเจอร์เสริมของคนที่ 4)
 *
 * ใช้ AlarmManager แบบ "ไม่เจาะจงเวลาเป๊ะ" ([AlarmManager.setAndAllowWhileIdle])
 * จึงไม่ต้องขอสิทธิ์ SCHEDULE_EXACT_ALARM — เวลาจริงอาจคลาดเคลื่อนได้ไม่กี่นาทีเมื่อเครื่องอยู่ในโหมดประหยัดพลังงาน
 *
 * วิธีทำงาน: ตั้งปลุกครั้งเดียวสำหรับ "รอบถัดไป" ทีละรอบ
 * พอ [ReminderReceiver] ทำงานเสร็จก็เรียก [sync] เพื่อตั้งรอบต่อไปเอง
 */
object ReminderScheduler {
    private const val REQUEST_CODE = 4001

    /**
     * หาเวลาเตือนรอบถัดไปหลังเวลา [now]
     * @param minutes นาทีนับจากเที่ยงคืน (เช่น 20:00 = 1200)
     * @param days วันที่เตือน 1=จันทร์ … 7=อาทิตย์
     * @return null ถ้าไม่ได้เลือกวันเลย
     */
    fun nextTrigger(now: ZonedDateTime, minutes: Int, days: Set<Int>): ZonedDateTime? {
        if (days.isEmpty()) return null
        for (offset in 0L..7L) {
            val date = now.toLocalDate().plusDays(offset)
            if (date.dayOfWeek.value !in days) continue
            val at = date.atTime((minutes / 60) % 24, minutes % 60).atZone(now.zone)
            if (at.isAfter(now)) return at
        }
        return null
    }

    /** อ่านค่าตั้งค่าล่าสุด แล้วตั้ง (หรือยกเลิก) การปลุกให้ตรงกัน — เรียกได้ซ้ำโดยไม่เกิดปลุกซ้อน */
    fun sync(context: Context) {
        val app = context.applicationContext
        val settings = SettingsStore(app)
        val next = if (settings.reminder) {
            nextTrigger(ZonedDateTime.now(), settings.reminderMinutes, settings.reminderDays)
        } else null

        val alarmManager = app.getSystemService(AlarmManager::class.java)
        if (next == null) {
            alarmManager.cancel(pendingIntent(app))
        } else {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                next.toInstant().toEpochMilli(),
                pendingIntent(app),
            )
        }
    }

    private fun pendingIntent(context: Context): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            Intent(context, ReminderReceiver::class.java).setAction(ReminderReceiver.ACTION_REMIND),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
}

package com.cs364.mooddiary.notify

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * ปลุกที่ตั้งไว้จะหายเมื่อรีสตาร์ทเครื่อง / อัปเดตแอป / เปลี่ยนเวลาหรือเขตเวลา
 * จึงต้องตั้งใหม่ทุกครั้งที่ได้รับ broadcast เหล่านี้
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ReminderScheduler.sync(context)
    }
}

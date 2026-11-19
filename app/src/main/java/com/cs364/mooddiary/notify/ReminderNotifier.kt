package com.cs364.mooddiary.notify

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.cs364.mooddiary.MainActivity
import com.cs364.mooddiary.R
import com.cs364.mooddiary.data.SettingsStore
import java.util.Locale

/** สร้างช่องแจ้งเตือนและแสดงข้อความ "มาบันทึกอารมณ์กันเถอะ" */
object ReminderNotifier {
    const val CHANNEL_ID = "daily_reminder"
    private const val NOTIFICATION_ID = 1001

    /** Android 13+ ต้องได้รับอนุญาต POST_NOTIFICATIONS ก่อน */
    fun canPost(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) return false
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    /** @return true ถ้าส่งได้ (ใช้กับปุ่มทดสอบในหน้าตั้งค่า) */
    fun show(context: Context): Boolean {
        val app = context.applicationContext
        if (!canPost(app)) return false
        val res = localized(app)
        ensureChannel(app, res)

        val open = PendingIntent.getActivity(
            app, 0,
            Intent(app, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(app, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(res.getString(R.string.app_name))
            .setContentText(res.getString(R.string.reminder_preview))
            .setStyle(NotificationCompat.BigTextStyle().bigText(res.getString(R.string.reminder_preview)))
            .setColor(0xFFA6693A.toInt())
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(open)
            .setAutoCancel(true)
            .build()
        @Suppress("MissingPermission")   // ตรวจสิทธิ์ด้วย canPost() ข้างบนแล้ว
        NotificationManagerCompat.from(app).notify(NOTIFICATION_ID, notification)
        return true
    }

    private fun ensureChannel(context: Context, res: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            res.getString(R.string.notif_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply { description = res.getString(R.string.notif_channel_desc) }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    /**
     * ข้อความในแจ้งเตือนต้องใช้ภาษาที่ผู้ใช้เลือกในแอป (ไม่ใช่ภาษาของเครื่อง)
     * เพราะ BroadcastReceiver ไม่ได้ผ่าน Activity ที่ AppCompat ตั้งภาษาให้
     */
    private fun localized(context: Context): Context {
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale.forLanguageTag(SettingsStore(context).notificationLanguage()))
        return context.createConfigurationContext(config)
    }
}

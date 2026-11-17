package com.cs364.mooddiary

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.cs364.mooddiary.ui.navigation.MoodNavHost
import com.cs364.mooddiary.ui.theme.MoodDiaryTheme

/** ใช้ AppCompatActivity เพื่อรองรับการสลับภาษาในแอป (ข้อ 3.3) */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoodDiaryTheme {
                MoodNavHost()
            }
        }
    }
}

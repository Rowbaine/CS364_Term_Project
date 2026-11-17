package com.cs364.mooddiary.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * จานสีหลักของธีม "ไม้ฮิโนกิ × ผลไม้น่ารัก"
 * ตรงกับหน้า "ธีมสี · ฟอนต์ · คอมโพเนนต์" บน canvas ออกแบบ
 */
object WoodPalette {
    // ไม้ / กระดาษ (Day)
    val Hinoki = Color(0xFFF2DFBF)      // พื้นหลังลายไม้อ่อน
    val Plank = Color(0xFFA8693A)       // ไม้เข้ม (ป้าย, ปุ่มหลัก)
    val Bark = Color(0xFF4A2E1A)        // เส้นขอบหนา + ตัวอักษร
    val Line = Color(0xFF6B4226)        // เส้นขอบการ์ด
    val Washi = Color(0xFFFFF8EC)       // กระดาษวาชิ (การ์ด)
    val Crate = Color(0xFFFBEBD0)       // ด้านในลังไม้
    val Sub = Color(0xFF74512F)         // ข้อความรอง
    val Dash = Color(0xFFC9A27E)        // เส้นประ
    val Cream = Color(0xFFFFF6E6)       // ตัวอักษรบนไม้เข้ม
    val Ink = Color(0xFF2F1D10)         // ตัวอักษรบนปุ่มสี

    // ไม้ / กระดาษ (Night)
    val HinokiNight = Color(0xFF2A1E17)
    val PlankNight = Color(0xFF6E4626)
    val WashiNight = Color(0xFF3A2A1F)
    val CrateNight = Color(0xFF4A3527)
    val LineNight = Color(0xFF120B07)
    val EdgeNight = Color(0xFF0D0805)
    val TextNight = Color(0xFFF6E7D0)
    val SubNight = Color(0xFFD9C2A5)
    val DashNight = Color(0xFF7A5A40)

    // สีเน้น
    val Sakura = Color(0xFFF7B6C2)      // ป้ายชมพู / เมนูที่เลือก
    val Leaf = Color(0xFF8CB369)        // ใบไม้บนผลไม้
    val Matcha = Color(0xFF9CCB6C)      // ปุ่มบันทึก
    val Coral = Color(0xFFFF9A8F)       // ปุ่มลบ
    val Hanko = Color(0xFFC8463D)       // ตราฮังโกะ 心
    val Blush = Color(0xFFFF8FA3)       // แก้มชมพู

    // สีตัวละครอารมณ์
    val Peach = Color(0xFFFFB7C5)       // ดีใจ
    val Orange = Color(0xFFFFC27A)      // สบายใจ
    val Melon = Color(0xFFBFE08A)       // เฉยๆ
    val Blueberry = Color(0xFFA9C4F5)   // เศร้า
    val Apple = Color(0xFFFF9A8F)       // หงุดหงิด
    val Coconut = Color(0xFFD2AE8A)     // เหนื่อย
}

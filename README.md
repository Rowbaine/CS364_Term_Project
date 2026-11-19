# สมุดอารมณ์ (Mood Diary) — CS364 Term Project

แอปบันทึกอารมณ์ประจำวัน: เลือกผลไม้ตามอารมณ์ แล้วเขียนบันทึกสั้นๆ (ไม่เกิน 200 ตัวอักษร)
ธีม "ไม้ฮิโนกิ × ผลไม้น่ารัก" (ญี่ปุ่น + Watermelon Drop) · Kotlin + Jetpack Compose

🎨 **Figma (UI + Prototype):** [เปิดไฟล์ออกแบบใน Figma](https://www.figma.com/design/ykMCAPkxdlLbhNxbFv21Kg)

## เปิดโปรเจกต์

1. Android Studio (Ladybug ขึ้นไป) → **Open** → เลือกโฟลเดอร์นี้
2. รอ Gradle Sync (โหลด Gradle 8.9 / AGP 8.7.3 ให้เอง)
3. Run บน emulator หรือเครื่องจริง (Android 8.0 / API 26 ขึ้นไป)

## โครงสร้าง

```
app/src/main/java/com/cs364/mooddiary/
├── MainActivity.kt           AppCompatActivity (รองรับสลับภาษาในแอป)
├── MoodDiaryApp.kt           สร้าง repository/draft/settings + ใช้ธีมที่เลือกไว้
├── model/Mood.kt             อารมณ์ 6 แบบ + หน้าตาผลไม้ (FaceSpec), DiaryEntry, Draft
├── data/
│   ├── DiaryEntity.kt        ตาราง diary_entries (วันที่, อีโมจิ, ข้อความ) + ตัวแปลงเป็น DiaryEntry
│   ├── DiaryDao.kt           คำสั่งฐานข้อมูล: เพิ่ม, แก้ไข, ลบ, ดึงทั้งหมด, ดึงตามวัน
│   ├── DiaryDatabase.kt      ฐานข้อมูล Room ในเครื่อง (mood_diary.db)
│   ├── DiaryRepository.kt    ตัวกลางระหว่าง ViewModel กับ Room + DraftStore (ร่าง, SharedPreferences)
│   └── SettingsStore.kt      ภาษา TH/EN, ธีม Day/Night, การแจ้งเตือน
└── ui/
    ├── theme/                ★ ธีมทั้งหมด
    │   ├── Color.kt          WoodPalette: สีไม้ กระดาษ ผลไม้
    │   ├── Type.kt           ฟอนต์ Mitr (หัวข้อ) + Mali (เนื้อหา)
    │   └── Theme.kt          MoodDiaryTheme, MoodTheme.colors (Day/Night), MoodDimens
    ├── components/
    │   ├── MoodFace.kt       ตัวละครผลไม้ วาดด้วย Canvas
    │   └── WoodComponents.kt PaperCard, ChunkyButton, WoodCrate, HangingSign, WoodDialog ฯลฯ
    ├── navigation/MoodNavHost.kt   เส้นทางหน้าจอ + แผนภาพ Back
    ├── screens/              Home, PickMood, Write, Detail, Settings
    └── DiaryViewModel.kt
app/src/main/res/
├── font/                     Mitr, Mali (Google Fonts, OFL — ดู licenses/)
├── values/strings.xml        ภาษาอังกฤษ
├── values-th/strings.xml     ภาษาไทย
└── values-night/colors.xml   สีพื้นหลังโหมดกลางคืน
```

## ใช้ธีมในหน้าจอใหม่

```kotlin
@Composable
fun MyScreen() {
    val c = MoodTheme.colors                    // สีตามโหมด Day/Night อัตโนมัติ
    WoodBackground(Modifier.fillMaxSize()) {    // พื้นลายไม้
        PaperCard {                             // การ์ดกระดาษ ขอบหนา เงาทึบ
            MoodFace(Mood.HAPPY.face, size = 56.dp)
            Text(stringResource(R.string.mood_happy), style = MaterialTheme.typography.titleMedium, color = c.text)
        }
        ChunkyButton("บันทึก", onClick = {}, style = ButtonStyle.Primary)
    }
}
```

- ข้อความทุกตัวให้ใส่ทั้ง `values/strings.xml` และ `values-th/strings.xml`
- อย่าใส่สีตรงๆ ในหน้าจอ ให้ใช้ `MoodTheme.colors.xxx` เพื่อให้ Night theme ทำงาน
- เพิ่มหรือเปลี่ยนคอลัมน์ในตาราง: ต้องเพิ่ม `version` ใน `DiaryDatabase.kt` และเขียน Migration ด้วย ไม่อย่างนั้นแอปที่ติดตั้งไว้แล้วจะเปิดไม่ขึ้น

## หน้าปฏิทิน / สถิติ / เวลาเตือน

- **ปฏิทิน** (`ui/screens/CalendarScreen.kt`): ปฏิทินรายเดือน (ปี พ.ศ. เมื่อใช้ภาษาไทย) วันที่มีบันทึกแสดงหน้าผลไม้ แตะวันเพื่อดูบันทึก หรือกด "เขียน" เพื่อบันทึกย้อนหลังวันนั้น
- **สถิติ** (`ui/screens/StatsScreen.kt`): สลับ สัปดาห์นี้ / เดือนนี้ — อารมณ์ที่พบบ่อยที่สุด, กราฟสัดส่วน 6 อารมณ์, ความต่อเนื่อง
- **เวลาเตือน** (`ui/screens/ReminderSheet.kt`): ในหน้าตั้งค่า แตะ "เตือนให้เขียนทุกวัน" → เลือกเวลา (ทีละ 30 นาที) และวันที่เตือน ค่าถูกเก็บใน `SettingsStore` (ยังไม่ได้ตั้งการแจ้งเตือนจริงของระบบ)
- เมนูล่างใช้ร่วมกันที่ `ui/components/BottomWoodNav.kt`

## เทียบเกณฑ์การให้คะแนน

| ข้อ | ทำไว้ที่ |
|---|---|
| 1.1 ฟังก์ชันครบ | เพิ่ม (Pick → Write), ดู (Home, Detail), แก้ไข (Detail → Pick → Write), ลบ (Detail), Empty state (Home) |
| 3.1 หลายขนาดจอ | ทุกหน้าเลื่อนได้ + `widthIn(max)`; จอ ≥ 600dp แสดงการ์ด 2 คอลัมน์ (`HomeScreen`) |
| 3.2 แนวจอ | ล็อกแนวตั้ง `android:screenOrientation="portrait"` (AndroidManifest) |
| 3.3 ไทย/อังกฤษ | `values` + `values-th`, สลับในหน้าตั้งค่า (`SettingsStore.setLanguage`) |
| 3.4 Day/Night | `DayColors` / `NightColors` ใน `Theme.kt`, สลับในหน้าตั้งค่า |
| 4.1 Back navigation | แผนภาพใน `MoodNavHost.kt` ทุกหน้ามีปุ่มย้อนกลับ |
| 4.2 กันออกโดยไม่ตั้งใจ | Home: กด Back ซ้ำภายใน 2 วิ (`BackHandler` + Toast); Write: Dialog ยืนยันก่อนออก |
| 4.3 ยืนยันก่อนลบ | `WoodDialog` ที่ Detail (ลบ 1 รายการ) และ Settings (ลบทั้งหมด) |
| 4.4 Lifecycle | `WriteScreen`: บันทึกร่างอัตโนมัติ + `LifecycleEventEffect(ON_PAUSE)`; Home: แบนเนอร์ "เขียนต่อ" |

ยังไม่ได้ทำ: การแจ้งเตือนจริงของระบบ (ตั้งเวลาได้แล้ว แต่ยังไม่ส่ง notification)

## แหล่งที่มา (สำหรับข้อ 5.2)

- ฟอนต์ Mitr และ Mali จาก Google Fonts (SIL Open Font License) — ไฟล์สัญญาอยู่ใน `licenses/`
- โครงโปรเจกต์ ธีม คอมโพเนนต์ และหน้าจอเริ่มต้น สร้างโดยใช้ Claude (AI) ช่วยเขียน จากแบบ UI ที่ออกแบบร่วมกัน

package com.cs364.mooddiary.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cs364.mooddiary.model.Mood
import com.cs364.mooddiary.ui.DiaryViewModel
import androidx.navigation.NavHostController
import com.cs364.mooddiary.ui.components.NavTab
import com.cs364.mooddiary.ui.screens.CalendarScreen
import com.cs364.mooddiary.ui.screens.DetailScreen
import com.cs364.mooddiary.ui.screens.StatsScreen
import java.time.LocalDate
import com.cs364.mooddiary.ui.screens.HomeScreen
import com.cs364.mooddiary.ui.screens.PickMoodScreen
import com.cs364.mooddiary.ui.screens.SettingsScreen
import com.cs364.mooddiary.ui.screens.WriteScreen

/**
 * เส้นทางหน้าจอ (ข้อ 4.1 Back navigation)
 *
 *  home ──(+)──> pick ──> write ──(บันทึก)──> home
 *  home ──(การ์ด)──> detail ──(แก้ไข)──> pick ──> write ──> detail
 *  home ──(ตั้งค่า)──> settings
 *  เมนูล่าง: หน้าหลัก ⇄ ปฏิทิน ⇄ สถิติ ⇄ ตั้งค่า (Back จากปฏิทิน/สถิติ → หน้าหลัก)
 *  calendar ──(แตะวัน → การ์ด)──> detail
 *  calendar ──(เขียนย้อนหลัง)──> pick ──> write(วันที่เลือก) ──> calendar
 *  settings ──(เตือนให้เขียนทุกวัน)──> แผ่นเลือกเวลา
 *
 *  Back จาก pick/detail/settings → หน้าก่อนหน้า
 *  Back จาก write → ถามยืนยันก่อน (ข้อ 4.2)
 *  Back จาก home → ต้องกดซ้ำภายใน 2 วินาทีจึงออกจากแอป (ข้อ 4.2)
 */
object Routes {
    const val HOME = "home"
    const val PICK = "pick?entryId={entryId}&mood={mood}&day={day}"
    const val WRITE = "write?mood={mood}&entryId={entryId}&day={day}"
    const val DETAIL = "detail/{entryId}"
    const val SETTINGS = "settings"
    const val CALENDAR = "calendar"
    const val STATS = "stats"

    /** [day] = วันที่เขียนย้อนหลัง (epochDay) */
    fun pick(entryId: Long? = null, mood: Mood? = null, day: LocalDate? = null) =
        "pick?entryId=${entryId ?: -1}&mood=${mood?.key ?: ""}&day=${day?.toEpochDay() ?: NO_DAY}"
    fun write(mood: Mood, entryId: Long? = null, day: LocalDate? = null) =
        "write?mood=${mood.key}&entryId=${entryId ?: -1}&day=${day?.toEpochDay() ?: NO_DAY}"
    fun detail(id: Long) = "detail/$id"
}

private const val NO_DAY = Long.MIN_VALUE
private fun Long.orNull(): Long? = if (this == -1L) null else this
private fun Long.toDayOrNull(): LocalDate? = if (this == NO_DAY) null else LocalDate.ofEpochDay(this)

/** เปลี่ยนแท็บเมนูล่าง: หน้าหลักอยู่ล่างสุดของ back stack เสมอ */
private fun NavHostController.goTab(tab: NavTab) {
    when (tab) {
        NavTab.HOME -> popBackStack(Routes.HOME, inclusive = false)
        NavTab.SETTINGS -> navigate(Routes.SETTINGS) { launchSingleTop = true }
        NavTab.CALENDAR, NavTab.STATS -> navigate(if (tab == NavTab.CALENDAR) Routes.CALENDAR else Routes.STATS) {
            popUpTo(Routes.HOME) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}

@Composable
fun MoodNavHost() {
    val nav = rememberNavController()
    val vm: DiaryViewModel = viewModel()

    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                vm = vm,
                onAdd = { nav.navigate(Routes.pick()) },
                onOpen = { nav.navigate(Routes.detail(it)) },
                onContinueDraft = { d -> nav.navigate(Routes.write(d.mood, d.entryId)) },
                onTab = { nav.goTab(it) },
            )
        }
        composable(Routes.CALENDAR) {
            CalendarScreen(
                vm = vm,
                onTab = { nav.goTab(it) },
                onAdd = { nav.navigate(Routes.pick()) },
                onOpen = { nav.navigate(Routes.detail(it)) },
                onWriteFor = { day -> nav.navigate(Routes.pick(day = day)) },
            )
        }
        composable(Routes.STATS) {
            StatsScreen(vm = vm, onTab = { nav.goTab(it) }, onAdd = { nav.navigate(Routes.pick()) })
        }
        composable(
            Routes.PICK,
            arguments = listOf(
                navArgument("entryId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("mood") { type = NavType.StringType; defaultValue = "" },
                navArgument("day") { type = NavType.LongType; defaultValue = NO_DAY },
            ),
        ) { back ->
            val entryId = back.arguments?.getLong("entryId")?.orNull()
            val day = back.arguments?.getLong("day")?.toDayOrNull()
            val initial = back.arguments?.getString("mood")?.takeIf { it.isNotEmpty() }?.let { Mood.fromKey(it) }
                ?: entryId?.let { vm.entry(it)?.mood }
                ?: Mood.HAPPY
            PickMoodScreen(
                initial = initial,
                onBack = { nav.popBackStack() },
                onNext = { mood -> nav.navigate(Routes.write(mood, entryId, day)) },
            )
        }
        composable(
            Routes.WRITE,
            arguments = listOf(
                navArgument("mood") { type = NavType.StringType; defaultValue = Mood.HAPPY.key },
                navArgument("entryId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("day") { type = NavType.LongType; defaultValue = NO_DAY },
            ),
        ) { back ->
            val entryId = back.arguments?.getLong("entryId")?.orNull()
            val day = back.arguments?.getLong("day")?.toDayOrNull()
            WriteScreen(
                day = day,
                vm = vm,
                mood = Mood.fromKey(back.arguments?.getString("mood")),
                entryId = entryId,
                onBack = { nav.popBackStack() },

                onSaved = {
                    // แก้ไขเสร็จ → กลับหน้ารายละเอียด, เขียนย้อนหลังจากปฏิทิน → กลับปฏิทิน, นอกนั้น → หน้าหลัก
                    val backToDetail = entryId != null &&
                        (nav.popBackStack(Routes.detail(entryId), inclusive = false) || nav.popBackStack(Routes.DETAIL, inclusive = false))
                    val backToCalendar = !backToDetail && day != null && nav.popBackStack(Routes.CALENDAR, inclusive = false)
                    if (!backToDetail && !backToCalendar) nav.popBackStack(Routes.HOME, inclusive = false)
                },
            )
        }
        composable(
            Routes.DETAIL,
            arguments = listOf(navArgument("entryId") { type = NavType.LongType }),
        ) { back ->
            val id = back.arguments?.getLong("entryId") ?: -1L
            DetailScreen(
                vm = vm,
                entryId = id,
                onBack = { nav.popBackStack() },
                onEdit = { entry -> nav.navigate(Routes.pick(entry.id, entry.mood)) },
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(vm = vm, onBack = { nav.popBackStack() })
        }
    }
}

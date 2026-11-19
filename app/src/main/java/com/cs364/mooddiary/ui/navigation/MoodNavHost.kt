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
import com.cs364.mooddiary.ui.screens.DetailScreen
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
 *
 *  Back จาก pick/detail/settings → หน้าก่อนหน้า
 *  Back จาก write → ถามยืนยันก่อน (ข้อ 4.2)
 *  Back จาก home → ต้องกดซ้ำภายใน 2 วินาทีจึงออกจากแอป (ข้อ 4.2)
 */
object Routes {
    const val HOME = "home"
    const val PICK = "pick?entryId={entryId}&mood={mood}"
    const val WRITE = "write?mood={mood}&entryId={entryId}"
    const val DETAIL = "detail/{entryId}"
    const val SETTINGS = "settings"

    fun pick(entryId: Long? = null, mood: Mood? = null) = "pick?entryId=${entryId ?: -1}&mood=${mood?.key ?: ""}"
    fun write(mood: Mood, entryId: Long? = null) = "write?mood=${mood.key}&entryId=${entryId ?: -1}"
    fun detail(id: Long) = "detail/$id"
}

private fun Long.orNull(): Long? = if (this == -1L) null else this

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
                onSettings = { nav.navigate(Routes.SETTINGS) { launchSingleTop = true } },
            )
        }
        composable(
            Routes.PICK,
            arguments = listOf(
                navArgument("entryId") { type = NavType.LongType; defaultValue = -1L },
                navArgument("mood") { type = NavType.StringType; defaultValue = "" },
            ),
        ) { back ->
            val entryId = back.arguments?.getLong("entryId")?.orNull()
            val initial = back.arguments?.getString("mood")?.takeIf { it.isNotEmpty() }?.let { Mood.fromKey(it) }
                ?: entryId?.let { vm.entry(it)?.mood }
                ?: Mood.HAPPY
            PickMoodScreen(
                initial = initial,
                onBack = { nav.popBackStack() },
                onNext = { mood -> nav.navigate(Routes.write(mood, entryId)) },
            )
        }
        composable(
            Routes.WRITE,
            arguments = listOf(
                navArgument("mood") { type = NavType.StringType; defaultValue = Mood.HAPPY.key },
                navArgument("entryId") { type = NavType.LongType; defaultValue = -1L },
            ),
        ) { back ->
            val entryId = back.arguments?.getLong("entryId")?.orNull()
            WriteScreen(
                vm = vm,
                mood = Mood.fromKey(back.arguments?.getString("mood")),
                entryId = entryId,
                onBack = { nav.popBackStack() },

                onSaved = {
                    val backToDetail = entryId != null && nav.popBackStack(Routes.detail(entryId), inclusive = false)
                    if (!backToDetail) {
                        nav.popBackStack(
                            Routes.HOME,
                            inclusive = false
                        )
                    }
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

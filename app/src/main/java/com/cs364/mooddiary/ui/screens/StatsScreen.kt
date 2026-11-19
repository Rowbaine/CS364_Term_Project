package com.cs364.mooddiary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cs364.mooddiary.R
import com.cs364.mooddiary.model.DiaryEntry
import com.cs364.mooddiary.model.Mood
import com.cs364.mooddiary.model.SpecialFaces
import com.cs364.mooddiary.ui.DiaryViewModel
import com.cs364.mooddiary.ui.components.BottomWoodNav
import com.cs364.mooddiary.ui.components.MoodFace
import com.cs364.mooddiary.ui.components.NavTab
import com.cs364.mooddiary.ui.components.PaperCard
import com.cs364.mooddiary.ui.components.WoodBackground
import com.cs364.mooddiary.ui.components.WoodSegmented
import com.cs364.mooddiary.ui.components.chunky
import com.cs364.mooddiary.ui.theme.MoodTheme
import com.cs364.mooddiary.ui.theme.WoodPalette
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private enum class Period { WEEK, MONTH, YEAR }

/** ความต่อเนื่องสูงสุดจากชุดวันที่มีบันทึก */
private fun bestStreak(days: Set<LocalDate>): Int {
    var best = 0
    for (d in days) {
        if (days.contains(d.minusDays(1))) continue
        var n = 0; var x = d
        while (days.contains(x)) { n++; x = x.plusDays(1) }
        if (n > best) best = n
    }
    return best
}

/** ต่อเนื่องจนถึงวันนี้ (ถ้าวันนี้ยังไม่เขียน นับถึงเมื่อวาน) */
private fun currentStreak(days: Set<LocalDate>, today: LocalDate): Int {
    var d = if (days.contains(today)) today else today.minusDays(1)
    var n = 0
    while (days.contains(d)) { n++; d = d.minusDays(1) }
    return n
}

/**
 * หน้าสถิติ: สลับ สัปดาห์นี้ / เดือนนี้ / ปีนี้
 * อารมณ์ที่พบบ่อยที่สุด, สัดส่วนอารมณ์ 6 แบบ และความต่อเนื่อง
 */
@Composable
fun StatsScreen(vm: DiaryViewModel, onTab: (NavTab) -> Unit, onAdd: () -> Unit) {
    val entries by vm.entries.collectAsStateWithLifecycle()
    val c = MoodTheme.colors
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    var period by rememberSaveable { mutableStateOf(Period.WEEK) }

    val from = when (period) {
        Period.WEEK -> today.with(DayOfWeek.MONDAY)
        Period.MONTH -> today.withDayOfMonth(1)
        Period.YEAR -> today.withDayOfYear(1)
    }
    val inRange = remember(entries, period) {
        entries.filter {
            val d = Instant.ofEpochMilli(it.createdAt).atZone(zone).toLocalDate()
            !d.isBefore(from) && !d.isAfter(today)
        }
    }
    val allDays = remember(entries) { entries.map { Instant.ofEpochMilli(it.createdAt).atZone(zone).toLocalDate() }.toSet() }
    val loggedDaySet = inRange.map { Instant.ofEpochMilli(it.createdAt).atZone(zone).toLocalDate() }.toSet()
    val loggedDays = loggedDaySet.size
    // จำนวนวันที่บันทึกในแต่ละเดือน (ใช้วาดกราฟของมุมมองรายปี)
    val daysPerMonth = (1..12).map { m -> loggedDaySet.count { it.monthValue == m } }
    val counts: Map<Mood, Int> = Mood.entries.associateWith { m -> inRange.count { it.mood == m } }
    // อารมณ์ที่พบบ่อยที่สุด (ถ้าเท่ากัน เลือกอันที่บันทึกล่าสุด)
    val top: Mood? = inRange.groupBy { it.mood }.entries
        .maxWithOrNull(
            compareBy<Map.Entry<Mood, List<DiaryEntry>>> { it.value.size }.thenBy { g -> g.value.maxOf { it.createdAt } }
        )?.key

    WoodBackground(Modifier.fillMaxSize()) {
        val topPad = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        LazyColumn(
            modifier = Modifier.align(Alignment.TopCenter).widthIn(max = 560.dp).fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = topPad + 16.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.stats_title), style = MaterialTheme.typography.headlineSmall, color = c.text, modifier = Modifier.weight(1f))
                    SummaryChip(stringResource(R.string.stats_logged, loggedDays))
                }
            }
            item {
                WoodSegmented(
                    options = listOf(
                        Period.WEEK to stringResource(R.string.stats_week),
                        Period.MONTH to stringResource(R.string.stats_month),
                        Period.YEAR to stringResource(R.string.stats_year),
                    ),
                    selected = period, onSelect = { period = it },
                )
            }
            if (top == null) {
                item {
                    Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        MoodFace(SpecialFaces.Sleepy, size = 110.dp)
                        Text(stringResource(R.string.stats_empty), style = MaterialTheme.typography.titleMedium, color = c.text)
                        Text(stringResource(R.string.stats_empty_sub), style = MaterialTheme.typography.bodyMedium, color = c.textSub, textAlign = TextAlign.Center)
                    }
                }
            } else {
                item {
                    Row(
                        Modifier.fillMaxWidth().chunky(c.accent, c.edge).padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        MoodFace(top.face, size = 68.dp)
                        Column {
                            Text(stringResource(R.string.stats_top), style = MaterialTheme.typography.labelMedium, color = WoodPalette.Ink)
                            Text(stringResource(top.label), style = MaterialTheme.typography.headlineSmall, color = WoodPalette.Ink)
                            Text(stringResource(R.string.stats_top_count, counts[top] ?: 0, inRange.size),
                                style = MaterialTheme.typography.bodySmall, color = WoodPalette.Ink)
                        }
                    }
                }
                item { Breakdown(counts) }
                if (period == Period.YEAR) item { MonthlyBars(daysPerMonth, today.monthValue) }
            }
            item {
                val (title, sub) = when (period) {
                    Period.WEEK -> stringResource(R.string.stats_streak, currentStreak(allDays, today)) to
                        stringResource(R.string.stats_best, bestStreak(allDays))
                    Period.MONTH -> stringResource(R.string.stats_month_progress, loggedDays, today.dayOfMonth) to
                        stringResource(R.string.stats_month_left, today.lengthOfMonth() - today.dayOfMonth)
                    Period.YEAR -> stringResource(R.string.stats_month_progress, loggedDays, today.dayOfYear) to
                        stringResource(R.string.stats_year_left, today.lengthOfYear() - today.dayOfYear)
                }
                Row(
                    Modifier.fillMaxWidth().chunky(c.card, c.line).padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Box(Modifier.size(44.dp).background(c.crate, CircleShape).border(2.dp, c.line, CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.LocalFireDepartment, null, tint = Color(0xFFE2574C), modifier = Modifier.size(24.dp))
                    }
                    Column {
                        Text(title, style = MaterialTheme.typography.titleSmall, color = c.text)
                        Text(sub, style = MaterialTheme.typography.bodySmall, color = c.textSub)
                    }
                }
            }
        }
        BottomWoodNav(selected = NavTab.STATS, onTab = onTab, onAdd = onAdd, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
private fun Breakdown(counts: Map<Mood, Int>) {
    val c = MoodTheme.colors
    val max = (counts.values.maxOrNull() ?: 0).coerceAtLeast(1)
    PaperCard(Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.stats_breakdown), style = MaterialTheme.typography.titleSmall, color = c.text,
            modifier = Modifier.padding(bottom = 8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (m in Mood.entries) {
                val n = counts[m] ?: 0
                val label = stringResource(m.label)
                Row(Modifier.fillMaxWidth().semantics { contentDescription = "$label $n" },
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MoodFace(m.face, size = 28.dp, blush = false)
                    Text(label, style = MaterialTheme.typography.labelMedium, color = c.text, modifier = Modifier.width(64.dp), maxLines = 1)
                    Box(
                        Modifier.weight(1f).height(14.dp)
                            .background(c.crate, RoundedCornerShape(50))
                            .border(2.dp, c.line, RoundedCornerShape(50)),
                    ) {
                        if (n > 0) {
                            Box(Modifier.fillMaxWidth(n.toFloat() / max).fillMaxHeight().padding(2.dp)
                                .background(m.face.color, RoundedCornerShape(50)))
                        }
                    }
                    Text(n.toString(), style = MaterialTheme.typography.labelLarge, color = c.text,
                        textAlign = TextAlign.End, modifier = Modifier.width(22.dp))
                }
            }
        }
    }
}

/** กราฟแท่งรายปี: จำนวนวันที่บันทึกในแต่ละเดือน (เดือนปัจจุบันใช้สีเข้ม) */
@Composable
private fun MonthlyBars(daysPerMonth: List<Int>, currentMonth: Int) {
    val c = MoodTheme.colors
    val max = (daysPerMonth.maxOrNull() ?: 0).coerceAtLeast(1)
    val chartHeight = 88.dp
    PaperCard(Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.stats_by_month), style = MaterialTheme.typography.titleSmall, color = c.text,
            modifier = Modifier.padding(bottom = 8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            daysPerMonth.forEachIndexed { i, n ->
                val month = i + 1
                Column(
                    Modifier.weight(1f).semantics { contentDescription = "$month: $n" },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(if (n > 0) n.toString() else "", style = MaterialTheme.typography.labelSmall, color = c.textSub, maxLines = 1)
                    Box(Modifier.height(chartHeight).fillMaxWidth(), contentAlignment = Alignment.BottomCenter) {
                        if (n > 0) {
                            Box(
                                Modifier.width(16.dp).height(chartHeight * n / max)
                                    .background(if (month == currentMonth) c.primary else c.accent, RoundedCornerShape(6.dp))
                                    .border(2.dp, c.edge, RoundedCornerShape(6.dp)),
                            )
                        } else {
                            Box(Modifier.width(16.dp).height(4.dp).background(c.crate, RoundedCornerShape(50)))
                        }
                    }
                    Text(month.toString(), style = MaterialTheme.typography.labelSmall,
                        color = if (month == currentMonth) c.text else c.textSub, maxLines = 1,
                        modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

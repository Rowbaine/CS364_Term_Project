package com.cs364.mooddiary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cs364.mooddiary.R
import com.cs364.mooddiary.model.DiaryEntry
import com.cs364.mooddiary.model.SpecialFaces
import com.cs364.mooddiary.ui.DiaryViewModel
import com.cs364.mooddiary.ui.components.BottomWoodNav
import com.cs364.mooddiary.ui.components.ButtonStyle
import com.cs364.mooddiary.ui.components.ChunkyButton
import com.cs364.mooddiary.ui.components.IconSquareButton
import com.cs364.mooddiary.ui.components.MoodFace
import com.cs364.mooddiary.ui.components.NavTab
import com.cs364.mooddiary.ui.components.PaperCard
import com.cs364.mooddiary.ui.components.WoodBackground
import com.cs364.mooddiary.ui.components.chunky
import com.cs364.mooddiary.ui.theme.MoodTheme
import com.cs364.mooddiary.ui.theme.WoodPalette
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** ชื่อเดือน + ปี (ภาษาไทยใช้ปี พ.ศ.) */
internal fun monthTitle(ym: YearMonth, locale: Locale): String {
    val name = ym.month.getDisplayName(TextStyle.FULL_STANDALONE, locale)
    val year = if (locale.language == "th") ym.year + 543 else ym.year
    return "$name $year"
}

/**
 * หน้าปฏิทิน: เดือนละหน้า วันที่มีบันทึกจะแสดงหน้าผลไม้ของอารมณ์ล่าสุดในวันนั้น
 * แตะวันที่เพื่อดูบันทึกของวันนั้น หรือเขียนย้อนหลังถ้ายังไม่มี
 */
@Composable
fun CalendarScreen(
    vm: DiaryViewModel,
    onTab: (NavTab) -> Unit,
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
    onWriteFor: (LocalDate) -> Unit,
) {
    val entries by vm.entries.collectAsStateWithLifecycle()
    val c = MoodTheme.colors
    val locale: Locale = LocalConfiguration.current.locales[0]
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)

    var monthStr by rememberSaveable { mutableStateOf(YearMonth.from(today).toString()) }
    var selectedStr by rememberSaveable { mutableStateOf(today.toString()) }
    val month = YearMonth.parse(monthStr)
    val selected = LocalDate.parse(selectedStr)

    val byDay = remember(entries) {
        entries.groupBy { Instant.ofEpochMilli(it.createdAt).atZone(zone).toLocalDate() }
    }
    val loggedThisMonth = byDay.keys.count { YearMonth.from(it) == month }
    val dayEntries: List<DiaryEntry> = byDay[selected].orEmpty().sortedByDescending { it.createdAt }
    val timeFmt = remember(locale) { DateTimeFormatter.ofPattern("HH:mm", locale) }
    val dayTitleFmt = remember(locale) { DateTimeFormatter.ofPattern("EEEE d MMMM", locale) }

    WoodBackground(Modifier.fillMaxSize()) {
        val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        LazyColumn(
            modifier = Modifier.align(Alignment.TopCenter).widthIn(max = 560.dp).fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = top + 16.dp, bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.calendar_title), style = MaterialTheme.typography.headlineSmall, color = c.text, modifier = Modifier.weight(1f))
                    SummaryChip(stringResource(R.string.calendar_logged, loggedThisMonth))
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    IconSquareButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.calendar_prev),
                        onClick = { monthStr = month.minusMonths(1).toString() })
                    Text(monthTitle(month, locale), style = MaterialTheme.typography.titleMedium, color = c.text,
                        textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    IconSquareButton(Icons.AutoMirrored.Rounded.ArrowForward, stringResource(R.string.calendar_next),
                        onClick = { monthStr = month.plusMonths(1).toString() })
                }
            }
            item {
                MonthGrid(month, today, selected, byDay, locale, onPick = { selectedStr = it.toString() })
            }
            item {
                Text(selected.format(dayTitleFmt), style = MaterialTheme.typography.titleSmall, color = c.text,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp))
            }
            if (dayEntries.isNotEmpty()) {
                items(dayEntries, key = { it.id }) { e ->
                    EntryCard(e, timeFmt.format(Instant.ofEpochMilli(e.createdAt).atZone(zone)), onClick = { onOpen(e.id) })
                }
            } else {
                item { NoEntryCard(future = selected.isAfter(today), onWrite = { onWriteFor(selected) }) }
            }
        }
        BottomWoodNav(selected = NavTab.CALENDAR, onTab = onTab, onAdd = onAdd, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

@Composable
internal fun SummaryChip(text: String) {
    val c = MoodTheme.colors
    Text(
        text,
        modifier = Modifier
            .background(c.card, RoundedCornerShape(50))
            .border(2.5.dp, c.line, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        style = MaterialTheme.typography.labelMedium, color = c.text, maxLines = 1,
    )
}

@Composable
private fun MonthGrid(
    month: YearMonth,
    today: LocalDate,
    selected: LocalDate,
    byDay: Map<LocalDate, List<DiaryEntry>>,
    locale: Locale,
    onPick: (LocalDate) -> Unit,
) {
    val c = MoodTheme.colors
    val first = month.atDay(1)
    val lead = first.dayOfWeek.value - 1               // สัปดาห์เริ่มวันจันทร์
    val totalCells = ((lead + month.lengthOfMonth() + 6) / 7) * 7
    PaperCard(Modifier.fillMaxWidth(), contentPadding = 8.dp) {
        Row(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
            for (d in DayOfWeek.values()) {
                Text(d.getDisplayName(TextStyle.SHORT, locale), modifier = Modifier.weight(1f), textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = c.textSub, maxLines = 1)
            }
        }
        for (w in 0 until totalCells / 7) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (i in 0 until 7) {
                    val dayNum = w * 7 + i - lead + 1
                    if (dayNum < 1 || dayNum > month.lengthOfMonth()) {
                        Spacer(Modifier.weight(1f).height(50.dp))
                    } else {
                        val date = month.atDay(dayNum)
                        DayCell(date, today, date == selected, byDay[date]?.maxByOrNull { it.createdAt },
                            onClick = { onPick(date) }, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(date: LocalDate, today: LocalDate, isSelected: Boolean, latest: DiaryEntry?, onClick: () -> Unit, modifier: Modifier) {
    val c = MoodTheme.colors
    val future = date.isAfter(today)
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier
            .height(50.dp)
            .then(
                when {
                    isSelected -> Modifier.background(c.accent, shape).border(2.5.dp, c.edge, shape)
                    date == today -> Modifier.drawBehind {
                        val r = 12.dp.toPx()
                        drawRoundRect(c.line, cornerRadius = CornerRadius(r, r),
                            style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))))
                    }
                    else -> Modifier
                },
            )
            .clip(shape)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            date.dayOfMonth.toString(),
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected || date == today) FontWeight.Bold else FontWeight.SemiBold),
            color = when { isSelected -> WoodPalette.Ink; future -> c.dash; else -> c.text },
        )
        when {
            latest != null -> MoodFace(latest.mood.face, size = 28.dp, blush = false)
            !future -> Box(Modifier.size(28.dp).padding(3.dp).drawBehind {
                drawCircle(c.dash, style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))))
            })
            else -> Spacer(Modifier.size(28.dp))
        }
    }
}

@Composable
private fun NoEntryCard(future: Boolean, onWrite: () -> Unit) {
    val c = MoodTheme.colors
    Row(
        Modifier.fillMaxWidth().chunky(c.card, c.line).padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MoodFace(SpecialFaces.Sleepy, size = 52.dp)
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.calendar_no_entry), style = MaterialTheme.typography.labelLarge, color = c.text)
            Text(stringResource(if (future) R.string.calendar_future else R.string.calendar_write_back),
                style = MaterialTheme.typography.bodySmall, color = c.textSub)
        }
        if (!future) {
            ChunkyButton(stringResource(R.string.calendar_write), onWrite, style = ButtonStyle.Paper, height = 48.dp)
        }
    }
}

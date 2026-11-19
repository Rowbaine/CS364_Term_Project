package com.cs364.mooddiary.ui.screens

import android.app.Activity
import android.os.SystemClock
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cs364.mooddiary.R
import com.cs364.mooddiary.model.DiaryEntry
import com.cs364.mooddiary.model.Draft
import com.cs364.mooddiary.model.Mood
import com.cs364.mooddiary.model.SpecialFaces
import com.cs364.mooddiary.ui.DiaryViewModel
import com.cs364.mooddiary.ui.components.HangingSign
import com.cs364.mooddiary.ui.components.MoodFace
import com.cs364.mooddiary.ui.components.MoodPill
import com.cs364.mooddiary.ui.components.PaperCard
import com.cs364.mooddiary.ui.components.WoodBackground
import com.cs364.mooddiary.ui.components.chunky
import com.cs364.mooddiary.ui.components.plankGrain
import com.cs364.mooddiary.ui.theme.MoodTheme
import com.cs364.mooddiary.ui.theme.WoodPalette
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HomeScreen(
    vm: DiaryViewModel,
    onAdd: () -> Unit,
    onOpen: (Long) -> Unit,
    onContinueDraft: (Draft) -> Unit,
    onSettings: () -> Unit,
) {
    val entries by vm.entries.collectAsStateWithLifecycle()
    val draft by vm.draft.collectAsStateWithLifecycle()
    val c = MoodTheme.colors
    val context = LocalContext.current
    val config = LocalConfiguration.current
    val locale: Locale = config.locales[0]
    // ข้อ 3.1: จอกว้าง (แท็บเล็ต) แสดงการ์ด 2 คอลัมน์
    val columns = if (config.screenWidthDp >= 600) 2 else 1

    // ข้อ 4.2: กด Back ครั้งแรกแสดงข้อความ กดซ้ำภายใน 2 วินาทีจึงออกจากแอป
    var lastBack by remember { mutableLongStateOf(0L) }
    BackHandler {
        val now = SystemClock.elapsedRealtime()
        if (now - lastBack < 2000) {
            (context as? Activity)?.finish()
        } else {
            lastBack = now
            Toast.makeText(context, R.string.toast_press_back_again, Toast.LENGTH_SHORT).show()
        }
    }

    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val byDay = remember(entries) {
        entries.groupBy { Instant.ofEpochMilli(it.createdAt).atZone(zone).toLocalDate() }
    }
    val streak = remember(byDay) {
        var d = if (byDay.containsKey(today)) today else today.minusDays(1)
        var n = 0
        while (byDay.containsKey(d)) { n++; d = d.minusDays(1) }
        n
    }
    val timeFmt = remember(locale) { DateTimeFormatter.ofPattern("EEE d MMM · HH:mm", locale) }
    val greet = when (LocalTime.now().hour) {
        in 5..11 -> R.string.greet_morning
        in 12..16 -> R.string.greet_afternoon
        else -> R.string.greet_evening
    }

    WoodBackground(Modifier.fillMaxSize()) {
        val top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier.align(Alignment.TopCenter).widthIn(max = 840.dp).fillMaxSize(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = top + 12.dp, bottom = 140.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            val full: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }

            item(span = full) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    HangingSign(stringResource(R.string.home_title))
                }
            }
            item(span = full) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp), verticalAlignment = Alignment.Bottom) {
                    Column(Modifier.weight(1f)) {
                        Text(today.format(DateTimeFormatter.ofPattern("EEEE d MMMM", locale)),
                            style = MaterialTheme.typography.bodyMedium, color = c.textSub)
                        Text(stringResource(greet), style = MaterialTheme.typography.titleLarge, color = c.text)
                    }
                    if (streak > 0) {
                        Row(
                            Modifier
                                .background(c.card, RoundedCornerShape(50))
                                .border(2.5.dp, c.line, RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(Icons.Rounded.LocalFireDepartment, null, tint = Color0xE2574C, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.size(4.dp))
                            Text(stringResource(R.string.streak_days, streak), style = MaterialTheme.typography.labelMedium, color = c.text)
                        }
                    }
                }
            }
            // ข้อ 4.4: มีร่างค้างอยู่ → ให้เขียนต่อได้ทันที
            draft?.let { d ->
                item(span = full) { DraftBanner(d, locale, onContinue = { onContinueDraft(d) }) }
            }
            item(span = full) { WeekStrip(today, byDay, locale, onOpen) }


            if (entries.isEmpty()) {
                item(span = full) { EmptyState() }
            } else {
                item(span = full) {
                    Text(stringResource(R.string.recent_entries), style = MaterialTheme.typography.titleMedium,
                        color = c.text, modifier = Modifier.padding(start = 4.dp, top = 6.dp))
                }
                items(entries, key = { it.id }) { e ->
                    EntryCard(e, timeFmt.format(Instant.ofEpochMilli(e.createdAt).atZone(zone)), onClick = { onOpen(e.id) })
                }
            }
        }

        BottomWoodNav(
            modifier = Modifier.align(Alignment.BottomCenter),
            onAdd = onAdd,
            onSettings = onSettings,
            onSoon = { Toast.makeText(context, R.string.toast_coming_soon, Toast.LENGTH_SHORT).show() },
        )
    }
}

private val Color0xE2574C = androidx.compose.ui.graphics.Color(0xFFE2574C)

@Composable
private fun DraftBanner(d: Draft, locale: Locale, onContinue: () -> Unit) {
    val c = MoodTheme.colors
    val time = DateTimeFormatter.ofPattern("HH:mm", locale).format(Instant.ofEpochMilli(d.updatedAt).atZone(ZoneId.systemDefault()))
    Row(
        Modifier.fillMaxWidth().chunky(c.accent, c.edge, 20.dp).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(Icons.Rounded.Edit, null, tint = WoodPalette.Ink, modifier = Modifier.size(26.dp))
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.draft_title), style = MaterialTheme.typography.titleSmall, color = WoodPalette.Ink)
            Text(stringResource(R.string.draft_sub, stringResource(d.mood.label), time),
                style = MaterialTheme.typography.bodySmall, color = WoodPalette.Ink)
        }
        Text(
            stringResource(R.string.draft_continue),
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(WoodPalette.Washi)
                .border(2.5.dp, WoodPalette.Ink, RoundedCornerShape(14.dp))
                .clickable(role = Role.Button, onClick = onContinue)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            style = MaterialTheme.typography.labelLarge, color = WoodPalette.Ink,
        )
    }
}

@Composable
private fun WeekStrip(today: LocalDate, byDay: Map<LocalDate, List<DiaryEntry>>, locale: Locale, onOpen: (Long) -> Unit) {
    val c = MoodTheme.colors
    PaperCard(Modifier.fillMaxWidth(), contentPadding = 10.dp) {
        Row(Modifier.fillMaxWidth()) {
            for (i in 6 downTo 0) {
                val day = today.minusDays(i.toLong())
                val dayEntry = byDay[day]?.maxByOrNull {
                    it.createdAt
                }
                val mood: Mood? = dayEntry?.mood
                val dayModifier = Modifier
                    .weight(1f)
                    .then(
                        if (dayEntry != null) {
                            Modifier.clickable(role = Role.Button) {
                                onOpen(dayEntry.id)
                            }
                        } else {
                            Modifier
                        }
                    )
                Column(
                    dayModifier,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                }
            }
        }
    }
}

@Composable
private fun EntryCard(e: DiaryEntry, time: String, onClick: () -> Unit) {
    val c = MoodTheme.colors
    PaperCard(Modifier.fillMaxWidth(), onClick = onClick, contentPadding = 12.dp) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            MoodFace(e.mood.face, size = 58.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    MoodPill(e.mood)
                    Spacer(Modifier.weight(1f))
                    Text(time, style = MaterialTheme.typography.labelSmall, color = c.textSub, maxLines = 1)
                }
                Text(e.text, style = MaterialTheme.typography.bodyMedium, color = c.text,
                    maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun EmptyState() {
    val c = MoodTheme.colors
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MoodFace(SpecialFaces.Sleepy, size = 120.dp)
        Text(stringResource(R.string.empty_title), style = MaterialTheme.typography.titleLarge, color = c.text)
        Text(stringResource(R.string.empty_sub), style = MaterialTheme.typography.bodyMedium,
            color = c.textSub, textAlign = TextAlign.Center)
    }
}

@Composable
private fun BottomWoodNav(modifier: Modifier, onAdd: () -> Unit, onSettings: () -> Unit, onSoon: () -> Unit) {
    val c = MoodTheme.colors
    Box(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 30.dp)
                .background(c.card, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .drawBehind { drawRect(c.line, size = androidx.compose.ui.geometry.Size(size.width, 3.dp.toPx())) }
                .navigationBarsPadding()
                .height(84.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavItem(Icons.Rounded.Home, stringResource(R.string.nav_home), selected = true, onClick = {}, modifier = Modifier.weight(1f))
            NavItem(Icons.Rounded.CalendarMonth, stringResource(R.string.nav_calendar), onClick = onSoon, modifier = Modifier.weight(1f))
            Spacer(Modifier.weight(1f))
            NavItem(Icons.Rounded.BarChart, stringResource(R.string.nav_stats), onClick = onSoon, modifier = Modifier.weight(1f))
            NavItem(Icons.Rounded.Settings, stringResource(R.string.nav_settings), onClick = onSettings, modifier = Modifier.weight(1f))
        }
        // ปุ่ม + ไม้กลม ลอยอยู่ตรงกลาง
        val addLabel = stringResource(R.string.nav_add)
        Box(
            Modifier
                .align(Alignment.TopCenter)
                .offset(y = 2.dp)
                .size(68.dp)
                .chunky(c.plank, c.edge, 34.dp, shadow = 5.dp)
                .clip(CircleShape)
                .plankGrain(c.plank)
                .clickable(role = Role.Button, onClickLabel = addLabel, onClick = onAdd),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Rounded.Add, contentDescription = addLabel, tint = c.onPlank, modifier = Modifier.size(34.dp))
        }
    }
}

@Composable
private fun NavItem(icon: ImageVector, label: String, onClick: () -> Unit, modifier: Modifier = Modifier, selected: Boolean = false) {
    val c = MoodTheme.colors
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(role = Role.Tab, onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Box(
            Modifier
                .then(if (selected) Modifier.background(c.accent, RoundedCornerShape(50)).border(2.dp, WoodPalette.Bark, RoundedCornerShape(50)) else Modifier)
                .padding(horizontal = 14.dp, vertical = 4.dp),
        ) {
            Icon(icon, null, tint = if (selected) WoodPalette.Ink else c.textSub, modifier = Modifier.size(22.dp))
        }
        Text(label, style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold),
            color = if (selected) c.text else c.textSub, maxLines = 1)
    }
}

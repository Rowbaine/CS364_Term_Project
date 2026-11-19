package com.cs364.mooddiary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cs364.mooddiary.R
import com.cs364.mooddiary.model.Mood
import com.cs364.mooddiary.ui.components.ButtonStyle
import com.cs364.mooddiary.ui.components.ChunkyButton
import com.cs364.mooddiary.ui.components.IconSquareButton
import com.cs364.mooddiary.ui.components.MoodFace
import com.cs364.mooddiary.ui.components.chunky
import com.cs364.mooddiary.ui.theme.MoodTheme
import com.cs364.mooddiary.ui.theme.WoodPalette
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

internal fun formatMinutes(m: Int): String = "%02d:%02d".format(Locale.ROOT, (m / 60) % 24, m % 60)

/** ข้อความสรุปการเตือน เช่น "ทุกวันเวลา 20:00" หรือ "จ. พ. ศ. เวลา 20:00" */
@Composable
internal fun reminderSummary(enabled: Boolean, minutes: Int, days: Set<Int>): String {
    val locale = LocalConfiguration.current.locales[0]
    val time = formatMinutes(minutes)
    return when {
        !enabled || days.isEmpty() -> stringResource(R.string.reminder_sub_off)
        days.size == 7 -> stringResource(R.string.reminder_sub_daily, time)
        else -> stringResource(R.string.reminder_sub_days,
            days.sorted().joinToString(" ") { DayOfWeek.of(it).getDisplayName(TextStyle.SHORT, locale) }, time)
    }
}

/**
 * แผ่นเลือกเวลาเตือนจากด้านล่าง: ปรับเวลาทีละ 30 นาที, เลือกวัน, ดูตัวอย่างการแจ้งเตือน
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderSheet(
    initialMinutes: Int,
    initialDays: Set<Int>,
    onDismiss: () -> Unit,
    onSave: (minutes: Int, days: Set<Int>) -> Unit,
) {
    val c = MoodTheme.colors
    val locale = LocalConfiguration.current.locales[0]
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var minutes by rememberSaveable { mutableIntStateOf(initialMinutes) }
    var days by rememberSaveable { mutableStateOf(initialDays.sorted().joinToString("")) }
    val daySet = days.mapNotNull { it.digitToIntOrNull() }.toSet()
    val step = 30
    val shift: (Int) -> Unit = { d -> minutes = ((minutes + d) % 1440 + 1440) % 1440 }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = c.background,
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 24.dp).navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(stringResource(R.string.reminder_title), style = MaterialTheme.typography.titleLarge, color = c.text)

            // ตัวเลือกเวลา: เวลาก่อน/หลังแตะเลือกได้ ปุ่ม − / + ทีละ 30 นาที
            Text(formatMinutes(minutes - step + 1440), style = MaterialTheme.typography.titleLarge, color = c.dash,
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(role = Role.Button) { shift(-step) }.padding(horizontal = 16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconSquareButton(Icons.Rounded.Remove, stringResource(R.string.reminder_earlier), onClick = { shift(-step) })
                Box(
                    Modifier.weight(1f).chunky(c.accent, c.edge, 18.dp).padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(formatMinutes(minutes), style = MaterialTheme.typography.headlineSmall.copy(fontSize = 34.sp), color = WoodPalette.Ink)
                }
                IconSquareButton(Icons.Rounded.Add, stringResource(R.string.reminder_later), onClick = { shift(step) })
            }
            Text(formatMinutes(minutes + step), style = MaterialTheme.typography.titleLarge, color = c.dash,
                modifier = Modifier.clip(RoundedCornerShape(12.dp)).clickable(role = Role.Button) { shift(step) }.padding(horizontal = 16.dp))

            Text(stringResource(R.string.reminder_days), style = MaterialTheme.typography.labelMedium, color = c.textSub,
                modifier = Modifier.fillMaxWidth())
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                for (d in 1..7) {
                    val on = d in daySet
                    Box(
                        Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(RoundedCornerShape(50))
                            .background(if (on) c.accent else c.card)
                            .border(2.dp, if (on) c.edge else c.line, RoundedCornerShape(50))
                            .semantics { selected = on }
                            .clickable(role = Role.Checkbox) {
                                val next = if (on) daySet - d else daySet + d
                                days = next.sorted().joinToString("")
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(DayOfWeek.of(d).getDisplayName(TextStyle.NARROW, locale), style = MaterialTheme.typography.labelLarge,
                            color = if (on) WoodPalette.Ink else c.text, textAlign = TextAlign.Center, maxLines = 1)
                    }
                }
            }

            // ตัวอย่างการแจ้งเตือน
            Row(
                Modifier.fillMaxWidth().background(c.card, RoundedCornerShape(18.dp)).border(2.5.dp, c.line, RoundedCornerShape(18.dp))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MoodFace(Mood.HAPPY.face, size = 36.dp)
                Column {
                    Text(stringResource(R.string.app_name) + " · " + formatMinutes(minutes), style = MaterialTheme.typography.labelSmall, color = c.textSub)
                    Text(stringResource(R.string.reminder_preview), style = MaterialTheme.typography.bodyMedium, color = c.text)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ChunkyButton(stringResource(R.string.cancel), onDismiss, Modifier.weight(1f), style = ButtonStyle.Paper)
                ChunkyButton(stringResource(R.string.save), { onSave(minutes, daySet) }, Modifier.weight(1f), style = ButtonStyle.Primary)
            }
        }
    }
}

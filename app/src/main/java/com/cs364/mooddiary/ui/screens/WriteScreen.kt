package com.cs364.mooddiary.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.cs364.mooddiary.R
import com.cs364.mooddiary.model.MAX_NOTE_LENGTH
import com.cs364.mooddiary.model.Mood
import com.cs364.mooddiary.model.SpecialFaces
import com.cs364.mooddiary.ui.DiaryViewModel
import com.cs364.mooddiary.ui.components.BackButton
import com.cs364.mooddiary.ui.components.ButtonStyle
import com.cs364.mooddiary.ui.components.ChunkyButton
import com.cs364.mooddiary.ui.components.MoodFace
import com.cs364.mooddiary.ui.components.MoodPill
import com.cs364.mooddiary.ui.components.WoodBackground
import com.cs364.mooddiary.ui.components.WoodDialog
import com.cs364.mooddiary.ui.components.chunky
import com.cs364.mooddiary.ui.components.plankGrain
import com.cs364.mooddiary.ui.theme.MoodTheme
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WriteScreen(
    vm: DiaryViewModel,
    mood: Mood,
    entryId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val c = MoodTheme.colors
    val locale = LocalConfiguration.current.locales[0]
    val originalEntry = remember(entryId) {
        entryId?.let { vm.entry(it) }
    }
    val original = originalEntry?.text.orEmpty()
    val isSaving = remember { mutableStateOf(false) }


    // ข้อ 4.4: ถ้ามีร่างของบันทึกนี้ค้างไว้ ให้เริ่มจากร่างนั้น

    var text by rememberSaveable(entryId, mood.key) {
        val d = vm.draft.value

        mutableStateOf(
            if (d != null && d.entryId == entryId) {
                d.text
            } else {
                original
            }
        )
    }

    var savedAt by remember { mutableLongStateOf(0L) }

    var showLeave by rememberSaveable { mutableStateOf(false) }

    var didSave by rememberSaveable { mutableStateOf(false) }

    val changed = text != original || (entryId != null && mood != originalEntry?.mood)

    // บันทึกร่างอัตโนมัติหลังหยุดพิมพ์ 0.5 วินาที
    LaunchedEffect(text, mood, didSave) {
        if (!changed || didSave) {
            return@LaunchedEffect
        }

        delay(500)

        if (!didSave) {
            vm.saveDraft(mood, text, entryId)
            savedAt = System.currentTimeMillis()
        }
    }
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        if (changed && !didSave) {
            vm.saveDraft(mood, text, entryId)
        }
    }

    // ข้อ 4.2: กด Back ระหว่างเขียน → ถามก่อน
    val requestBack: () -> Unit = { if (changed) { showLeave = true } else { onBack() } }
    BackHandler(enabled = changed) { showLeave = true }

    WoodBackground(Modifier.fillMaxSize()) {
        Column(
            Modifier.align(Alignment.TopCenter).widthIn(max = 560.dp).fillMaxSize()
                .safeDrawingPadding().imePadding().padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    BackButton(requestBack)
                    Spacer(Modifier.weight(1f))
                    StepDots(step = 2)
                    Spacer(Modifier.weight(1f))
                    Spacer(Modifier.width(48.dp))
                }
                Spacer(Modifier.height(18.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    MoodFace(mood.face, size = 84.dp)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        MoodPill(mood)
                        Text(stringResource(R.string.write_title), style = MaterialTheme.typography.titleLarge, color = c.text)
                        if (savedAt > 0L) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Rounded.Check, null, tint = c.success, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    stringResource(R.string.write_autosaved,
                                        DateTimeFormatter.ofPattern("HH:mm", locale).format(Instant.ofEpochMilli(savedAt).atZone(ZoneId.systemDefault()))),
                                    style = MaterialTheme.typography.labelSmall, color = c.success,
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(18.dp))

                // กรอบไม้ + กระดาษมีเส้นบรรทัด
                Box(
                    Modifier.fillMaxWidth()
                        .chunky(c.plank, c.edge, 26.dp, shadow = 5.dp)
                        .clip(RoundedCornerShape(26.dp))
                        .plankGrain(c.plank)
                        .padding(10.dp),
                ) {
                    val lineColor = if (c.isNight) androidx.compose.ui.graphics.Color(0xFF5E4533) else androidx.compose.ui.graphics.Color(0xFFEAD3B0)
                    Column(
                        Modifier.fillMaxWidth()
                            .background(c.card, RoundedCornerShape(18.dp))
                            .border(3.dp, c.edge, RoundedCornerShape(18.dp))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Text(stringResource(R.string.write_label), style = MaterialTheme.typography.labelMedium, color = c.textSub)
                        val lineHeight = 35.sp
                        BasicTextField(
                            value = text,
                            onValueChange = { if (it.length <= MAX_NOTE_LENGTH) text = it },
                            textStyle = MaterialTheme.typography.bodyLarge.copy(color = c.text, lineHeight = lineHeight),
                            cursorBrush = SolidColor(c.text),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 210.dp)
                                .drawBehind {
                                    val step = lineHeight.toPx()
                                    var y = step
                                    while (y < size.height) {
                                        drawRect(lineColor, topLeft = Offset(0f, y - 1.dp.toPx()), size = Size(size.width, 2.dp.toPx()))
                                        y += step
                                    }
                                },
                            decorationBox = { inner ->
                                Box {
                                    if (text.isEmpty()) {
                                        Text(stringResource(R.string.write_placeholder),
                                            style = MaterialTheme.typography.bodyLarge.copy(lineHeight = lineHeight), color = c.textSub)
                                    }
                                    inner()
                                }
                            },
                        )
                        Text(
                            stringResource(R.string.write_counter, text.length, MAX_NOTE_LENGTH),
                            modifier = Modifier.align(Alignment.End),
                            style = MaterialTheme.typography.labelSmall, color = c.textSub,
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.write_prompt_hint), style = MaterialTheme.typography.labelMedium, color = c.textSub)
                Spacer(Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(R.string.prompt_good, R.string.prompt_thanks, R.string.prompt_tomorrow).forEach { res ->
                        val prompt = stringResource(res)
                        Text(
                            prompt,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(c.card)
                                .border(2.5.dp, c.line, RoundedCornerShape(50))
                                .clickable(role = Role.Button) {
                                    text = (if (text.isBlank()) prompt else "$text $prompt").take(MAX_NOTE_LENGTH)
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.labelMedium, color = c.text,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            ChunkyButton(
                text = stringResource(R.string.write_save),
                onClick = {
                    if (!didSave && text.isNotBlank()) {
                        didSave = true
                        vm.save(
                            mood = mood,
                            text = text,
                            entryId = entryId
                        )
                        onSaved()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                style = ButtonStyle.Primary,
                icon = Icons.Rounded.Check,
                enabled = text.isNotBlank(),
                height = 60.dp,
            )
            Spacer(Modifier.height(6.dp))
        }
    }

    if (showLeave) {
        WoodDialog(
            face = SpecialFaces.Nervous,
            title = stringResource(R.string.leave_title),
            message = stringResource(R.string.leave_message),
            confirmText = stringResource(R.string.leave_go),
            onConfirm = { vm.saveDraft(mood, text, entryId); showLeave = false; onBack() },
            dismissText = stringResource(R.string.leave_stay),
            onDismiss = { showLeave = false },
            confirmStyle = ButtonStyle.Paper,
            dismissStyle = ButtonStyle.Primary,
        )
    }
}

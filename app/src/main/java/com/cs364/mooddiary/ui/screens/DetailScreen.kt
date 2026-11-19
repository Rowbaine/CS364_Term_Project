package com.cs364.mooddiary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cs364.mooddiary.R
import com.cs364.mooddiary.model.SpecialFaces
import com.cs364.mooddiary.model.DiaryEntry
import com.cs364.mooddiary.ui.DiaryViewModel
import com.cs364.mooddiary.ui.components.BackButton
import com.cs364.mooddiary.ui.components.ButtonStyle
import com.cs364.mooddiary.ui.components.ChunkyButton
import com.cs364.mooddiary.ui.components.MoodFace
import com.cs364.mooddiary.ui.components.MoodPill
import com.cs364.mooddiary.ui.components.PaperCard
import com.cs364.mooddiary.ui.components.WoodBackground
import com.cs364.mooddiary.ui.components.WoodDialog
import com.cs364.mooddiary.ui.components.chunky
import com.cs364.mooddiary.ui.components.plankGrain
import com.cs364.mooddiary.ui.theme.MoodTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun DetailScreen(vm: DiaryViewModel, entryId: Long, onBack: () -> Unit, onEdit: (DiaryEntry) -> Unit) {
    val entries by vm.entries.collectAsStateWithLifecycle()
    val entry = entries.firstOrNull { it.id == entryId }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }
    val c = MoodTheme.colors
    val locale = LocalConfiguration.current.locales[0]

    if (entry == null) {
        LaunchedEffect(entryId) {
            onBack()
        }
        return
    }
    val created = Instant.ofEpochMilli(entry.createdAt).atZone(ZoneId.systemDefault())

    WoodBackground(Modifier.fillMaxSize()) {
        Column(
            Modifier.align(Alignment.TopCenter).widthIn(max = 560.dp).fillMaxSize()
                .safeDrawingPadding().padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    BackButton(onBack)
                    Spacer(Modifier.weight(1f))
                    Text(DateTimeFormatter.ofPattern("EEEE d MMM", locale).format(created),
                        style = MaterialTheme.typography.titleSmall, color = c.text)
                    Spacer(Modifier.weight(1f))
                    Spacer(Modifier.width(48.dp))
                }
                Spacer(Modifier.height(22.dp))
                // จานไม้กลม + ผลไม้ตัวใหญ่
                Box(
                    Modifier.size(168.dp).chunky(c.plank, c.edge, 84.dp, shadow = 6.dp).clip(CircleShape).plankGrain(c.plank),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier.size(136.dp).background(c.crate, CircleShape).border(3.dp, c.edge, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { MoodFace(entry.mood.face, size = 116.dp) }
                }
                Spacer(Modifier.height(16.dp))
                MoodPill(entry.mood, extra = DateTimeFormatter.ofPattern("HH:mm", locale).format(created))
                Spacer(Modifier.height(20.dp))
                PaperCard(Modifier.fillMaxWidth(), contentPadding = 18.dp) {
                    Text(entry.text, style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 35.sp), color = c.text)
                }
                Spacer(Modifier.height(16.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ChunkyButton(stringResource(R.string.edit), { onEdit(entry) }, Modifier.weight(1f),
                    style = ButtonStyle.Paper, icon = Icons.Rounded.Edit)
                ChunkyButton(stringResource(R.string.delete), { confirmDelete = true }, Modifier.weight(1f),
                    style = ButtonStyle.Danger, icon = Icons.Rounded.Delete)
            }
            Spacer(Modifier.height(6.dp))
        }
    }

    // ข้อ 4.3: ยืนยันก่อนลบทุกครั้ง
    if (confirmDelete) {
        WoodDialog(
            face = SpecialFaces.Worried,
            title = stringResource(R.string.delete_title),
            message = stringResource(R.string.delete_message),
            confirmText = stringResource(R.string.delete_confirm),
            onConfirm = {
                confirmDelete = false
                vm.delete(entry.id)
            },
            dismissText = stringResource(R.string.cancel),
            onDismiss = { confirmDelete = false },
        )
    }
}

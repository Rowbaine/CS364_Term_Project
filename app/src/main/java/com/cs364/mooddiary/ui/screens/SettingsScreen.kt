package com.cs364.mooddiary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.StayCurrentPortrait
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.cs364.mooddiary.R
import com.cs364.mooddiary.data.SettingsStore
import com.cs364.mooddiary.model.SpecialFaces
import com.cs364.mooddiary.ui.DiaryViewModel
import com.cs364.mooddiary.ui.components.BackButton
import com.cs364.mooddiary.ui.components.PaperCard
import com.cs364.mooddiary.ui.components.WoodBackground
import com.cs364.mooddiary.ui.components.WoodDialog
import com.cs364.mooddiary.ui.components.chunky
import com.cs364.mooddiary.ui.theme.MoodTheme
import com.cs364.mooddiary.ui.theme.WoodPalette

@Composable
fun SettingsScreen(vm: DiaryViewModel, onBack: () -> Unit) {
    val c = MoodTheme.colors
    val lang = remember { SettingsStore.currentLanguage() }
    var reminder by remember { mutableStateOf(vm.settings.reminder) }
    var confirmWipe by rememberSaveable { mutableStateOf(false) }

    WoodBackground(Modifier.fillMaxSize()) {
        Column(
            Modifier.align(Alignment.TopCenter).widthIn(max = 560.dp).fillMaxSize()
                .safeDrawingPadding().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BackButton(onBack)
                Spacer(Modifier.width(14.dp))
                Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge, color = c.text)
            }

            // ข้อ 3.3 ภาษา
            PaperCard(Modifier.fillMaxWidth()) {
                SectionLabel(stringResource(R.string.settings_language))
                Segmented(
                    options = listOf("th" to "ไทย", "en" to "English"),
                    selected = lang,
                    onSelect = { SettingsStore.setLanguage(it) },   // Activity จะ recreate พร้อมภาษาใหม่
                )
            }

            // ข้อ 3.4 ธีม
            PaperCard(Modifier.fillMaxWidth()) {
                SectionLabel(stringResource(R.string.settings_theme))
                Segmented(
                    options = listOf("day" to stringResource(R.string.theme_day), "night" to stringResource(R.string.theme_night)),
                    icons = listOf(Icons.Rounded.WbSunny, Icons.Rounded.DarkMode),
                    selected = if (c.isNight) "night" else "day",
                    onSelect = { vm.settings.setNight(it == "night") },
                )
            }

            PaperCard(Modifier.fillMaxWidth(), contentPadding = 14.dp) {
                SettingRow(Icons.Rounded.Notifications, stringResource(R.string.settings_reminder), stringResource(R.string.settings_reminder_sub)) {
                    Switch(
                        checked = reminder,
                        onCheckedChange = { reminder = it; vm.settings.reminder = it },
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = c.primary, checkedThumbColor = WoodPalette.Washi,
                            checkedBorderColor = c.edge, uncheckedBorderColor = c.line,
                            uncheckedTrackColor = c.crate, uncheckedThumbColor = c.textSub,
                        ),
                    )
                }
                Spacer(Modifier.height(10.dp))
                // ข้อ 3.2: ล็อกแนวตั้ง (ตั้งใน AndroidManifest.xml)
                SettingRow(Icons.Rounded.StayCurrentPortrait, stringResource(R.string.settings_portrait), stringResource(R.string.settings_portrait_sub)) {
                    Icon(Icons.Rounded.Lock, null, tint = c.textSub)
                }
            }

            // ข้อ 4.3: ลบทั้งหมดต้องยืนยัน
            PaperCard(Modifier.fillMaxWidth(), onClick = { confirmWipe = true }, contentPadding = 14.dp) {
                Row(Modifier.heightIn(min = 32.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Delete, null, tint = c.dangerText)
                    Spacer(Modifier.width(12.dp))
                    Text(stringResource(R.string.settings_delete_all), style = MaterialTheme.typography.labelLarge, color = c.dangerText)
                }
            }

            Text(stringResource(R.string.settings_version, "1.0"), style = MaterialTheme.typography.labelSmall,
                color = c.textSub, modifier = Modifier.align(Alignment.CenterHorizontally))
        }
    }

    if (confirmWipe) {
        WoodDialog(
            face = SpecialFaces.Worried,
            title = stringResource(R.string.delete_all_title),
            message = stringResource(R.string.delete_message),
            confirmText = stringResource(R.string.delete_all_confirm),
            onConfirm = { vm.deleteAll(); confirmWipe = false },
            dismissText = stringResource(R.string.cancel),
            onDismiss = { confirmWipe = false },
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium, color = MoodTheme.colors.textSub,
        modifier = Modifier.padding(bottom = 10.dp))
}

@Composable
private fun Segmented(
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    icons: List<ImageVector>? = null,
) {
    val c = MoodTheme.colors
    Row(
        Modifier.fillMaxWidth().background(c.crate, RoundedCornerShape(18.dp)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEachIndexed { i, (key, label) ->
            val on = key == selected
            Row(
                Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp)
                    .then(if (on) Modifier.chunky(c.accent, WoodPalette.Bark, 14.dp, borderWidth = 2.5.dp, shadow = 3.dp) else Modifier)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(role = Role.RadioButton) { if (!on) onSelect(key) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val fg = if (on) WoodPalette.Ink else c.text
                icons?.getOrNull(i)?.let {
                    Icon(it, null, tint = fg, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp))
                }
                Text(label, style = MaterialTheme.typography.labelLarge, color = fg)
            }
        }
    }
}

@Composable
private fun SettingRow(icon: ImageVector, title: String, sub: String, trailing: @Composable () -> Unit) {
    val c = MoodTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = c.text)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.labelLarge, color = c.text)
            Text(sub, style = MaterialTheme.typography.bodySmall, color = c.textSub)
        }
        Spacer(Modifier.width(8.dp))
        trailing()
    }
}

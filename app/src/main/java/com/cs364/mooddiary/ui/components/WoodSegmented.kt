package com.cs364.mooddiary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.cs364.mooddiary.ui.theme.MoodTheme
import com.cs364.mooddiary.ui.theme.WoodPalette

/** ปุ่มสลับ 2–3 ตัวเลือก (เช่น สัปดาห์นี้ / เดือนนี้) */
@Composable
fun <T> WoodSegmented(
    options: List<Pair<T, String>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = MoodTheme.colors
    Row(
        modifier.fillMaxWidth().background(c.crate, RoundedCornerShape(18.dp)).padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (key, label) ->
            val on = key == selected
            Row(
                Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp)
                    .then(if (on) Modifier.chunky(c.accent, WoodPalette.Bark, 14.dp, borderWidth = 2.5.dp, shadow = 3.dp) else Modifier)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(role = Role.RadioButton) { if (!on) onSelect(key) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(label, style = MaterialTheme.typography.labelLarge, color = if (on) WoodPalette.Ink else c.text)
            }
        }
    }
}

package com.cs364.mooddiary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cs364.mooddiary.R
import com.cs364.mooddiary.ui.theme.MoodTheme
import com.cs364.mooddiary.ui.theme.WoodPalette

/** แท็บของเมนูล่าง */
enum class NavTab { HOME, CALENDAR, STATS, SETTINGS }

/**
 * แถบเมนูล่างแบบไม้ ใช้ร่วมกันทุกหน้าหลัก (หน้าหลัก / ปฏิทิน / สถิติ)
 * ปุ่ม + ไม้กลมลอยอยู่ตรงกลาง
 */
@Composable
fun BottomWoodNav(
    selected: NavTab,
    onTab: (NavTab) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = MoodTheme.colors
    Box(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 30.dp)
                .background(c.card, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .drawBehind { drawRect(c.line, size = Size(size.width, 3.dp.toPx())) }
                .navigationBarsPadding()
                .height(84.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NavItem(Icons.Rounded.Home, stringResource(R.string.nav_home), selected == NavTab.HOME, { onTab(NavTab.HOME) }, Modifier.weight(1f))
            NavItem(Icons.Rounded.CalendarMonth, stringResource(R.string.nav_calendar), selected == NavTab.CALENDAR, { onTab(NavTab.CALENDAR) }, Modifier.weight(1f))
            Spacer(Modifier.weight(1f))
            NavItem(Icons.Rounded.BarChart, stringResource(R.string.nav_stats), selected == NavTab.STATS, { onTab(NavTab.STATS) }, Modifier.weight(1f))
            NavItem(Icons.Rounded.Settings, stringResource(R.string.nav_settings), selected == NavTab.SETTINGS, { onTab(NavTab.SETTINGS) }, Modifier.weight(1f))
        }
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
private fun NavItem(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val c = MoodTheme.colors
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .semantics { this.selected = selected }
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
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold),
            color = if (selected) c.text else c.textSub, maxLines = 1,
        )
    }
}

package com.cs364.mooddiary.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cs364.mooddiary.R
import com.cs364.mooddiary.model.FaceSpec
import com.cs364.mooddiary.model.Mood
import com.cs364.mooddiary.ui.theme.MoodDimens
import com.cs364.mooddiary.ui.theme.MoodTheme
import com.cs364.mooddiary.ui.theme.WoodPalette

/* ---------- Modifier: ลายไม้ + ขอบหนา + เงาทึบ ---------- */

/** พื้นหลังลายไม้ฮิโนกิ (เส้นเสี้ยนไม้แนวตั้งบางๆ) */
fun Modifier.woodGrain(base: Color, grain: Color): Modifier = drawBehind {
    drawRect(base)
    val thick = 2.dp.toPx()
    var x = 0f
    val step = 23.dp.toPx()
    while (x < size.width) {
        drawRect(grain, topLeft = Offset(x, 0f), size = Size(thick, size.height)); x += step
    }
    x = 6.dp.toPx()
    val step2 = 9.dp.toPx()
    while (x < size.width) {
        drawRect(grain.copy(alpha = grain.alpha * 0.6f), topLeft = Offset(x, 0f), size = Size(1.dp.toPx(), size.height)); x += step2
    }
}

/** ลายไม้เข้มแนวนอน (ป้าย / ปุ่มไม้ / ลัง) */
fun Modifier.plankGrain(base: Color): Modifier = drawBehind {
    drawRect(base)
    val line = Color(0x24461E0A)
    var y = 0f
    val step = 13.dp.toPx()
    while (y < size.height) {
        drawRect(line, topLeft = Offset(0f, y), size = Size(size.width, 2.dp.toPx())); y += step
    }
}

/**
 * สไตล์หลักของธีม: ขอบหนา 3dp + เงาทึบด้านล่าง (เหมือนของเล่นไม้)
 * ใช้ก่อน .clip(...).clickable(...) เพื่อให้เงาไม่ถูกตัด
 */
fun Modifier.chunky(
    fill: Color,
    border: Color,
    radius: Dp = MoodDimens.radiusCard,
    borderWidth: Dp = MoodDimens.border,
    shadow: Dp = MoodDimens.shadow,
): Modifier = this
    .drawBehind {
        if (shadow > 0.dp) {
            val r = radius.toPx()
            drawRoundRect(border, topLeft = Offset(0f, shadow.toPx()), size = size, cornerRadius = CornerRadius(r, r))
        }
    }
    .background(fill, RoundedCornerShape(radius))
    .border(borderWidth, border, RoundedCornerShape(radius))

/* ---------- คอมโพเนนต์ ---------- */

@Composable
fun WoodBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val c = MoodTheme.colors
    Box(modifier.woodGrain(c.background, c.grain), content = content)
}

/** การ์ดกระดาษวาชิ */
@Composable
fun PaperCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    contentPadding: Dp = 14.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = MoodTheme.colors
    val shape = RoundedCornerShape(MoodDimens.radiusCard)
    Column(
        modifier
            .chunky(c.card, c.line)
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(contentPadding),
        content = content,
    )
}

enum class ButtonStyle { Wood, Primary, Paper, Danger }

/** ปุ่มอ้วนๆ ขอบหนา กดแล้วยุบ */
@Composable
fun ChunkyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ButtonStyle = ButtonStyle.Wood,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 56.dp,
) {
    val c = MoodTheme.colors
    val (fill, fg) = when (style) {
        ButtonStyle.Wood -> c.plank to c.onPlank
        ButtonStyle.Primary -> c.primary to c.onPrimary
        ButtonStyle.Paper -> WoodPalette.Washi to WoodPalette.Bark
        ButtonStyle.Danger -> c.danger to c.onDanger
    }
    val shape = RoundedCornerShape(MoodDimens.radiusButton)
    Row(
        modifier
            .heightIn(min = height)
            .alpha(if (enabled) 1f else 0.5f)
            .chunky(fill, c.edge, MoodDimens.radiusButton)
            .clip(shape)
            .then(if (style == ButtonStyle.Wood) Modifier.plankGrain(fill) else Modifier)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.titleSmall.copy(fontSize = 18.sp), color = fg)
    }
}

/** ปุ่มไอคอนสี่เหลี่ยมมุมมน (ปุ่มย้อนกลับ) */
@Composable
fun IconSquareButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = MoodTheme.colors
    Box(
        modifier
            .size(MoodDimens.touch)
            .chunky(c.card, c.line, MoodDimens.radiusSmall, shadow = 3.dp)
            .clip(RoundedCornerShape(MoodDimens.radiusSmall))
            .clickable(role = Role.Button, onClickLabel = contentDescription, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = contentDescription, tint = c.text, modifier = Modifier.size(24.dp))
    }
}

@Composable
fun BackButton(onClick: () -> Unit, modifier: Modifier = Modifier) =
    IconSquareButton(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.back), onClick, modifier)

/** ป้ายชื่ออารมณ์สีตามผลไม้ */
@Composable
fun MoodPill(mood: Mood, modifier: Modifier = Modifier, extra: String? = null) {
    Text(
        text = if (extra == null) stringResource(mood.label) else stringResource(mood.label) + " · " + extra,
        modifier = modifier
            .background(mood.face.color, RoundedCornerShape(50))
            .border(2.dp, WoodPalette.Bark, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 2.dp),
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
        color = WoodPalette.Ink,
        maxLines = 1,
    )
}

/** ป้ายไม้แขวนชื่อแอป + ตราฮังโกะ 心 */
@Composable
fun HangingSign(title: String, modifier: Modifier = Modifier) {
    val c = MoodTheme.colors
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(120.dp)) {
            repeat(2) { Box(Modifier.width(3.dp).height(18.dp).background(c.line, RoundedCornerShape(2.dp))) }
        }
        Row(
            Modifier
                .chunky(c.plank, c.edge, 18.dp, shadow = 5.dp)
                .clip(RoundedCornerShape(18.dp))
                .plankGrain(c.plank)
                .padding(horizontal = 22.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, style = MaterialTheme.typography.headlineSmall, color = c.onPlank)
            HankoStamp()
        }
    }
}

@Composable
fun HankoStamp(modifier: Modifier = Modifier) {
    Box(
        modifier
            .rotate(-8f)
            .size(30.dp)
            .background(WoodPalette.Hanko, RoundedCornerShape(8.dp))
            .border(2.dp, MoodTheme.colors.edge, RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Text("心", color = WoodPalette.Cream, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

/** ลังไม้ (กรอบไม้เข้ม + ด้านในสีอ่อน) เหมือนกล่องในเกม */
@Composable
fun WoodCrate(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val c = MoodTheme.colors
    Box(
        modifier
            .chunky(c.plank, c.edge, 30.dp, shadow = 6.dp)
            .clip(RoundedCornerShape(30.dp))
            .plankGrain(c.plank)
            .padding(12.dp),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .background(c.crate, RoundedCornerShape(22.dp))
                .border(MoodDimens.border, c.edge, RoundedCornerShape(22.dp))
                .padding(12.dp),
            content = content,
        )
    }
}

/**
 * หน้าต่างยืนยัน (ใช้กับการลบทุกจุด ข้อ 4.3 และการออกจากหน้าเขียน ข้อ 4.2)
 */
@Composable
fun WoodDialog(
    face: FaceSpec,
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    dismissText: String,
    onDismiss: () -> Unit,
    confirmStyle: ButtonStyle = ButtonStyle.Danger,
    dismissStyle: ButtonStyle = ButtonStyle.Paper,
) {
    val c = MoodTheme.colors
    Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .chunky(c.background, c.edge, 28.dp, shadow = 6.dp)
                .clip(RoundedCornerShape(28.dp))
                .woodGrain(c.background, c.grain)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MoodFace(face, size = 76.dp, blush = false)
            Text(title, style = MaterialTheme.typography.titleLarge, color = c.text, textAlign = TextAlign.Center)
            Text(message, style = MaterialTheme.typography.bodyMedium, color = c.textSub, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ChunkyButton(dismissText, onDismiss, Modifier.weight(1f), style = dismissStyle)
                ChunkyButton(confirmText, onConfirm, Modifier.weight(1f), style = confirmStyle)
            }
        }
    }
}

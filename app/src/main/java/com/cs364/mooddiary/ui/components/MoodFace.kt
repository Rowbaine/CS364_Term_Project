package com.cs364.mooddiary.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cs364.mooddiary.model.FaceSpec
import com.cs364.mooddiary.ui.theme.WoodPalette

private const val LEAF = "M52 19 C55 9 66 6 74 10 C69 18 60 21 52 19 Z"

private fun parse(d: String): Path = PathParser().parsePathString(d).toPath()

/**
 * ตัวละครผลไม้หน้าตาน่ารัก วาดด้วย Canvas (ไม่ใช้รูปภาพ จึงคมทุกขนาดจอ)
 * ใช้: MoodFace(Mood.HAPPY.face, size = 56.dp)
 */
@Composable
fun MoodFace(
    face: FaceSpec,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp,
    blush: Boolean = true,
) {
    val leaf = remember { parse(LEAF) }
    val eyeL = remember(face) { parse(face.eyeL) }
    val eyeR = remember(face) { parse(face.eyeR) }
    val mouth = remember(face) { parse(face.mouth) }
    val outline = WoodPalette.Bark

    Canvas(modifier.size(size)) {
        val s = this.size.minDimension / 100f
        scale(s, pivot = Offset.Zero) {
            val center = Offset(50f, 56f)
            drawCircle(face.color, radius = 38f, center = center)
            drawCircle(outline, radius = 38f, center = center, style = Stroke(width = 3.5f))
            drawPath(leaf, WoodPalette.Leaf)
            drawPath(leaf, outline, style = Stroke(width = 3f, join = StrokeJoin.Round))
            if (blush) {
                drawOval(WoodPalette.Blush.copy(alpha = 0.55f), topLeft = Offset(23f, 62f), size = Size(12f, 8f))
                drawOval(WoodPalette.Blush.copy(alpha = 0.55f), topLeft = Offset(65f, 62f), size = Size(12f, 8f))
            }
            val eyeStroke = Stroke(width = if (face.dotEyes) 7f else 3.5f, cap = StrokeCap.Round)
            drawPath(eyeL, outline, style = eyeStroke)
            drawPath(eyeR, outline, style = eyeStroke)
            drawPath(mouth, outline, style = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

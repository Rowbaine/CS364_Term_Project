package com.cs364.mooddiary.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cs364.mooddiary.R
import com.cs364.mooddiary.model.Mood
import com.cs364.mooddiary.ui.components.BackButton
import com.cs364.mooddiary.ui.components.ChunkyButton
import com.cs364.mooddiary.ui.components.MoodFace
import com.cs364.mooddiary.ui.components.PaperCard
import com.cs364.mooddiary.ui.components.WoodBackground
import com.cs364.mooddiary.ui.components.WoodCrate
import com.cs364.mooddiary.ui.components.chunky
import com.cs364.mooddiary.ui.theme.MoodTheme

@Composable
fun PickMoodScreen(initial: Mood, onBack: () -> Unit, onNext: (Mood) -> Unit) {
    var picked by rememberSaveable { mutableStateOf(initial) }
    val c = MoodTheme.colors

    WoodBackground(Modifier.fillMaxSize()) {
        Column(
            Modifier.align(Alignment.TopCenter).widthIn(max = 560.dp).fillMaxSize()
                .safeDrawingPadding().imePadding().padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    BackButton(onBack)
                    Spacer(Modifier.weight(1f))
                    StepDots(step = 1)
                    Spacer(Modifier.weight(1f))
                    Spacer(Modifier.width(48.dp))
                }
                Spacer(Modifier.height(22.dp))
                Text(
                    stringResource(R.string.pick_title),
                    modifier = Modifier.chunky(c.accent, c.line, 16.dp).padding(horizontal = 20.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleLarge, color = c.onAccent, textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.pick_sub), style = MaterialTheme.typography.bodyMedium, color = c.textSub)
                Spacer(Modifier.height(22.dp))

                WoodCrate(Modifier.fillMaxWidth()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Mood.entries.chunked(3).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { m ->
                                    MoodChoice(m, selected = m == picked, onClick = { picked = m }, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(22.dp))
                PaperCard(Modifier.fillMaxWidth(), contentPadding = 14.dp) {
                    Text(
                        stringResource(R.string.pick_selected, stringResource(picked.label), stringResource(picked.hint)),
                        style = MaterialTheme.typography.bodyMedium, color = c.text,
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
            ChunkyButton(
                text = stringResource(R.string.pick_next),
                onClick = { onNext(picked) },
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.AutoMirrored.Rounded.ArrowForward,
                height = 60.dp,
            )
            Spacer(Modifier.height(6.dp))
        }
    }
}

@Composable
private fun MoodChoice(mood: Mood, selected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val c = MoodTheme.colors
    val scale by animateFloatAsState(if (selected) 1.06f else 1f, label = "moodScale")
    Column(
        modifier
            .scale(scale)
            .then(if (selected) Modifier.chunky(c.card, c.line, 22.dp) else Modifier)
            .clip(RoundedCornerShape(22.dp))
            .semantics { this.selected = selected }
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        MoodFace(mood.face, size = 72.dp)
        Text(stringResource(mood.label), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = c.text)
    }
}

@Composable
fun StepDots(step: Int, total: Int = 2) {
    val c = MoodTheme.colors
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        for (i in 1..total) {
            Box(
                Modifier
                    .size(width = if (i == step) 26.dp else 10.dp, height = 10.dp)
                    .background(if (i == step) c.textSub else c.dash, RoundedCornerShape(6.dp)),
            )
        }
    }
}

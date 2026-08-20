/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dalelalmuslim.knote.ui.theme.LocalAppColors

@Composable
internal fun BreathingCircle(
    breathScale: Float,
    ringPhase: Float,
    bellAlpha: Float,
    sizeDp: Int
) {
    val colors = LocalAppColors.current
    Canvas(modifier = Modifier.size(sizeDp.dp)) {
        val center = Offset(size.width / 2, size.height / 2)
        val maxR   = size.minDimension / 2f

        if (bellAlpha > 0f) {
            drawCircle(
                color  = colors.accent.copy(alpha = bellAlpha * 0.5f),
                radius = maxR * 0.94f,
                center = center,
                style  = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        for (i in 0..2) {
            val phase = (ringPhase + i / 3f) % 1f
            val r = maxR * (0.42f + phase * 0.58f)
            val a = (1f - phase) * 0.22f
            drawCircle(
                color  = colors.accent.copy(alpha = a),
                radius = r,
                center = center,
                style  = Stroke(width = 1.2.dp.toPx())
            )
        }

        val coreR = maxR * 0.50f * breathScale
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    colors.accent.copy(alpha = 0.28f),
                    colors.accent.copy(alpha = 0.10f),
                    colors.accentContainer.copy(alpha = 0.04f)
                ),
                center = center,
                radius = coreR
            ),
            radius = coreR,
            center = center
        )
        drawCircle(
            color  = colors.accent.copy(alpha = 0.55f),
            radius = coreR * 0.18f,
            center = center
        )
    }
}

@Composable
internal fun SettingSection(label: String, content: @Composable () -> Unit) {
    val colors = LocalAppColors.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, color = colors.onSurfaceSecondary, fontSize = 12.sp,
             fontWeight = FontWeight.Medium, letterSpacing = 0.8.sp)
        content()
    }
}

@Composable
internal fun <T> TimerChipRow(
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        options.forEach { opt ->
            val isSel = opt == selected
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSel) colors.accentContainer else colors.surface)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { onSelect(opt) }
                    .padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Text(
                    text = label(opt),
                    color = if (isSel) colors.accent else colors.onSurfaceSecondary,
                    fontSize = 13.sp,
                    fontWeight = if (isSel) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    softWrap = false,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

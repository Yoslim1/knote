/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dalelalmuslim.knote.ui.theme.LocalAppColors
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dalelalmuslim.knote.data.MoodEntry
import com.dalelalmuslim.knote.ui.strings.AppStrings
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import java.time.LocalDate
import kotlinx.collections.immutable.ImmutableList

private enum class MoodPeriod { ALL, MONTH, WEEK }
private enum class TimeUnit { MINUTES, HOURS }
private enum class JournalUnit { ENTRIES, DAYS }

@Composable
internal fun StatsRow(
    meditatedMinutes: Int,
    journalEntries: Int,
    journalDays: Int,
    allMoods: ImmutableList<MoodEntry>,
    strings: AppStrings,
    hazeState: HazeState,
    glassStyle: HazeStyle,
    glassBorder: Color,
    isDark: Boolean
) {
    var moodPeriod  by remember { mutableStateOf(MoodPeriod.ALL) }
    var timeUnit    by remember { mutableStateOf(TimeUnit.MINUTES) }
    var journalUnit by remember { mutableStateOf(JournalUnit.ENTRIES) }

    val today   = LocalDate.now().toEpochDay()
    val avgMood = when (moodPeriod) {
        MoodPeriod.ALL   -> allMoods
        MoodPeriod.MONTH -> allMoods.filter { it.date >= today - 29 }
        MoodPeriod.WEEK  -> allMoods.filter { it.date >= today - 6 }
    }.let { list ->
        if (list.isNotEmpty()) list.map { it.mood }.average() else -1.0
    }

    val moodValue = if (avgMood < 0) "–" else {
        if (avgMood % 1.0 == 0.0) "${avgMood.toInt()}/5" else "%.1f/5".format(avgMood)
    }
    val moodLabel = when (moodPeriod) {
        MoodPeriod.ALL   -> strings.meditationMoodAll
        MoodPeriod.MONTH -> strings.meditationMoodMonth
        MoodPeriod.WEEK  -> strings.meditationMoodWeek
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Long practice reads better in hours, a first sitting in minutes — so
        // the chip lets its reader choose, the way the mood chip already does.
        val hours = meditatedMinutes / 60.0
        StatChip(
            emoji       = "🧘",
            value       = when {
                meditatedMinutes <= 0        -> "–"
                timeUnit == TimeUnit.MINUTES -> "$meditatedMinutes"
                hours % 1.0 == 0.0           -> "${hours.toInt()}"
                else                         -> "%.1f".format(hours)
            },
            label       = if (timeUnit == TimeUnit.MINUTES) strings.meditationMinutesLabel
                          else strings.meditationHoursLabel,
            hazeState   = hazeState,
            glassStyle  = glassStyle,
            glassBorder = glassBorder,
            isDark      = isDark,
            onClick     = {
                timeUnit = if (timeUnit == TimeUnit.MINUTES) TimeUnit.HOURS else TimeUnit.MINUTES
            },
            modifier    = Modifier.weight(1f)
        )
        val journalValue = if (journalUnit == JournalUnit.ENTRIES) journalEntries else journalDays
        StatChip(
            emoji       = "📖",
            value       = if (journalValue > 0) "$journalValue" else "–",
            label       = when {
                journalUnit == JournalUnit.DAYS && journalValue == 1 -> strings.meditationJournalDaySingular
                journalUnit == JournalUnit.DAYS                      -> strings.meditationJournalDayPlural
                journalValue == 1                                    -> strings.meditationJournalSingular
                else                                                 -> strings.meditationJournalPlural
            },
            hazeState   = hazeState,
            glassStyle  = glassStyle,
            glassBorder = glassBorder,
            isDark      = isDark,
            onClick     = {
                journalUnit = if (journalUnit == JournalUnit.ENTRIES) JournalUnit.DAYS
                              else JournalUnit.ENTRIES
            },
            modifier    = Modifier.weight(1f)
        )
        StatChip(
            emoji       = "😊",
            value       = moodValue,
            label       = moodLabel,
            hazeState   = hazeState,
            glassStyle  = glassStyle,
            glassBorder = glassBorder,
            isDark      = isDark,
            onClick     = { moodPeriod = when (moodPeriod) {
                MoodPeriod.ALL   -> MoodPeriod.MONTH
                MoodPeriod.MONTH -> MoodPeriod.WEEK
                MoodPeriod.WEEK  -> MoodPeriod.ALL
            }},
            modifier    = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatChip(
    emoji: String,
    value: String,
    label: String,
    hazeState: HazeState,
    glassStyle: HazeStyle,
    glassBorder: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val shape      = RoundedCornerShape(18.dp)
    val valueColor = LocalAppColors.current.onSurface
    val labelColor = LocalAppColors.current.onSurfaceSecondary

    Column(
        modifier = modifier
            .border(0.5.dp, glassBorder, shape)
            .clip(shape)
            .hazeEffect(hazeState, glassStyle)
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication        = null
                ) { onClick() } else Modifier
            )
            .padding(vertical = 14.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 22.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(5.dp))
        Text(
            text       = value,
            fontSize   = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color      = valueColor,
            textAlign  = TextAlign.Center
        )
        Text(
            text      = label,
            fontSize  = 10.sp,
            color     = labelColor,
            textAlign = TextAlign.Center
        )
    }
}

/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dalelalmuslim.knote.data.Task
import com.dalelalmuslim.knote.ui.strings.LocalAppStrings
import com.dalelalmuslim.knote.ui.theme.LocalAppColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle as JavaTextStyle

@Composable
internal fun AppointmentLinkChip(task: Task, onClick: () -> Unit) {
    val strings = LocalAppStrings.current
    val taskDate = LocalDate.ofEpochDay(task.date)
    val today    = LocalDate.now()
    val dateStr  = when (taskDate) {
        today                -> strings.today
        today.plusDays(1)    -> strings.tomorrow
        today.minusDays(1)   -> strings.yesterday
        else -> taskDate.dayOfWeek
            .getDisplayName(JavaTextStyle.SHORT, strings.locale)
            .replaceFirstChar { it.uppercase() } +
            ", " + taskDate.format(DateTimeFormatter.ofPattern("d. MMM", strings.locale))
    }
    val timeStr  = if (task.time.isNotEmpty()) " · ${task.time}" else ""
    val label    = "$dateStr$timeStr · ${task.title}"

    Surface(
        color    = NoteAccent.copy(alpha = 0.08f),
        shape    = RoundedCornerShape(0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector        = Icons.Outlined.CalendarMonth,
                contentDescription = null,
                tint               = NoteAccent,
                modifier           = Modifier.size(15.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text     = label,
                fontSize = 13.sp,
                color    = NoteAccent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(6.dp))
            Icon(
                imageVector        = Icons.Default.ChevronRight,
                contentDescription = strings.noteLinkGoToAppointment,
                tint               = NoteAccent,
                modifier           = Modifier.size(16.dp)
            )
        }
    }
}

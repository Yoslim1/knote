/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dalelalmuslim.knote.data.Note
import com.dalelalmuslim.knote.data.NoteType
import com.dalelalmuslim.knote.ui.components.AppTab
import com.dalelalmuslim.knote.ui.components.soundClick
import com.dalelalmuslim.knote.ui.components.BottomBar
import com.dalelalmuslim.knote.ui.components.DateSlider
import com.dalelalmuslim.knote.ui.screens.NoteColorPicker
import com.dalelalmuslim.knote.ui.screens.NoteEditorBarState
import com.dalelalmuslim.knote.ui.screens.matchesTypeFilter
import com.dalelalmuslim.knote.ui.strings.LocalAppStrings
import com.dalelalmuslim.knote.ui.theme.LocalAppColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.collections.immutable.ImmutableList

private val DefaultAccent = Color(0xFF5B5BD6)

@Composable
internal fun TodayTopBar(
    modifier: Modifier,
    selectedDate: LocalDate,
    onOpenCalendar: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val strings = LocalAppStrings.current
    val colors = LocalAppColors.current
    val today = LocalDate.now()
    val dayLabel = when (selectedDate) {
        today              -> strings.today
        today.plusDays(1)  -> strings.tomorrow
        today.minusDays(1) -> strings.yesterday
        else -> selectedDate.dayOfWeek.getDisplayName(
            java.time.format.TextStyle.FULL, strings.locale
        ).replaceFirstChar { it.uppercase() }
    }
    AppTopBar(
        modifier = modifier,
        title    = dayLabel,
        subtitle = selectedDate.format(DateTimeFormatter.ofPattern("d. MMMM yyyy", strings.locale)),
        trailing = {
            IconButton(onClick = soundClick(onOpenCalendar)) {
                Icon(Icons.Default.CalendarMonth, contentDescription = strings.calendarTitle, tint = colors.accent)
            }
            IconButton(onClick = soundClick(onOpenSettings)) {
                Icon(Icons.Default.Settings, contentDescription = strings.settings, tint = colors.accent)
            }
        }
    )
}

@Composable
internal fun NotesTopBar(
    modifier: Modifier,
    noteEditorActive: Boolean,
    editorBar: NoteEditorBarState?,
    selectedNoteIds: Set<Long>,
    notes: ImmutableList<Note>,
    onClearSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    onPinSelected: () -> Unit,
    onSetColor: (Int) -> Unit,
    /** Colour of the open note, for the editor's controls. */
    editorAccent: Color = DefaultAccent,
    onOpenTrash: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val strings = LocalAppStrings.current
    val colors = LocalAppColors.current
    // With every selected note already pinned, the action unpins instead.
    val allSelectedPinned = selectedNoteIds.isNotEmpty() &&
        notes.filter { it.id in selectedNoteIds }.all { it.isPinned }
    if (noteEditorActive && editorBar != null) {
        AppTopBar(
            modifier = modifier,
            title    = " ",
            subtitle = " ",
            leading  = {
                IconButton(onClick = soundClick(editorBar.onBack)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.back, tint = editorAccent)
                }
            },
            trailing = {
                TextButton(onClick = soundClick(editorBar.onToggle)) {
                    Text(
                        text       = if (editorBar.isEditing) strings.notesDone else strings.notesEdit,
                        color      = editorAccent,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        )
    } else if (selectedNoteIds.isNotEmpty()) {
        // The colours live in the bar itself: putting them in the list would
        // either sit out of sight or drag the reader away from the note.
        val selected = notes.filter { it.id in selectedNoteIds }
        Column(modifier = modifier) {
            AppTopBar(
            title    = strings.notesSelected(selectedNoteIds.size),
            leading  = {
                IconButton(onClick = soundClick(onClearSelection)) {
                    Icon(Icons.Default.Close, contentDescription = strings.cancel, tint = colors.accent)
                }
            },
            trailing = {
                // Grid cards cannot be swiped, so pinning lives here.
                IconButton(onClick = soundClick(onPinSelected)) {
                    Icon(
                        imageVector = if (allSelectedPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                        contentDescription = if (allSelectedPinned) strings.notesUnpinAction
                                             else strings.notesPinAction,
                        tint = colors.accent
                    )
                }
                IconButton(onClick = soundClick(onDeleteSelected)) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = strings.notesDeleteSelected, tint = colors.accent)
                }
            }
            )
            NoteColorPicker(
                current  = selected.map { it.color }.distinct().singleOrNull(),
                onPick   = onSetColor,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 10.dp)
            )
        }
    } else {
        AppTopBar(
            modifier = modifier,
            title    = strings.tabNotes,
            subtitle = strings.notesCountByType(
                notes.count { matchesTypeFilter(it, NoteType.LIST) },
                notes.count { matchesTypeFilter(it, NoteType.NOTE) }
            ),
            trailing = {
                IconButton(onClick = soundClick(onOpenTrash)) {
                    Icon(Icons.Default.Delete, contentDescription = strings.trash, tint = colors.accent)
                }
                IconButton(onClick = soundClick(onOpenSettings)) {
                    Icon(Icons.Default.Settings, contentDescription = strings.settings, tint = colors.accent)
                }
            }
        )
    }
}

@Composable
internal fun FinanceTopBar(
    modifier: Modifier,
    selectedDate: LocalDate,
    onOpenMonthlyOverview: () -> Unit,
    onOpenBudget: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val strings = LocalAppStrings.current
    val colors = LocalAppColors.current
    AppTopBar(
        modifier = modifier,
        title    = strings.tabFinance,
        subtitle = selectedDate.format(DateTimeFormatter.ofPattern("d. MMMM yyyy", strings.locale)),
        trailing = {
            IconButton(onClick = soundClick(onOpenMonthlyOverview)) {
                Icon(Icons.Default.BarChart, contentDescription = strings.monthlyOverviewTitle, tint = colors.accent)
            }
            IconButton(onClick = soundClick(onOpenBudget)) {
                Icon(Icons.Default.Savings, contentDescription = strings.budgetDialogTitle, tint = colors.accent)
            }
            IconButton(onClick = soundClick(onOpenSettings)) {
                Icon(Icons.Default.Settings, contentDescription = strings.settings, tint = colors.accent)
            }
        }
    )
}

@Composable
internal fun AppBottomBar(
    modifier: Modifier,
    currentTab: AppTab,
    selectedDate: LocalDate,
    financeTabEnabled: Boolean,
    glassDividerColor: Color,
    onDateSelected: (LocalDate) -> Unit,
    onTodayLongPress: () -> Unit,
    onTabChange: (AppTab) -> Unit,
    dateScrollTrigger: Int = 0
) {
    Column(modifier = modifier) {
        HorizontalDivider(color = glassDividerColor, thickness = 0.5.dp)
        if (currentTab == AppTab.TODAY || currentTab == AppTab.FINANCE) {
            DateSlider(
                selectedDate     = selectedDate,
                onDateSelected   = onDateSelected,
                onTodayLongPress = onTodayLongPress,
                scrollToSelectedTrigger = dateScrollTrigger
            )
        }
        BottomBar(
            currentTab  = currentTab,
            showFinance = financeTabEnabled,
            onTabChange = onTabChange
        )
    }
}

@Composable
private fun AppTopBar(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    onSubtitleClick: (() -> Unit)? = null,
    leading: (@Composable () -> Unit)? = null,
    titleIcon: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val colors = LocalAppColors.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(
                start  = if (leading != null) 4.dp else 20.dp,
                end    = 8.dp,
                top    = 12.dp,
                bottom = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading?.invoke()
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (titleIcon != null) {
                    titleIcon()
                    Spacer(Modifier.width(8.dp))
                }
                Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = colors.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (subtitle != null) {
                Text(
                    text     = subtitle,
                    fontSize = 13.sp,
                    color    = colors.onSurfaceSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = if (onSubtitleClick != null)
                        Modifier.clickable(onClick = onSubtitleClick)
                    else Modifier
                )
            }
        }
        trailing?.invoke()
    }
}

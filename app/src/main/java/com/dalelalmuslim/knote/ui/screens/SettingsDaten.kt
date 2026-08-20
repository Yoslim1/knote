/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.screens
import com.dalelalmuslim.knote.ui.components.*
import com.dalelalmuslim.knote.ui.*

import com.dalelalmuslim.knote.ui.strings.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dalelalmuslim.knote.ui.theme.LocalAppColors

@Composable
internal fun DatenSection(
    onDeleteFinanceData: () -> Unit,
    onDeleteAllTasks: () -> Unit,
    onDeleteAllAppointments: () -> Unit,
    onDeleteAllHabits: () -> Unit,
    onDeleteAllNotes: () -> Unit,
    onDeleteAllMindfulness: () -> Unit
) {
    val strings = LocalAppStrings.current
    val colors  = LocalAppColors.current
    var showDeleteFinanceDialog      by remember { mutableStateOf(false) }
    var showDeleteTasksDialog        by remember { mutableStateOf(false) }
    var showDeleteAppointmentsDialog by remember { mutableStateOf(false) }
    var showDeleteHabitsDialog       by remember { mutableStateOf(false) }
    var showDeleteNotesDialog        by remember { mutableStateOf(false) }
    var showDeleteMindfulnessDialog  by remember { mutableStateOf(false) }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(16.dp))
        SectionLabel(strings.sectionData)
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            DeleteRow(strings.deleteAllTasks)        { showDeleteTasksDialog = true }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = colors.divider)
            DeleteRow(strings.deleteAllAppointments) { showDeleteAppointmentsDialog = true }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = colors.divider)
            DeleteRow(strings.deleteAllHabits)       { showDeleteHabitsDialog = true }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = colors.divider)
            DeleteRow(strings.deleteAllNotes)        { showDeleteNotesDialog = true }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = colors.divider)
            DeleteRow(strings.deleteFinanceData)     { showDeleteFinanceDialog = true }
            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = colors.divider)
            DeleteRow(strings.deleteAllMindfulness)  { showDeleteMindfulnessDialog = true }
        }
        Spacer(Modifier.height(24.dp))
    }

    if (showDeleteFinanceDialog) {
        ConfirmDeleteDialog(
            title = strings.deleteFinanceDialogTitle,
            text  = strings.deleteFinanceDialogText,
            deleteLabel = strings.delete,
            cancelLabel = strings.cancel,
            onConfirm = { showDeleteFinanceDialog = false; onDeleteFinanceData() },
            onDismiss = { showDeleteFinanceDialog = false }
        )
    }
    if (showDeleteTasksDialog) {
        ConfirmDeleteDialog(
            title = strings.deleteAllTasksDialogTitle,
            text  = strings.deleteAllTasksDialogText,
            deleteLabel = strings.delete,
            cancelLabel = strings.cancel,
            onConfirm = { showDeleteTasksDialog = false; onDeleteAllTasks() },
            onDismiss = { showDeleteTasksDialog = false }
        )
    }
    if (showDeleteAppointmentsDialog) {
        ConfirmDeleteDialog(
            title = strings.deleteAllAppointmentsDialogTitle,
            text  = strings.deleteAllAppointmentsDialogText,
            deleteLabel = strings.delete,
            cancelLabel = strings.cancel,
            onConfirm = { showDeleteAppointmentsDialog = false; onDeleteAllAppointments() },
            onDismiss = { showDeleteAppointmentsDialog = false }
        )
    }
    if (showDeleteHabitsDialog) {
        ConfirmDeleteDialog(
            title = strings.deleteAllHabitsDialogTitle,
            text  = strings.deleteAllHabitsDialogText,
            deleteLabel = strings.delete,
            cancelLabel = strings.cancel,
            onConfirm = { showDeleteHabitsDialog = false; onDeleteAllHabits() },
            onDismiss = { showDeleteHabitsDialog = false }
        )
    }
    if (showDeleteNotesDialog) {
        ConfirmDeleteDialog(
            title = strings.deleteAllNotesDialogTitle,
            text  = strings.deleteAllNotesDialogText,
            deleteLabel = strings.delete,
            cancelLabel = strings.cancel,
            onConfirm = { showDeleteNotesDialog = false; onDeleteAllNotes() },
            onDismiss = { showDeleteNotesDialog = false }
        )
    }
    if (showDeleteMindfulnessDialog) {
        ConfirmDeleteDialog(
            title = strings.deleteAllMindfulnessDialogTitle,
            text  = strings.deleteAllMindfulnessDialogText,
            deleteLabel = strings.delete,
            cancelLabel = strings.cancel,
            onConfirm = { showDeleteMindfulnessDialog = false; onDeleteAllMindfulness() },
            onDismiss = { showDeleteMindfulnessDialog = false }
        )
    }
}

@Composable
private fun DeleteRow(label: String, onClick: () -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(Color(0xFFD32F2F).copy(alpha = 0.12f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(label, fontSize = 15.sp, color = Color(0xFFD32F2F), modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = colors.onSurfaceTertiary, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    text: String,
    deleteLabel: String,
    cancelLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    GlassAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text  = { Text(text) },
        confirmButton = {
            TextButton(onClick = soundClick(onConfirm)) {
                Text(deleteLabel, color = Color(0xFFD32F2F))
            }
        },
        dismissButton = {
            TextButton(onClick = soundClick(onDismiss)) { Text(cancelLabel) }
        }
    )
}

/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.components
import com.dalelalmuslim.knote.ui.StatusGreen
import com.dalelalmuslim.knote.ui.StatusRed
import com.dalelalmuslim.knote.ui.StatusYellow
import com.dalelalmuslim.knote.ui.components.soundClick

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dalelalmuslim.knote.data.Task
import com.dalelalmuslim.knote.data.TaskStatus
import java.time.LocalDate
import com.dalelalmuslim.knote.ui.strings.LocalAppStrings
import com.dalelalmuslim.knote.ui.theme.LocalAppColors



@Composable
fun TaskItem(
    task: Task,
    onStatusClick: () -> Unit,
    onTitleSave: (String) -> Unit,
    onMoveToTomorrow: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    onPickDate: (() -> Unit)? = null,
    isDraggable: Boolean = true,
    swipeResetTrigger: Int = 0,
    confirmDeleteEnabled: Boolean = true,
    onOpenLinkedNote: (() -> Unit)? = null
) {
    val strings = LocalAppStrings.current
    var showDeleteDialog      by remember { mutableStateOf(false) }
    var deleteResetTrigger    by remember { mutableIntStateOf(0) }
    val prevDeleteShown        = remember { mutableStateOf(false) }
    LaunchedEffect(showDeleteDialog) {
        if (prevDeleteShown.value && !showDeleteDialog) deleteResetTrigger++
        prevDeleteShown.value = showDeleteDialog
    }

    SwipeToReveal(
        startAction  = SwipeAction(Icons.Default.CalendarMonth, Color(0xFF1976D2), onButtonClick = onPickDate, onAction = onMoveToTomorrow),
        resetTrigger = swipeResetTrigger + deleteResetTrigger,
        endAction    = SwipeAction(Icons.Default.Delete, Color(0xFFD32F2F), onAction = {
            if (confirmDeleteEnabled) showDeleteDialog = true else onDelete()
        })
    ) {
        TaskContent(
            task               = task,
            onStatusClick      = onStatusClick,
            onTitleSave        = onTitleSave,
            modifier = modifier,
            isDraggable        = isDraggable,
            onOpenLinkedNote   = onOpenLinkedNote
        )
    }

    if (showDeleteDialog) {
        GlassAlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(strings.deleteTaskDialogTitle(task.isAppointment)) },
            text  = { Text(strings.deleteTaskDialogText(task.title)) },
            confirmButton = {
                TextButton(onClick = soundClick { showDeleteDialog = false; onDelete() }) {
                    Text(strings.delete, color = Color(0xFFD32F2F))
                }
            },
            dismissButton = {
                TextButton(onClick = soundClick { showDeleteDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}

@Composable
private fun TaskContent(
    task: Task,
    onStatusClick: () -> Unit,
    onTitleSave: (String) -> Unit,
    modifier: Modifier = Modifier,
    isDraggable: Boolean = true,
    onOpenLinkedNote: (() -> Unit)? = null
) {
    val strings      = LocalAppStrings.current
    val colors       = LocalAppColors.current
    var editText     by remember(task.id) { mutableStateOf(task.title) }
    var isFocused    by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val statusColor by animateColorAsState(
        targetValue = when (task.status) {
            TaskStatus.RED    -> StatusRed
            TaskStatus.YELLOW -> StatusYellow
            TaskStatus.GREEN  -> StatusGreen
        },
        label = "statusColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (task.isAppointment) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .then(
                        if (task.isDone) Modifier.background(StatusGreen, CircleShape)
                        else Modifier.border(2.dp, Color(0xFFAAAAAA), CircleShape)
                    )
                    .clickable { onStatusClick() },
                contentAlignment = Alignment.Center
            ) {
                if (task.isDone) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(statusColor, RoundedCornerShape(6.dp))
                    .border(1.5.dp, statusColor.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    .clickable { onStatusClick() },
                contentAlignment = Alignment.Center
            ) {
                if (task.status == TaskStatus.GREEN) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(Modifier.width(14.dp))

        BasicTextField(
            value = editText,
            onValueChange = { editText = it },
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { state ->
                    isFocused = state.isFocused
                    if (!state.isFocused) {
                        if (editText.isBlank()) editText = task.title
                        else onTitleSave(editText)
                    }
                },
            textStyle = TextStyle(fontSize = 16.sp, color = colors.onSurface),
            cursorBrush = SolidColor(if (isFocused) Color.Black else Color.Transparent),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
        )

        if (task.isAppointment) {
            Spacer(Modifier.width(6.dp))
            val isPast = LocalDate.ofEpochDay(task.date) <= LocalDate.now()
            val doneExpression = remember(task.id) {
                val exprs = strings.appointmentDoneExpressions
                exprs[kotlin.math.abs(task.id.hashCode()) % exprs.size]
            }
            val timeText = if (task.isDone && isPast) {
                doneExpression
            } else {
                task.time.ifEmpty { strings.allDay }
            }
            Text(
                text = timeText,
                fontSize = 13.sp,
                color = colors.onSurfaceSecondary
            )
            if (onOpenLinkedNote != null) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.Article,
                    contentDescription = null,
                    tint               = Color(0xFF3D5AFE),
                    modifier           = Modifier
                        .size(18.dp)
                        .clickable { onOpenLinkedNote() }
                )
            }
        }

        if (!task.isAppointment && isDraggable) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = null,
                tint = Color(0xFFCCCCCC),
                modifier = Modifier
                    .size(24.dp)
                    .then(modifier)
            )
        }
    }
}

/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.dialogs
import com.dalelalmuslim.knote.ui.components.soundClick

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import com.dalelalmuslim.knote.data.NoteType
import com.dalelalmuslim.knote.ui.components.GlassAlertDialog
import com.dalelalmuslim.knote.ui.strings.LocalAppStrings

@Composable
fun AddNoteDialog(
    onConfirm: (String, String, NoteType) -> Unit,
    onDismiss: () -> Unit
) {
    val strings        = LocalAppStrings.current
    var title         by remember { mutableStateOf("") }
    var content       by remember { mutableStateOf("") }
    var selectedType  by remember { mutableStateOf(NoteType.NOTE) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    GlassAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.dialogNewNote) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text(strings.placeholderTitle) },
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text(strings.placeholderContent) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NoteType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick  = soundClick { selectedType = type },
                            label    = { Text(strings.noteTypeName(type)) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick  = soundClick { if (title.isNotBlank()) { onConfirm(title, content, selectedType); onDismiss() } },
                enabled  = title.isNotBlank()
            ) { Text(strings.add) }
        },
        dismissButton = {
            TextButton(onClick = soundClick(onDismiss)) { Text(strings.cancel) }
        }
    )
}

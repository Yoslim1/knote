/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.screens

import com.dalelalmuslim.knote.ui.components.*

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dalelalmuslim.knote.ui.strings.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddCategoryDialog(
    onConfirm: (name: String, group: String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings       = LocalAppStrings.current
    var name          by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf(ALL_GROUPS.first()) }
    var expanded      by remember { mutableStateOf(false) }

    GlassAlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.addCategory) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text(strings.categoryNameHint) }, singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
                    OutlinedTextField(
                        value = selectedGroup, onValueChange = {}, readOnly = true,
                        label = { Text(strings.categoryGroupHint) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        ALL_GROUPS.forEach { group ->
                            DropdownMenuItem(text = { Text(group) }, onClick = soundClick { selectedGroup = group; expanded = false })
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = soundClick { if (name.isNotBlank()) onConfirm(name, selectedGroup) }, enabled = name.isNotBlank()) {
                Text(strings.add)
            }
        },
        dismissButton = { TextButton(onClick = soundClick(onDismiss)) { Text(strings.cancel) } }
    )
}

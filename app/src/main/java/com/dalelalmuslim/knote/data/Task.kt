/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable enum class TaskStatus { RED, YELLOW, GREEN }
@Serializable enum class TaskCategory { APPOINTMENT, TOP, MORE }

@Serializable
@Entity(tableName = "tasks", indices = [Index(value = ["date"])])
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val date: Long,
    val status: TaskStatus = TaskStatus.RED,
    val category: TaskCategory = TaskCategory.MORE,
    val slot: Int = 0,
    val sortOrder: Int = 0,
    val isAppointment: Boolean = false,
    val time: String = "",
    val isDone: Boolean = false,
    val linkedNoteId: Long? = null
)

/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.data.backup

import com.dalelalmuslim.knote.data.AdditionalIncome
import com.dalelalmuslim.knote.data.AppSettings
import com.dalelalmuslim.knote.data.CaffeineDose
import com.dalelalmuslim.knote.data.Category
import com.dalelalmuslim.knote.data.Expense
import com.dalelalmuslim.knote.data.GratitudeEntry
import com.dalelalmuslim.knote.data.Habit
import com.dalelalmuslim.knote.data.HabitLog
import com.dalelalmuslim.knote.data.MoodEntry
import com.dalelalmuslim.knote.data.Note
import com.dalelalmuslim.knote.data.RecurringCostHistory
import com.dalelalmuslim.knote.data.Task
import kotlinx.serialization.Serializable

@Serializable
data class BackupPayload(
    val schemaVersion: Int,
    val appVersion: String,
    val createdAt: Long,
    val tasks: List<Task> = emptyList(),
    val notes: List<Note> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val categories: List<Category> = emptyList(),
    val settings: AppSettings? = null,
    val habits: List<Habit> = emptyList(),
    val habitLogs: List<HabitLog> = emptyList(),
    val gratitude: List<GratitudeEntry> = emptyList(),
    val moods: List<MoodEntry> = emptyList(),
    val caffeineDoses: List<CaffeineDose> = emptyList(),
    val recurringCostHistory: List<RecurringCostHistory> = emptyList(),
    val additionalIncomes: List<AdditionalIncome> = emptyList(),
)

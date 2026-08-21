/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey val id: Int = 1,
    val financeTabEnabled: Boolean = true,
    /** Whether the mindfulness view can be reached at all. */
    val mindfulnessEnabled: Boolean = true,
    val themeMode: String = "SYSTEM",
    val fontScale: Float  = 1.0f,
    val language: String  = "AUTO",
    val salary: Double    = 0.0,
    val salaryDay: String = "FIRST",
    val confirmDeleteEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val totalMeditatedMinutes: Int = 0,
    val desiredBedtimeMinutes: Int = 23 * 60,
    val caffeineMetabolism: String = "NORMAL",
    val currency: String = "EUR",
    val appLockTimeoutSeconds: Int = 0,
    val notificationsEnabled: Boolean = true,
    val notificationLeadMinutes: Int = 15,
    val blockScreenshots: Boolean = false,
    val showHolidays: Boolean = false,
    val holidayCountry: String = "",
    val holidayRegion: String = "",
    val includeHolidaysInExport: Boolean = true,
    /** Where the cursor starts in a new note: the title, or straight into the text. */
    val newNoteStartsWithTitle: Boolean = false,
    /** Whether the pointer to the mindfulness view has been shown already. */
    val mindfulnessHintSeen: Boolean = false,
)

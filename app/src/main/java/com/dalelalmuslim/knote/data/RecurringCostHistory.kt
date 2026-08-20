/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "recurring_cost_history")
data class RecurringCostHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: String,
    val categoryName: String,
    val amount: Double,
    val startMonth: String,
    val endMonth: String?
)

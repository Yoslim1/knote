/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "additional_incomes", indices = [Index(value = ["date"])])
data class AdditionalIncome(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: Long,
    val label: String,
    val amount: Double
)

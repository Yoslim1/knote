/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "gratitude_entries")
data class GratitudeEntry(
    @PrimaryKey val date: Long,
    val entry1: String = "",
    val entry2: String = "",
    val entry3: String = ""
) {
    val filledCount: Int get() = listOf(entry1, entry2, entry3).count { it.isNotBlank() }
    val isFull: Boolean get() = filledCount == 3
}

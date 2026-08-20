/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GratitudeDao {
    @Query("SELECT * FROM gratitude_entries WHERE date = :epochDay")
    fun observeForDate(epochDay: Long): Flow<GratitudeEntry?>

    @Query("SELECT * FROM gratitude_entries ORDER BY date DESC")
    fun observeAll(): Flow<List<GratitudeEntry>>

    @Query("SELECT * FROM gratitude_entries ORDER BY date ASC")
    suspend fun getAllOnce(): List<GratitudeEntry>

    @Query("SELECT * FROM gratitude_entries WHERE date >= :fromEpochDay ORDER BY date DESC")
    fun observeFrom(fromEpochDay: Long): Flow<List<GratitudeEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: GratitudeEntry)

    @Query("DELETE FROM gratitude_entries WHERE date = :epochDay")
    suspend fun deleteByDate(epochDay: Long)

    @Query("DELETE FROM gratitude_entries")
    suspend fun deleteAll()
}

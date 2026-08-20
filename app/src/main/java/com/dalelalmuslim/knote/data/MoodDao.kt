/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodDao {
    @Query("SELECT * FROM mood_entries WHERE date = :epochDay")
    fun observeForDate(epochDay: Long): Flow<MoodEntry?>

    @Query("SELECT * FROM mood_entries ORDER BY date DESC LIMIT 30")
    fun observeRecent(): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries ORDER BY date DESC")
    fun observeAll(): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries ORDER BY date ASC")
    suspend fun getAllOnce(): List<MoodEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: MoodEntry)

    @Query("DELETE FROM mood_entries")
    suspend fun deleteAll()
}

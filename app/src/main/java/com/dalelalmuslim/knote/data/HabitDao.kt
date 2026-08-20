/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY sortOrder ASC")
    fun observeAll(): Flow<List<Habit>>

    @Query("SELECT habitId FROM habit_logs WHERE date = :epochDay")
    fun observeCompletionsForDate(epochDay: Long): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(habit: Habit)

    @Delete
    suspend fun delete(habit: Habit)

    @Update
    suspend fun update(habit: Habit)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun logCompletion(log: HabitLog)

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND date = :epochDay")
    suspend fun removeCompletion(habitId: String, epochDay: Long)

    @Query("SELECT * FROM habit_logs ORDER BY date ASC")
    fun observeAllLogs(): Flow<List<HabitLog>>

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    @Query("DELETE FROM habit_logs")
    suspend fun deleteAllHabitLogs()
}

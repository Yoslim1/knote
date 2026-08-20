/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE date = :epochDay ORDER BY sortOrder ASC, id ASC")
    fun getTasksForDate(epochDay: Long): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE date = :epochDay ORDER BY sortOrder ASC, id ASC")
    suspend fun getTasksForDateOnce(epochDay: Long): List<Task>

    @Query("SELECT * FROM tasks ORDER BY date ASC, sortOrder ASC")
    suspend fun getAllTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE date >= :startDate AND date <= :endDate ORDER BY date ASC")
    fun getTasksForRange(startDate: Long, endDate: Long): Flow<List<Task>>

    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM tasks WHERE isAppointment = 0")
    suspend fun deleteAllTasks()

    @Query("DELETE FROM tasks WHERE isAppointment = 1")
    suspend fun deleteAllAppointments()

    @Query("SELECT * FROM tasks WHERE isAppointment = 1 ORDER BY date ASC, time ASC")
    fun getAllAppointments(): Flow<List<Task>>

    /** One-shot variant for headless reminder rescheduling (e.g. after a reboot). */
    @Query("SELECT * FROM tasks WHERE isAppointment = 1 ORDER BY date ASC, time ASC")
    suspend fun getAllAppointmentsOnce(): List<Task>

    @Query("UPDATE tasks SET linkedNoteId = NULL WHERE linkedNoteId = :noteId")
    suspend fun clearLinkedNoteId(noteId: Long)

    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): Task?

    @Query("UPDATE tasks SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: TaskStatus)
}

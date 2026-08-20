/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringCostHistoryDao {
    @Query("SELECT * FROM recurring_cost_history ORDER BY startMonth DESC")
    fun getAll(): Flow<List<RecurringCostHistory>>

    @Query("SELECT * FROM recurring_cost_history WHERE categoryId = :categoryId AND endMonth IS NULL LIMIT 1")
    suspend fun getActiveForCategory(categoryId: String): RecurringCostHistory?

    @Insert
    suspend fun insert(entry: RecurringCostHistory): Long

    @Update
    suspend fun update(entry: RecurringCostHistory)

    @Query("DELETE FROM recurring_cost_history WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM recurring_cost_history WHERE categoryId = :categoryId")
    suspend fun deleteForCategory(categoryId: String)

    @Query("DELETE FROM recurring_cost_history")
    suspend fun deleteAll()

    @Query("DELETE FROM recurring_cost_history WHERE categoryId IN (SELECT id FROM categories WHERE isDefault = 0)")
    suspend fun deleteForCustomCategories()
}

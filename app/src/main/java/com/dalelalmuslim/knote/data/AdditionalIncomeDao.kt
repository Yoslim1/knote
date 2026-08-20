/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AdditionalIncomeDao {
    @Query("SELECT * FROM additional_incomes WHERE date = :epochDay ORDER BY id ASC")
    fun getForDate(epochDay: Long): Flow<List<AdditionalIncome>>

    @Query("SELECT * FROM additional_incomes WHERE date >= :from AND date <= :to ORDER BY date ASC")
    fun getForRange(from: Long, to: Long): Flow<List<AdditionalIncome>>

    @Insert
    suspend fun insert(entry: AdditionalIncome): Long

    @Delete
    suspend fun delete(entry: AdditionalIncome)

    @Query("DELETE FROM additional_incomes")
    suspend fun deleteAll()
}

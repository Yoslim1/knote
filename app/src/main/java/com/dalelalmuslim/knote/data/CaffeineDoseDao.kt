/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CaffeineDoseDao {
    @Query("SELECT * FROM caffeine_doses WHERE timeMillis >= :fromMillis AND timeMillis <= :toMillis ORDER BY timeMillis ASC")
    fun observeInRange(fromMillis: Long, toMillis: Long): Flow<List<CaffeineDose>>

    @Query("SELECT * FROM caffeine_doses ORDER BY timeMillis DESC LIMIT 50")
    fun observeRecent(): Flow<List<CaffeineDose>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(dose: CaffeineDose): Long

    @Query("DELETE FROM caffeine_doses WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM caffeine_doses")
    suspend fun deleteAll()
}

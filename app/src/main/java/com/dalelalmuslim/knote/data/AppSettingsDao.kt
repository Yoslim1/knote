/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun observe(): Flow<AppSettings?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getOnce(): AppSettings?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDefault(settings: AppSettings)

    @Update
    suspend fun update(settings: AppSettings)

    @Query("UPDATE app_settings SET totalMeditatedMinutes = totalMeditatedMinutes + :minutes WHERE id = 1")
    suspend fun addMeditatedMinutes(minutes: Int)
}

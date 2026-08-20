/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dalelalmuslim.knote.data.AppDatabase
import com.dalelalmuslim.knote.data.GratitudeEntry
import com.dalelalmuslim.knote.data.MoodEntry
import com.dalelalmuslim.knote.service.MeditationService
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import java.time.LocalDate

class MeditationViewModel(app: Application) : AndroidViewModel(app) {

    private val db = AppDatabase.getInstance(app)

    val timerState = MeditationService.state

    val todayGratitude: StateFlow<GratitudeEntry?> =
        db.gratitudeDao().observeForDate(LocalDate.now().toEpochDay())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allGratitude: StateFlow<ImmutableList<GratitudeEntry>> =
        db.gratitudeDao().observeAll()
            .map { it.toImmutableList() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    val weekGratitude: StateFlow<ImmutableList<GratitudeEntry>> =
        db.gratitudeDao().observeFrom(LocalDate.now().minusDays(6).toEpochDay())
            .map { it.toImmutableList() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    val monthGratitude: StateFlow<ImmutableList<GratitudeEntry>> =
        db.gratitudeDao().observeFrom(LocalDate.now().minusDays(29).toEpochDay())
            .map { it.toImmutableList() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    val todayMood: StateFlow<MoodEntry?> =
        db.moodDao().observeForDate(LocalDate.now().toEpochDay())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val recentMoods: StateFlow<ImmutableList<MoodEntry>> =
        db.moodDao().observeRecent()
            .map { it.toImmutableList() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    val allMoods: StateFlow<ImmutableList<MoodEntry>> =
        db.moodDao().observeAll()
            .map { it.toImmutableList() }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), persistentListOf())

    val totalMeditatedMinutes: StateFlow<Int> =
        db.appSettingsDao().observe()
            .map { it?.totalMeditatedMinutes ?: 0 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun startMeditation(durationMinutes: Int, bellIntervalMinutes: Int, sound: String) {
        val ctx = getApplication<Application>()
        ctx.startForegroundService(Intent(ctx, MeditationService::class.java).apply {
            action = MeditationService.ACTION_START
            putExtra(MeditationService.EXTRA_DURATION, durationMinutes)
            putExtra(MeditationService.EXTRA_BELL_INTERVAL, bellIntervalMinutes)
            putExtra(MeditationService.EXTRA_SOUND, sound)
        })
    }

    fun stopMeditation() = sendAction(MeditationService.ACTION_STOP)
    fun pauseMeditation() = sendAction(MeditationService.ACTION_PAUSE)
    fun resumeMeditation() = sendAction(MeditationService.ACTION_RESUME)

    private fun sendAction(action: String) {
        val ctx = getApplication<Application>()
        ctx.startService(Intent(ctx, MeditationService::class.java).apply { this.action = action })
    }

    fun saveGratitude(e1: String, e2: String, e3: String) {
        viewModelScope.launch {
            db.gratitudeDao().upsert(GratitudeEntry(LocalDate.now().toEpochDay(), e1.trim(), e2.trim(), e3.trim()))
        }
    }

    fun saveMood(mood: Int) {
        viewModelScope.launch {
            db.moodDao().upsert(MoodEntry(LocalDate.now().toEpochDay(), mood))
        }
    }

    fun exportMindfulnessData(callback: (List<GratitudeEntry>, List<MoodEntry>) -> Unit) {
        viewModelScope.launch {
            callback(db.gratitudeDao().getAllOnce(), db.moodDao().getAllOnce())
        }
    }

    fun deleteGratitude(date: Long) {
        viewModelScope.launch { db.gratitudeDao().deleteByDate(date) }
    }

    fun deleteAllMindfulnessData() {
        viewModelScope.launch {
            db.gratitudeDao().deleteAll()
            db.moodDao().deleteAll()
        }
    }
}

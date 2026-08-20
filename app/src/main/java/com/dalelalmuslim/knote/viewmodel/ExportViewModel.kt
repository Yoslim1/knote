/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.viewmodel

import android.app.Application
import android.net.Uri
import android.provider.DocumentsContract
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dalelalmuslim.knote.export.ExportFormat
import com.dalelalmuslim.knote.export.ExportOptions
import com.dalelalmuslim.knote.export.Exporter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.OutputStream

class ExportViewModel(app: Application) : AndroidViewModel(app) {

    sealed interface UiState {
        data object Idle : UiState
        data class Running(val current: Int, val total: Int, val isNotes: Boolean) : UiState
        data class Success(val uri: Uri, val mimeType: String) : UiState
        data class Error(val message: String?) : UiState
    }

    private val _state = MutableStateFlow<UiState>(UiState.Idle)
    val state = _state.asStateFlow()

    private var job: Job? = null

    fun export(exporter: Exporter, format: ExportFormat, options: ExportOptions, target: Uri) {
        if (job?.isActive == true) return
        job = viewModelScope.launch(Dispatchers.IO) {
            _state.value = UiState.Running(0, 1, exporter.category == com.dalelalmuslim.knote.export.ExportCategory.NOTIZEN)
            var out: OutputStream? = null
            try {
                val resolver = getApplication<Application>().contentResolver
                out = resolver.openOutputStream(target)
                    ?: throw IOException("OutputStream konnte nicht geöffnet werden")
                exporter.write(out, format, options) { current, total ->
                    _state.value = UiState.Running(current, total, exporter.category == com.dalelalmuslim.knote.export.ExportCategory.NOTIZEN)
                }
                _state.value = UiState.Success(target, exporter.mimeType(format))
            } catch (ce: CancellationException) {
                deleteQuietly(target)
                throw ce
            } catch (t: Throwable) {
                deleteQuietly(target)
                _state.value = UiState.Error(t.message)
            } finally {
                try { out?.close() } catch (_: Exception) { }
            }
        }
    }

    fun cancel() {
        job?.cancel()
        _state.value = UiState.Idle
    }

    fun consume() {
        if (state.value !is UiState.Running) _state.value = UiState.Idle
    }

    private fun deleteQuietly(uri: Uri) {
        try {
            DocumentsContract.deleteDocument(getApplication<Application>().contentResolver, uri)
        } catch (_: Exception) { }
    }
}

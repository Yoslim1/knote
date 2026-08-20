/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import com.dalelalmuslim.knote.data.AppDatabase
import com.dalelalmuslim.knote.data.backup.BackupCodec
import com.dalelalmuslim.knote.data.backup.BackupCorruptException
import com.dalelalmuslim.knote.data.backup.BackupRepository
import com.dalelalmuslim.knote.data.backup.IncompatibleSchemaException
import com.dalelalmuslim.knote.data.backup.WrongBackupPasswordException
import com.dalelalmuslim.knote.security.wipe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed interface ImportOutcome {
    data object Success : ImportOutcome
    data object WrongPassword : ImportOutcome
    data object Corrupt : ImportOutcome
    data class Incompatible(val foundVersion: Int) : ImportOutcome
    data class Error(val message: String?) : ImportOutcome
}

sealed interface ExportOutcome {
    data object Success : ExportOutcome
    data class Error(val message: String?) : ExportOutcome
}

class BackupViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = BackupRepository(AppDatabase.getInstance(app))
    private val appVersion: String =
        runCatching {
            app.packageManager.getPackageInfo(app.packageName, 0).versionName
        }.getOrNull() ?: "?"

    suspend fun export(context: Context, uri: Uri, password: CharArray): ExportOutcome = try {
        val payload = repo.buildPayload(appVersion)
        val jsonBytes = repo.encode(payload)
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.use { os ->
                BackupCodec.encrypt(os, password, jsonBytes)
            } ?: throw IllegalStateException("Konnte Datei nicht zum Schreiben öffnen")
        }
        jsonBytes.fill(0)
        ExportOutcome.Success
    } catch (e: Exception) {
        ExportOutcome.Error(e.message)
    } finally {
        password.wipe()
    }

    suspend fun import(context: Context, uri: Uri, password: CharArray, replace: Boolean): ImportOutcome = try {
        val jsonBytes = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { ins ->
                BackupCodec.decrypt(ins, password)
            } ?: throw IllegalStateException("Konnte Datei nicht zum Lesen öffnen")
        }
        val payload = repo.decode(jsonBytes)
        jsonBytes.fill(0)
        if (payload.schemaVersion > BackupRepository.SCHEMA_VERSION) {
            ImportOutcome.Incompatible(payload.schemaVersion)
        } else {
            repo.restore(payload, replace)
            ImportOutcome.Success
        }
    } catch (e: WrongBackupPasswordException) {
        ImportOutcome.WrongPassword
    } catch (e: BackupCorruptException) {
        ImportOutcome.Corrupt
    } catch (e: IncompatibleSchemaException) {
        ImportOutcome.Incompatible(e.foundVersion)
    } catch (e: Exception) {
        ImportOutcome.Error(e.message)
    } finally {
        password.wipe()
    }
}

/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.data

import android.content.Context
import com.dalelalmuslim.knote.security.wipe

object DatabaseProvider {

    @Volatile private var database: AppDatabase? = null
    @Volatile private var dekRef: ByteArray? = null

    val isOpen: Boolean get() = database != null

    fun open(context: Context, dek: ByteArray): AppDatabase {
        database?.let { dek.wipe(); return it }
        return synchronized(this) {
            val existing = database
            if (existing != null) {
                dek.wipe()
                existing
            } else {
                AppDatabase.build(context.applicationContext, dek).also {
                    database = it
                    dekRef = dek
                }
            }
        }
    }

    fun requireDatabase(): AppDatabase =
        database ?: throw IllegalStateException("Datenbank ist gesperrt – DEK liegt noch nicht vor")

    fun currentDek(): ByteArray? = dekRef

    fun close() = synchronized(this) {
        database?.close()
        database = null
        dekRef?.wipe()
        dekRef = null
    }
}

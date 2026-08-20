/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabaseEncryptionTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val sidecars = listOf("", "-wal", "-shm", "-journal")

    @Before fun clean() {
        DatabaseProvider.close()
        sidecars.forEach { context.getDatabasePath("knote.db$it").delete() }
    }

    @After fun cleanup() {
        sidecars.forEach { context.getDatabasePath("knote.db$it").delete() }
    }

    @Test fun opensWithCorrectDek() = runBlocking {
        val dek = ByteArray(32) { it.toByte() }
        val db = AppDatabase.build(context, dek)
        db.appSettingsDao().insertDefault(AppSettings(id = 1, salary = 1234.0))
        val settings = db.appSettingsDao().getOnce()
        assertNotNull(settings)
        assertEquals(1234.0, settings!!.salary, 0.0001)
        db.close()
    }

    @Test fun failsWithWrongDek() = runBlocking {
        val dek = ByteArray(32) { it.toByte() }
        val db = AppDatabase.build(context, dek)
        db.appSettingsDao().insertDefault(AppSettings(id = 1))
        db.close()

        val wrongDek = ByteArray(32) { (it + 7).toByte() }
        val db2 = AppDatabase.build(context, wrongDek)
        assertThrows(Exception::class.java) {
            runBlocking { db2.appSettingsDao().getOnce() }
        }
        db2.close()
    }
}

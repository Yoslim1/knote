/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import android.content.Context
import net.zetetic.database.sqlcipher.SQLiteDatabase as CipherDb

@RunWith(AndroidJUnit4::class)
class PlaintextMigrationInstrumentedTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val sidecars = listOf("", "-wal", "-shm", "-journal")

    @Before fun clean() {
        DatabaseProvider.close()
        sidecars.forEach { context.getDatabasePath("knote.db$it").delete() }
    }

    @After fun cleanup() {
        sidecars.forEach { context.getDatabasePath("knote.db$it").delete() }
    }

    @Test fun plaintextDb_isMigrated_andDataSurvives() {
        val dbFile = context.getDatabasePath("knote.db")
        dbFile.parentFile?.mkdirs()

        val plain = android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(dbFile, null)
        plain.execSQL("CREATE TABLE app_settings (id INTEGER PRIMARY KEY, salary REAL)")
        plain.execSQL("INSERT INTO app_settings (id, salary) VALUES (1, 4242.0)")
        plain.version = 28
        plain.close()

        assertTrue(PlaintextDbMigration.isPlaintextDbPresent(context))

        val dek = ByteArray(32) { (it * 7 + 1).toByte() }
        PlaintextDbMigration.migrate(context, dek)

        assertFalse(PlaintextDbMigration.isPlaintextDbPresent(context))

        val enc = CipherDb.openOrCreateDatabase(dbFile, SqlCipherKey.rawKeyBytes(dek), null, null)
        try {
            enc.rawQuery("SELECT salary FROM app_settings WHERE id = 1", null).use { c ->
                assertTrue(c.moveToFirst())
                assertEquals(4242.0, c.getDouble(0), 0.0001)
            }
            assertEquals(28, enc.version)
        } finally {
            enc.close()
        }
    }
}

/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.data.backup

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.ByteArrayInputStream

class BackupHeaderTest {

    @Test
    fun decrypt_wrongMagic_throwsCorrupt() {
        val notABackup = ByteArray(32) { it.toByte() }
        assertThrows(BackupCorruptException::class.java) {
            runBlocking {
                BackupCodec.decrypt(ByteArrayInputStream(notABackup), "pw".toCharArray())
            }
        }
    }

    @Test
    fun decrypt_truncated_throwsCorrupt() {
        val tooShort = byteArrayOf('A'.code.toByte(), 'P'.code.toByte())
        assertThrows(BackupCorruptException::class.java) {
            runBlocking {
                BackupCodec.decrypt(ByteArrayInputStream(tooShort), "pw".toCharArray())
            }
        }
    }
}

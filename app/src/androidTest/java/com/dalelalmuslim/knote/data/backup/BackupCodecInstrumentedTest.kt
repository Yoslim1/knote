/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.data.backup

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@RunWith(AndroidJUnit4::class)
class BackupCodecInstrumentedTest {

    @Test fun encrypt_then_decrypt_roundtrip() {
        runBlocking {
            val payload = "{\"hello\":\"welt\",\"n\":42}".toByteArray(Charsets.UTF_8)
            val bos = ByteArrayOutputStream()
            BackupCodec.encrypt(bos, "s3cret-pass".toCharArray(), payload)

            val restored = BackupCodec.decrypt(ByteArrayInputStream(bos.toByteArray()), "s3cret-pass".toCharArray())
            assertArrayEquals(payload, restored)
        }
    }

    @Test fun decrypt_wrongPassword_throws() {
        val bos = ByteArrayOutputStream()
        runBlocking { BackupCodec.encrypt(bos, "right-pass".toCharArray(), "daten".toByteArray()) }

        assertThrows(WrongBackupPasswordException::class.java) {
            runBlocking {
                BackupCodec.decrypt(ByteArrayInputStream(bos.toByteArray()), "wrong-pass".toCharArray())
            }
        }
    }
}

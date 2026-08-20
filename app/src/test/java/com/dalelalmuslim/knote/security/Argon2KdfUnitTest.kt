/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.security

import com.lambdapioneer.argon2kt.Argon2Kt
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for [Argon2Kdf].
 *
 * Testability note: [Argon2Kdf.deriveKey] delegates to `argon2kt`, which loads a native
 * JNI library (`libargon2jni.so`). The `com.lambdapioneer.argon2kt:argon2kt` artifact only
 * ships that library for Android ABIs; there is no desktop-JVM build. On the plain JVM used
 * for local unit tests the native library is therefore unavailable and [Argon2Kdf.deriveKey]
 * would throw an [UnsatisfiedLinkError] if invoked.
 *
 * Consequently every derivation test below is gated by a JUnit [Assume] probe: it is skipped
 * cleanly wherever the native library is absent (the local test JVM) and would execute wherever
 * the native library is present. Always-executing end-to-end coverage of the same behaviour runs
 * in the instrumented tests (`KeyManagerInstrumentedTest.passphrase_roundtrip_and_wrongPassphrase_fails`
 * and `BackupCodecInstrumentedTest`), which execute against the real Android native library.
 *
 * These tests use only the actual public API of [Argon2Kdf]; no mocking is involved.
 */
class Argon2KdfUnitTest {

    @Before
    fun requireNativeArgon2() {
        val nativeAvailable = runCatching { Argon2Kt.assertJniWorking() }.isSuccess
        assumeTrue(
            "argon2kt is an Android-only JNI library; deriveKey cannot run on the desktop JVM",
            nativeAvailable,
        )
    }

    @Test
    fun deriveKey_withValidPasswordAndSalt_producesDocumentedHashLength() {
        val password = "correct horse battery staple".toByteArray(Charsets.UTF_8)
        val salt = ByteArray(Argon2Kdf.SALT_LENGTH_BYTES) { (it + 1).toByte() }

        val derived = runBlocking { Argon2Kdf.deriveKey(password, salt) }

        assertEquals(Argon2Kdf.HASH_LENGTH_BYTES, derived.size)
        assertTrue(derived.isNotEmpty())
    }

    @Test
    fun deriveKey_sameInput_isDeterministic() {
        val password = "correct horse battery staple".toByteArray(Charsets.UTF_8)
        val salt = ByteArray(Argon2Kdf.SALT_LENGTH_BYTES) { (it + 1).toByte() }

        val first = runBlocking { Argon2Kdf.deriveKey(password, salt) }
        val second = runBlocking { Argon2Kdf.deriveKey(password, salt) }

        assertArrayEquals(first, second)
    }

    @Test
    fun deriveKey_changedPassword_producesDifferentHash() {
        val salt = ByteArray(Argon2Kdf.SALT_LENGTH_BYTES) { (it + 1).toByte() }
        val correct = "correct horse battery staple".toByteArray(Charsets.UTF_8)
        val wrong = "wrong password".toByteArray(Charsets.UTF_8)

        val derivedCorrect = runBlocking { Argon2Kdf.deriveKey(correct, salt) }
        val derivedWrong = runBlocking { Argon2Kdf.deriveKey(wrong, salt) }

        assertNotEquals(derivedCorrect.contentToString(), derivedWrong.contentToString())
    }

    @Test
    fun deriveKey_changedSalt_producesDifferentHash() {
        val password = "correct horse battery staple".toByteArray(Charsets.UTF_8)
        val saltA = ByteArray(Argon2Kdf.SALT_LENGTH_BYTES) { 0x11 }
        val saltB = ByteArray(Argon2Kdf.SALT_LENGTH_BYTES) { 0x22 }

        val derivedA = runBlocking { Argon2Kdf.deriveKey(password, saltA) }
        val derivedB = runBlocking { Argon2Kdf.deriveKey(password, saltB) }

        assertNotEquals(derivedA.contentToString(), derivedB.contentToString())
    }

    @Test
    fun deriveKey_customValidParameters_areAccepted() {
        val password = "correct horse battery staple".toByteArray(Charsets.UTF_8)
        val salt = ByteArray(Argon2Kdf.SALT_LENGTH_BYTES) { (it + 1).toByte() }

        val derived = runBlocking {
            Argon2Kdf.deriveKey(
                password = password,
                salt = salt,
                memoryKiB = 16 * 1024,
                iterations = 2,
                parallelism = 2,
                hashLengthBytes = 48,
            )
        }

        assertEquals(48, derived.size)
    }

    @Test
    fun deriveKey_invalidParameters_throw() {
        val password = "correct horse battery staple".toByteArray(Charsets.UTF_8)
        val salt = ByteArray(Argon2Kdf.SALT_LENGTH_BYTES) { (it + 1).toByte() }

        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { Argon2Kdf.deriveKey(password, salt, memoryKiB = 0) }
        }
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { Argon2Kdf.deriveKey(password, salt, iterations = 0) }
        }
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { Argon2Kdf.deriveKey(password, salt, parallelism = 0) }
        }
        assertThrows(IllegalArgumentException::class.java) {
            runBlocking { Argon2Kdf.deriveKey(password, salt, hashLengthBytes = 0) }
        }
    }

    @Test
    fun deriveKey_emptySalt_throws() {
        val password = "correct horse battery staple".toByteArray(Charsets.UTF_8)
        val emptySalt = ByteArray(0)

        assertThrows(RuntimeException::class.java) {
            runBlocking { Argon2Kdf.deriveKey(password, emptySalt) }
        }
    }

    @Test
    fun deriveKey_emptyPassword_isAccepted() {
        val emptyPassword = ByteArray(0)
        val salt = ByteArray(Argon2Kdf.SALT_LENGTH_BYTES) { (it + 1).toByte() }

        val derived = runBlocking { Argon2Kdf.deriveKey(emptyPassword, salt) }

        assertEquals(Argon2Kdf.HASH_LENGTH_BYTES, derived.size)
    }
}

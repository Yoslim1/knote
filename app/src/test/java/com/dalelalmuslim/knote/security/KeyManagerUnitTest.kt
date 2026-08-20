/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.security

import android.content.Context
import android.content.SharedPreferences
import android.test.mock.MockContext
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Unit tests for [KeyManager].
 *
 * Testability note: [KeyManager] delegates all key-material handling to the Android security
 * stack:
 *  - the AndroidKeyStore provider (`KeyStore.getInstance("AndroidKeyStore")`) for no-lock /
 *    lock keys, which does not exist on a desktop JVM, and
 *  - [KeyStorage], which persists wrapped keys through `android.util.Base64`-encoded
 *    [SharedPreferences]; `android.util.Base64` is a non-functional stub in local unit tests.
 *
 * Any behaviour that wraps/unwraps, persists, or transitions key material therefore cannot be
 * executed reliably on the JVM, and is covered by the existing instrumented tests in
 * `androidTest/.../security/KeyManagerInstrumentedTest.kt` (real AndroidKeyStore, Argon2 native
 * library, real SharedPreferences).
 *
 * What IS reliably JVM-testable without the Android security stack is the uninitialized-state
 * guard contract: a freshly constructed [KeyManager] backed by an empty storage reports
 * not-initialized and refuses to report a mode. The tests below exercise that real contract via
 * the real public API, using a minimal in-memory [SharedPreferences] fake behind a stub
 * [MockContext] — no mocking framework is involved.
 */
class KeyManagerUnitTest {

    @Test
    fun isInitialized_whenStorageEmpty_returnsFalse() {
        val keyManager = KeyManager(stubContext())

        assertFalse(keyManager.isInitialized())
    }

    @Test
    fun currentMode_whenStorageEmpty_throwsIllegalStateException() {
        val keyManager = KeyManager(stubContext())

        assertThrows(IllegalStateException::class.java) { keyManager.currentMode() }
    }

    @Test
    fun requiresUserPresence_whenStorageEmpty_throwsIllegalStateException() {
        val keyManager = KeyManager(stubContext())

        assertThrows(IllegalStateException::class.java) { keyManager.requiresUserPresence() }
    }

    @Suppress("DEPRECATION")
    private fun stubContext(): Context {
        val prefs = InMemorySharedPreferences()
        return object : MockContext() {
            override fun getApplicationContext(): Context = this
            override fun getSharedPreferences(name: String, mode: Int): SharedPreferences = prefs
        }
    }

    /** Minimal in-memory [SharedPreferences] used only to read an empty store. */
    private class InMemorySharedPreferences : SharedPreferences {

        private val data = mutableMapOf<String, Any?>()

        override fun getString(key: String, defValue: String?): String? = data[key] as? String ?: defValue
        override fun getStringSet(key: String, defValue: Set<String>?): Set<String>? = data[key] as? MutableSet<String> ?: defValue
        override fun getInt(key: String, defValue: Int): Int = data[key] as? Int ?: defValue
        override fun getLong(key: String, defValue: Long): Long = data[key] as? Long ?: defValue
        override fun getFloat(key: String, defValue: Float): Float = data[key] as? Float ?: defValue
        override fun getBoolean(key: String, defValue: Boolean): Boolean = data[key] as? Boolean ?: defValue
        override fun contains(key: String): Boolean = data.containsKey(key)
        override fun getAll(): Map<String, *> = data
        override fun edit(): SharedPreferences.Editor = InMemoryEditor()

        override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit
        override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit

        private inner class InMemoryEditor : SharedPreferences.Editor {
            override fun putString(key: String, value: String?): SharedPreferences.Editor {
                if (value == null) data.remove(key) else data[key] = value
                return this
            }

            override fun putStringSet(key: String, values: MutableSet<String>?): SharedPreferences.Editor {
                if (values == null) data.remove(key) else data[key] = values
                return this
            }

            override fun putInt(key: String, value: Int): SharedPreferences.Editor {
                data[key] = value
                return this
            }

            override fun putLong(key: String, value: Long): SharedPreferences.Editor {
                data[key] = value
                return this
            }

            override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
                data[key] = value
                return this
            }

            override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
                data[key] = value
                return this
            }

            override fun remove(key: String): SharedPreferences.Editor {
                data.remove(key)
                return this
            }

            override fun clear(): SharedPreferences.Editor {
                data.clear()
                return this
            }

            override fun commit(): Boolean = true

            override fun apply() = Unit
        }
    }
}

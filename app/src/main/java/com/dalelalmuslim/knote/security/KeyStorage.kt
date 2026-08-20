/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.security

import android.content.Context
import android.util.Base64
import androidx.core.content.edit

class KeyStorage(context: Context) {

    private val prefs =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var mode: KeyMode?
        get() = prefs.getString(KEY_MODE, null)?.let { runCatching { KeyMode.valueOf(it) }.getOrNull() }
        set(value) = prefs.edit().apply {
            if (value == null) remove(KEY_MODE) else putString(KEY_MODE, value.name)
        }.let { it.apply() }

    var wrappedDek: ByteArray?
        get() = getBytes(KEY_WRAPPED_DEK)
        set(value) = setBytes(KEY_WRAPPED_DEK, value)

    var dekIv: ByteArray?
        get() = getBytes(KEY_DEK_IV)
        set(value) = setBytes(KEY_DEK_IV, value)

    var passphraseSalt: ByteArray?
        get() = getBytes(KEY_SALT)
        set(value) = setBytes(KEY_SALT, value)

    var argonMemoryKiB: Int
        get() = prefs.getInt(KEY_ARGON_MEM, Argon2Kdf.MEMORY_KIB)
        set(value) = prefs.edit { putInt(KEY_ARGON_MEM, value) }

    var argonIterations: Int
        get() = prefs.getInt(KEY_ARGON_ITER, Argon2Kdf.ITERATIONS)
        set(value) = prefs.edit { putInt(KEY_ARGON_ITER, value) }

    var argonParallelism: Int
        get() = prefs.getInt(KEY_ARGON_PAR, Argon2Kdf.PARALLELISM)
        set(value) = prefs.edit { putInt(KEY_ARGON_PAR, value) }

    // Optional recovery slot: the same DEK wrapped a second, independent time with a
    // device-generated recovery code (Argon2 + AES-GCM). Lets a KEYSTORE_LOCK user
    // recover without data loss if the biometric Keystore key is invalidated. Nullable,
    // so existing installs are unaffected until a recovery code is provisioned.
    var recoveryWrappedDek: ByteArray?
        get() = getBytes(KEY_REC_WRAPPED_DEK)
        set(value) = setBytes(KEY_REC_WRAPPED_DEK, value)

    var recoveryIv: ByteArray?
        get() = getBytes(KEY_REC_IV)
        set(value) = setBytes(KEY_REC_IV, value)

    var recoverySalt: ByteArray?
        get() = getBytes(KEY_REC_SALT)
        set(value) = setBytes(KEY_REC_SALT, value)

    val hasRecovery: Boolean
        get() = recoveryWrappedDek != null && recoveryIv != null && recoverySalt != null

    val isInitialized: Boolean
        get() = mode != null && wrappedDek != null

    fun clear() = prefs.edit { this.clear() }

    private fun getBytes(key: String): ByteArray? =
        prefs.getString(key, null)?.let { Base64.decode(it, Base64.NO_WRAP) }

    private fun setBytes(key: String, value: ByteArray?) = prefs.edit {
        if (value == null) remove(key) else putString(key, Base64.encodeToString(value, Base64.NO_WRAP))
    }

    companion object {
        private const val PREFS_NAME = "knote_keys"
        private const val KEY_MODE = "mode"
        private const val KEY_WRAPPED_DEK = "wrapped_dek"
        private const val KEY_DEK_IV = "dek_iv"
        private const val KEY_SALT = "passphrase_salt"
        private const val KEY_ARGON_MEM = "argon_mem_kib"
        private const val KEY_ARGON_ITER = "argon_iterations"
        private const val KEY_ARGON_PAR = "argon_parallelism"
        private const val KEY_REC_WRAPPED_DEK = "recovery_wrapped_dek"
        private const val KEY_REC_IV = "recovery_dek_iv"
        private const val KEY_REC_SALT = "recovery_salt"
    }
}

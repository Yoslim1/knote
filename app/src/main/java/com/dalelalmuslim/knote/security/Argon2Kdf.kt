/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.security

import com.lambdapioneer.argon2kt.Argon2Kt
import com.lambdapioneer.argon2kt.Argon2Mode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Argon2Kdf {

    const val MEMORY_KIB = 65536
    const val ITERATIONS = 3
    const val PARALLELISM = 1
    const val HASH_LENGTH_BYTES = 32
    const val SALT_LENGTH_BYTES = 16

    private val argon2 by lazy { Argon2Kt() }

    suspend fun deriveKey(
        password: ByteArray,
        salt: ByteArray,
        memoryKiB: Int = MEMORY_KIB,
        iterations: Int = ITERATIONS,
        parallelism: Int = PARALLELISM,
        hashLengthBytes: Int = HASH_LENGTH_BYTES,
    ): ByteArray = withContext(Dispatchers.Default) {
        val result = argon2.hash(
            mode = Argon2Mode.ARGON2_ID,
            password = password,
            salt = salt,
            tCostInIterations = iterations,
            mCostInKibibyte = memoryKiB,
            parallelism = parallelism,
            hashLengthInBytes = hashLengthBytes,
        )
        result.rawHashAsByteArray()
    }
}

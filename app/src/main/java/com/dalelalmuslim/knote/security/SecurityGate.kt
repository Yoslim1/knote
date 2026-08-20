/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.security

import android.content.Context
import android.os.SystemClock
import com.dalelalmuslim.knote.data.DatabaseProvider
import com.dalelalmuslim.knote.data.PlaintextDbMigration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object SecurityGate {

    @Volatile
    lateinit var keyManager: KeyManager
        private set

    fun install(keyManager: KeyManager) {
        this.keyManager = keyManager
    }

    @Volatile var relockTimeoutSeconds: Int = 0

    @Volatile private var backgroundedAt: Long = 0L

    @Volatile var relocked: Boolean = false
        private set

    /** How long a detour into a system activity may take before it counts as leaving. */
    private const val SYSTEM_ACTIVITY_GRACE_MS = 60 * 1000L

    /**
     * True while a system activity we opened ourselves is in front: the
     * BiometricPrompt's device-credential confirmation, or one of the document
     * dialogs. Both run in a *separate* activity, which takes us through
     * onStop/onStart. That is not the user backgrounding the app, so we must not
     * treat it as a relock trigger — doing so relocks mid-authentication and
     * fights the unlock in progress, or interrupts a recovery code being saved.
     *
     * It only holds for [SYSTEM_ACTIVITY_GRACE_MS], so leaving the phone on an
     * open dialog does not keep the app unlocked indefinitely.
     */
    @Volatile var authInProgress: Boolean = false

    private fun gateReady(): Boolean = ::keyManager.isInitialized && keyManager.isInitialized()

    fun onAppBackgrounded() {
        if (!relocked) backgroundedAt = SystemClock.elapsedRealtime()
    }

    fun evaluateRelock() {
        if (!gateReady() || !keyManager.requiresUserPresence()) {
            relocked = false
            backgroundedAt = 0L
            return
        }
        if (backgroundedAt == 0L) return
        val elapsed = SystemClock.elapsedRealtime() - backgroundedAt
        // A detour into a system activity we opened ourselves is not the user
        // leaving — but only for as long as such a detour plausibly takes. Beyond
        // that the phone has been out of the flow long enough to lock, whatever
        // is still open on top of us.
        if (authInProgress && elapsed < SYSTEM_ACTIVITY_GRACE_MS) {
            backgroundedAt = 0L
            return
        }
        if (elapsed >= relockTimeoutSeconds * 1000L) relocked = true
    }

    fun clearRelock() {
        relocked = false
        backgroundedAt = 0L
    }

    enum class StartGate {
        UNLOCKED,

        NEEDS_BIOMETRIC,

        NEEDS_PASSPHRASE,
    }

    suspend fun prepare(context: Context): StartGate = withContext(Dispatchers.IO) {
        val appCtx = context.applicationContext
        if (DatabaseProvider.isOpen) {
            if (gateReady() && keyManager.requiresUserPresence() && relocked) {
                return@withContext when (keyManager.currentMode()) {
                    KeyMode.KEYSTORE_LOCK -> StartGate.NEEDS_BIOMETRIC
                    KeyMode.PASSPHRASE -> StartGate.NEEDS_PASSPHRASE
                    KeyMode.KEYSTORE_NO_LOCK -> StartGate.UNLOCKED
                }
            }
            return@withContext StartGate.UNLOCKED
        }

        if (!keyManager.isInitialized()) {
            val dek = keyManager.initialize(KeyMode.KEYSTORE_NO_LOCK)
            try {
                PlaintextDbMigration.migrate(appCtx, dek)
            } catch (t: Throwable) {
                keyManager.wipeKeys()
                dek.wipe()
                throw t
            }
            DatabaseProvider.open(appCtx, dek)
            return@withContext StartGate.UNLOCKED
        }

        if (!keyManager.requiresUserPresence()) {
            val dek = keyManager.unlockWithoutPrompt()
            DatabaseProvider.open(appCtx, dek)
            return@withContext StartGate.UNLOCKED
        }

        when (keyManager.currentMode()) {
            KeyMode.KEYSTORE_LOCK -> StartGate.NEEDS_BIOMETRIC
            KeyMode.PASSPHRASE -> StartGate.NEEDS_PASSPHRASE
            KeyMode.KEYSTORE_NO_LOCK -> StartGate.UNLOCKED
        }
    }

    fun unlockWithKeystore(context: Context, cryptoCipher: javax.crypto.Cipher) {
        val dek = keyManager.unlockWithKeystore(cryptoCipher)
        DatabaseProvider.open(context.applicationContext, dek)
    }

    suspend fun unlockWithPassphrase(context: Context, passphrase: CharArray) {
        val dek = keyManager.unlockWithPassphrase(passphrase)
        DatabaseProvider.open(context.applicationContext, dek)
    }

    /**
     * Recovers a locked database with the recovery code: unwrap the DEK and open the
     * database with the lock removed (no-lock). The user then chooses a new lock. Safe
     * if interrupted — the next launch simply opens without a lock. No data is lost.
     */
    suspend fun unlockWithRecoveryCode(context: Context, code: CharArray) {
        val dek = keyManager.unlockWithRecoveryCode(code)
        try {
            keyManager.rewrapDek(dek, KeyMode.KEYSTORE_NO_LOCK)
            DatabaseProvider.open(context.applicationContext, dek)
        } catch (t: Throwable) {
            dek.wipe()
            throw t
        }
    }

    suspend fun resetAndReinitialize(context: Context): Unit = withContext(Dispatchers.IO) {
        val appCtx = context.applicationContext
        DatabaseProvider.close()
        keyManager.wipeKeys()
        listOf("", "-wal", "-shm", "-journal").forEach {
            appCtx.getDatabasePath("knote.db$it").delete()
        }
        val dek = keyManager.initialize(KeyMode.KEYSTORE_NO_LOCK)
        DatabaseProvider.open(appCtx, dek)
    }
}

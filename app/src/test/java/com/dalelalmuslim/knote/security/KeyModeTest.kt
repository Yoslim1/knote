/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeyModeTest {

    @Test
    fun requiresUserPresence_perStage() {
        assertFalse(KeyMode.KEYSTORE_NO_LOCK.requiresUserPresence)
        assertTrue(KeyMode.KEYSTORE_LOCK.requiresUserPresence)
        assertTrue(KeyMode.PASSPHRASE.requiresUserPresence)
    }
}

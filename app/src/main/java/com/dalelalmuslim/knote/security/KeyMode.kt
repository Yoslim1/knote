/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.security

enum class KeyMode {
    KEYSTORE_NO_LOCK,

    KEYSTORE_LOCK,

    PASSPHRASE;

    val requiresUserPresence: Boolean
        get() = this == KEYSTORE_LOCK || this == PASSPHRASE
}

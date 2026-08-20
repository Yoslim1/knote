/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.security

class WrongPassphraseException(cause: Throwable? = null) :
    Exception("Falsche Passphrase oder beschaedigte Schluesseldaten", cause)

class WrongRecoveryCodeException(cause: Throwable? = null) :
    Exception("Falscher Wiederherstellungscode oder beschaedigte Schluesseldaten", cause)

class KeyInvalidatedException(cause: Throwable? = null) :
    Exception("Biometrie-Schluessel ungueltig geworden (neue Biometrie registriert?)", cause)

class WrongModeException(message: String) : IllegalStateException(message)

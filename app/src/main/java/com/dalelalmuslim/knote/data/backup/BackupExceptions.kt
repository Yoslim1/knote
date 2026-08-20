/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.data.backup

class WrongBackupPasswordException(cause: Throwable? = null) :
    Exception("Passwort falsch oder Datei beschädigt", cause)

class BackupCorruptException(message: String, cause: Throwable? = null) :
    Exception(message, cause)

class IncompatibleSchemaException(val foundVersion: Int, val supportedVersion: Int) :
    Exception("Backup-Schema $foundVersion ist neuer als unterstützt ($supportedVersion)")

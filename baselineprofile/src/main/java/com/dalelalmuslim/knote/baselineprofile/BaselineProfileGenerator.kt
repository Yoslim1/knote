/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Erzeugt das Baseline-Profil für com.dalelalmuslim.knote.
 *
 * Ausführen mit angeschlossenem Gerät/Emulator (API 36+):
 *   ./gradlew :app:generateReleaseBaselineProfile
 *
 * Das Ergebnis landet in app/src/release/generated/baselineProfiles/ und wird
 * beim Release-Build automatisch eingebettet (profileinstaller).
 *
 * Aktuell wird nur der Cold-Start-Pfad erfasst (Application-Init inkl. nativem
 * SQLCipher-Load, Compose-Setup, Lock-Gate, erstes Frame) – der mit Abstand
 * wertvollste Teil. Zum Erweitern auf Scroll-Journeys nach startActivityAndWait()
 * die jeweiligen Screens ansteuern (z. B. via device.findObject(...) + scroll).
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(
        packageName = "com.dalelalmuslim.knote",
        includeInStartupProfile = true,
    ) {
        pressHome()
        startActivityAndWait()
        device.waitForIdle()
    }
}

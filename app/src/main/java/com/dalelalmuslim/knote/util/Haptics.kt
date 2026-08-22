/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.VibratorManager

fun Context.performCheckHaptic() {
    // VibratorManager exists only from API 31; below that this would throw
    // NoClassDefFoundError on every task/habit completion, so skip silently.
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
    val vibrator = getSystemService(VibratorManager::class.java)?.defaultVibrator ?: return
    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
}

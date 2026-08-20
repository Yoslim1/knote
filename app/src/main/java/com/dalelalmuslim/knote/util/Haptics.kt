/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.util

import android.content.Context
import android.os.VibrationEffect
import android.os.VibratorManager

fun Context.performCheckHaptic() {
    val vibrator = getSystemService(VibratorManager::class.java)?.defaultVibrator ?: return
    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
}

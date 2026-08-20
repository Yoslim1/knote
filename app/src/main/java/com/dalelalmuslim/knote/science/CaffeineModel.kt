/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.science

import kotlin.math.ln
import kotlin.math.pow

enum class Metabolism(val halfLifeHours: Double) {
    FAST(3.5),
    NORMAL(5.0),
    SLOW(7.0),
}

object CaffeineModel {
    const val STANDARD_DOSE_MG = 95.0

    const val MIN_DOSE_MG = 32.0

    const val THRESHOLD_MG = 25.0

    const val DRAKE_FLOOR_H = 6.0

    data class Dose(val hour: Double, val amountMg: Double)

    fun remainingMgAt(doses: List<Dose>, t: Double, halfLife: Double): Double =
        doses.filter { it.hour <= t }
            .sumOf { it.amountMg * 0.5.pow((t - it.hour) / halfLife) }

    fun cutoffHour(bedHour: Double, doseMg: Double, thresholdMg: Double, halfLife: Double): Double =
        bedHour - halfLife * (ln(doseMg / thresholdMg) / ln(2.0))
}

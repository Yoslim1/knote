/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui

import androidx.compose.ui.graphics.Color

/**
 * The three states of the task light, shared so notes and tasks cannot drift
 * apart. Red means untouched, yellow started, green done.
 */
val StatusRed    = Color(0xFFE53935)
val StatusYellow = Color(0xFFFFB300)
val StatusGreen  = Color(0xFF43A047)

/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val onSurface: Color,
    val onSurfaceSecondary: Color,
    val onSurfaceTertiary: Color,
    val accent: Color,
    val accentContainer: Color,
    val divider: Color,
    val topBar: Color,
    val bottomBar: Color,
)

private val LightAccent = Color(0xFF5B5BD6)
private val DarkAccent = Color(0xFFA8A7FF)

val LightAppColors = AppColors(
    background          = Color(0xFFF7F7FA),
    surface             = Color(0xFFFFFFFF),
    surfaceVariant      = Color(0xFFF0F1F7),
    onSurface           = Color(0xFF1B1C24),
    onSurfaceSecondary  = Color(0xFF616372),
    onSurfaceTertiary   = Color(0xFF9294A0),
    accent              = LightAccent,
    accentContainer     = Color(0xFFE9E9FF),
    divider             = Color(0xFFE3E4EC),
    topBar              = Color(0xFFFFFFFF),
    bottomBar           = Color(0xFFFFFFFF),
)

val DarkAppColors = AppColors(
    background          = Color(0xFF11121A),
    surface             = Color(0xFF1B1D28),
    surfaceVariant      = Color(0xFF242634),
    onSurface           = Color(0xFFF4F4FA),
    onSurfaceSecondary  = Color(0xFFB6B7C6),
    onSurfaceTertiary   = Color(0xFF7B7D8C),
    accent              = DarkAccent,
    accentContainer     = Color(0xFF2D2D58),
    divider             = Color(0xFF343646),
    topBar              = Color(0xFF1B1D28),
    bottomBar           = Color(0xFF1B1D28),
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

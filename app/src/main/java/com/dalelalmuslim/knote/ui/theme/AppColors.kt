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

val LightAppColors = AppColors(
    background          = Color(0xFFF5F5F5),
    surface             = Color.White,
    surfaceVariant      = Color(0xFFF8F9FA),
    onSurface           = Color(0xFF1A1A1A),
    onSurfaceSecondary  = Color(0xFF666666),
    onSurfaceTertiary   = Color(0xFFBBBBBB),
    accent              = Color(0xFF3D5AFE),
    accentContainer     = Color(0xFFE8EAFE),
    divider             = Color(0xFFEEEEEE),
    topBar              = Color.White,
    bottomBar           = Color.White,
)

val DarkAppColors = AppColors(
    background          = Color(0xFF0E0E0E),
    surface             = Color(0xFF1C1C1C),
    surfaceVariant      = Color(0xFF161616),
    onSurface           = Color(0xFFE8E8E8),
    onSurfaceSecondary  = Color(0xFF9E9E9E),
    onSurfaceTertiary   = Color(0xFF4A4A4A),
    accent              = Color(0xFF3D5AFE),
    accentContainer     = Color(0xFF151C3D),
    divider             = Color(0xFF2A2A2A),
    topBar              = Color(0xFF1C1C1C),
    bottomBar           = Color(0xFF1C1C1C),
)

val LocalAppColors = staticCompositionLocalOf { LightAppColors }

/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.theme

import android.app.Activity
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat
import com.dalelalmuslim.knote.ui.components.AppClickSoundIndication

private val LightPrimary = Color(0xFF5B5BD6)
private val LightOnPrimary = Color.White
private val LightPrimaryContainer = Color(0xFFE9E9FF)
private val LightOnPrimaryContainer = Color(0xFF19194B)
private val LightSecondary = Color(0xFF596078)
private val LightOnSecondary = Color.White
private val LightBackground = Color(0xFFF7F7FA)
private val LightOnBackground = Color(0xFF1B1C24)
private val LightSurface = Color(0xFFFFFFFF)
private val LightOnSurface = Color(0xFF1B1C24)
private val LightSurfaceVariant = Color(0xFFF0F1F7)
private val LightOnSurfaceVariant = Color(0xFF616372)
private val LightOutline = Color(0xFF777985)

private val DarkPrimary = Color(0xFFA8A7FF)
private val DarkOnPrimary = Color(0xFF29285D)
private val DarkPrimaryContainer = Color(0xFF42427C)
private val DarkOnPrimaryContainer = Color(0xFFE9E9FF)
private val DarkSecondary = Color(0xFFBEC5E2)
private val DarkOnSecondary = Color(0xFF282E44)
private val DarkBackground = Color(0xFF11121A)
private val DarkOnBackground = Color(0xFFF4F4FA)
private val DarkSurface = Color(0xFF1B1D28)
private val DarkOnSurface = Color(0xFFF4F4FA)
private val DarkSurfaceVariant = Color(0xFF242634)
private val DarkOnSurfaceVariant = Color(0xFFB6B7C6)
private val DarkOutline = Color(0xFF8D8F9F)

@Composable
fun KnoteTheme(
    themeMode: String = "SYSTEM",
    fontScale: Float = 1.0f,
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemDark
    }

    val appColors = if (useDark) DarkAppColors else LightAppColors
    val materialColors = if (useDark) {
        darkColorScheme(
            primary = DarkPrimary,
            onPrimary = DarkOnPrimary,
            primaryContainer = DarkPrimaryContainer,
            onPrimaryContainer = DarkOnPrimaryContainer,
            secondary = DarkSecondary,
            onSecondary = DarkOnSecondary,
            background = DarkBackground,
            onBackground = DarkOnBackground,
            surface = DarkSurface,
            onSurface = DarkOnSurface,
            surfaceVariant = DarkSurfaceVariant,
            onSurfaceVariant = DarkOnSurfaceVariant,
            outline = DarkOutline,
        )
    } else {
        lightColorScheme(
            primary = LightPrimary,
            onPrimary = LightOnPrimary,
            primaryContainer = LightPrimaryContainer,
            onPrimaryContainer = LightOnPrimaryContainer,
            secondary = LightSecondary,
            onSecondary = LightOnSecondary,
            background = LightBackground,
            onBackground = LightOnBackground,
            surface = LightSurface,
            onSurface = LightOnSurface,
            surfaceVariant = LightSurfaceVariant,
            onSurfaceVariant = LightOnSurfaceVariant,
            outline = LightOutline,
        )
    }

    val baseDensity = LocalDensity.current
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !useDark
            controller.isAppearanceLightNavigationBars = !useDark
        }
    }

    CompositionLocalProvider(
        LocalAppColors provides appColors,
        LocalDensity provides Density(baseDensity.density, fontScale.coerceIn(0.85f, 1.30f)),
    ) {
        MaterialTheme(
            colorScheme = materialColors,
            typography = Typography,
        ) {
            CompositionLocalProvider(LocalIndication provides AppClickSoundIndication) {
                content()
            }
        }
    }
}

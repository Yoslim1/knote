/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.theme

import android.app.Activity
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import com.dalelalmuslim.knote.ui.components.AppClickSoundIndication
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Density
import androidx.core.view.WindowCompat

@Composable
fun KnoteTheme(
    themeMode: String = "SYSTEM",
    fontScale: Float  = 1.0f,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        "DARK"  -> true
        "LIGHT" -> false
        else    -> systemDark
    }

    val appBlue = Color(0xFF3D5AFE)
    val appColors   = if (useDark) DarkAppColors else LightAppColors
    val m3Scheme    = if (useDark)
        darkColorScheme(primary = appBlue, onPrimary = Color.White, secondary = appBlue, onSecondary = Color.White)
    else
        lightColorScheme(primary = appBlue, onPrimary = Color.White, secondary = appBlue, onSecondary = Color.White)
    val baseDensity = LocalDensity.current
    val view        = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDark
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !useDark
        }
    }

    CompositionLocalProvider(
        LocalAppColors provides appColors,
        LocalDensity provides Density(baseDensity.density, fontScale)
    ) {
        MaterialTheme(
            colorScheme = m3Scheme,
            typography  = Typography
        ) {
            // Must be INSIDE MaterialTheme: it re-provides LocalIndication (ripple),
            // so providing ours outside would be overridden.
            CompositionLocalProvider(LocalIndication provides AppClickSoundIndication) {
                content()
            }
        }
    }
}

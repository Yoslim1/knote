/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.locale

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import java.util.Locale

/**
 * Stores only the display-language choice outside the encrypted database so the
 * activity can localize the lock screen before SQLCipher is opened. No personal
 * or app data is written here.
 */
object AppLocalePreferences {
    private const val PREFS_NAME = "knote_display_preferences"
    private const val LANGUAGE_KEY = "language"
    const val AUTO = "AUTO"
    const val ENGLISH = "en"
    const val ARABIC = "ar"

    fun normalize(setting: String?): String = when (setting?.lowercase(Locale.ROOT)) {
        null, "", "auto" -> AUTO
        ENGLISH -> ENGLISH
        ARABIC -> ARABIC
        else -> AUTO
    }

    fun read(context: Context): String = normalize(
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(LANGUAGE_KEY, AUTO)
    )

    fun write(context: Context, setting: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(LANGUAGE_KEY, normalize(setting))
            .apply()
    }

    fun localeForSetting(setting: String?, systemLocale: Locale = Locale.getDefault()): Locale = when (normalize(setting)) {
        ENGLISH -> Locale.ENGLISH
        ARABIC -> Locale("ar")
        else -> systemLocale
    }

    fun localeListForSetting(setting: String?): LocaleList = when (normalize(setting)) {
        ENGLISH -> LocaleList.forLanguageTags(ENGLISH)
        ARABIC -> LocaleList.forLanguageTags(ARABIC)
        else -> LocaleList.getEmptyLocaleList()
    }

    fun localizedContext(context: Context, setting: String?): Context {
        val normalized = normalize(setting)
        if (normalized == AUTO) return context
        val locale = localeForSetting(normalized)
        val configuration = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.setLocales(LocaleList(locale))
        } else {
            @Suppress("DEPRECATION")
            configuration.setLocale(locale)
        }
        return context.createConfigurationContext(configuration)
    }

    fun wrap(context: Context): Context = localizedContext(context, read(context))
}

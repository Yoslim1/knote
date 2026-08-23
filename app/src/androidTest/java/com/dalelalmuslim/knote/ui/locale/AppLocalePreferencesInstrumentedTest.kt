/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.locale

import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppLocalePreferencesInstrumentedTest {
    private val context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun normalizes_only_supported_display_languages() {
        assertEquals(AppLocalePreferences.AUTO, AppLocalePreferences.normalize(null))
        assertEquals(AppLocalePreferences.AUTO, AppLocalePreferences.normalize("AUTO"))
        assertEquals(AppLocalePreferences.ENGLISH, AppLocalePreferences.normalize("EN"))
        assertEquals(AppLocalePreferences.ARABIC, AppLocalePreferences.normalize("ar"))
        assertEquals(AppLocalePreferences.AUTO, AppLocalePreferences.normalize("de"))
    }

    @Test
    fun writes_and_reads_language_mirror() {
        val previous = AppLocalePreferences.read(context)
        try {
            AppLocalePreferences.write(context, AppLocalePreferences.ARABIC)
            assertEquals(AppLocalePreferences.ARABIC, AppLocalePreferences.read(context))
            AppLocalePreferences.write(context, AppLocalePreferences.ENGLISH)
            assertEquals(AppLocalePreferences.ENGLISH, AppLocalePreferences.read(context))
        } finally {
            AppLocalePreferences.write(context, previous)
        }
    }

    @Test
    fun localized_arabic_context_has_rtl_configuration() {
        val localized = AppLocalePreferences.localizedContext(context, AppLocalePreferences.ARABIC)
        assertEquals("ar", localized.resources.configuration.locales[0].language)
        assertEquals(View.LAYOUT_DIRECTION_RTL, localized.resources.configuration.layoutDirection)
    }

    @Test
    fun locale_lists_are_empty_for_auto_and_explicit_for_supported_languages() {
        assertTrue(AppLocalePreferences.localeListForSetting(AppLocalePreferences.AUTO).isEmpty)
        assertEquals("en", AppLocalePreferences.localeListForSetting("EN")[0].language)
        assertEquals("ar", AppLocalePreferences.localeListForSetting("AR")[0].language)
    }
}

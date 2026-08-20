/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.strings

import android.content.Context
import com.dalelalmuslim.knote.R

internal class HolidayStrings(
    val menu: String,
    val menuSubtitle: String,
    val section: String,
    val showLabel: String,
    val showHint: String,
    val countryLabel: String,
    val regionLabel: String,
    val exportLabel: String,
    val exportHint: String,
    val pdfSectionTitle: String,
)

internal fun holidayStrings(ctx: Context): HolidayStrings = HolidayStrings(
    menu = ctx.getString(R.string.holiday_settings_menu),
    menuSubtitle = ctx.getString(R.string.holiday_settings_menu_subtitle),
    section = ctx.getString(R.string.holiday_settings_section),
    showLabel = ctx.getString(R.string.holiday_settings_show_label),
    showHint = ctx.getString(R.string.holiday_settings_show_hint),
    countryLabel = ctx.getString(R.string.holiday_settings_country_label),
    regionLabel = ctx.getString(R.string.holiday_settings_region_label),
    exportLabel = ctx.getString(R.string.holiday_settings_export_label),
    exportHint = ctx.getString(R.string.holiday_settings_export_hint),
    pdfSectionTitle = ctx.getString(R.string.holiday_pdf_section),
)

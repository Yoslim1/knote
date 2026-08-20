/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.holidays

import android.util.LruCache
import java.time.LocalDate

interface HolidayProvider {
    fun holidays(region: HolidayRegion, years: IntRange): List<Holiday>

    fun isHoliday(region: HolidayRegion, date: LocalDate): Boolean

    fun holidayOn(region: HolidayRegion, date: LocalDate): Holiday?
}

class DefaultHolidayProvider(cacheYears: Int = 64) : HolidayProvider {

    private val cache = LruCache<String, List<Holiday>>(cacheYears)

    private fun forYear(region: HolidayRegion, year: Int): List<Holiday> {
        val cacheKey = "${region.key}#$year"
        cache.get(cacheKey)?.let { return it }
        val computed = region.rules
            .mapNotNull { rule -> rule.dateIn(year)?.let { Holiday(it, rule.nameKey) } }
            .distinctBy { it.date to it.nameKey }
            .sortedBy { it.date }
        cache.put(cacheKey, computed)
        return computed
    }

    override fun holidays(region: HolidayRegion, years: IntRange): List<Holiday> =
        years.flatMap { forYear(region, it) }.sortedBy { it.date }

    override fun isHoliday(region: HolidayRegion, date: LocalDate): Boolean =
        forYear(region, date.year).any { it.date == date }

    override fun holidayOn(region: HolidayRegion, date: LocalDate): Holiday? =
        forYear(region, date.year).firstOrNull { it.date == date }
}

/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.holidays

import android.content.Context
import android.telephony.TelephonyManager
import java.util.Locale

object HolidayDefaults {

    fun resolveCountry(context: Context, chosenCountry: String): SupportedCountry {
        HolidayCatalog.byIso(chosenCountry)?.let { return it }
        HolidayCatalog.byIso(detectCountryIso(context))?.let { return it }
        return HolidayCatalog.countries.first()
    }

    fun detectCountryIso(context: Context): String? {
        val locale = Locale.getDefault()
        locale.country.takeIf { it.isNotBlank() }?.uppercase(Locale.ROOT)?.let { iso ->
            if (HolidayCatalog.byIso(iso) != null) return iso
        }
        countryForLanguage(locale.language)?.let { return it }
        val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val sim = tm?.simCountryIso?.takeIf { it.isNotBlank() }
        val network = tm?.networkCountryIso?.takeIf { it.isNotBlank() }
        return (sim ?: network)?.uppercase(Locale.ROOT)
    }

    private fun countryForLanguage(language: String): String? = when (language.lowercase(Locale.ROOT)) {
        "de" -> "DE"
        "fr" -> "FR"
        "it" -> "IT"
        "es" -> "ES"
        "nl" -> "NL"
        "pl" -> "PL"
        "pt" -> "PT"
        "en" -> "US"
        else -> null
    }

    fun resolveRegion(context: Context, chosenCountry: String, chosenRegion: String): HolidayRegion {
        val country = resolveCountry(context, chosenCountry)
        val subdivision = chosenRegion.takeIf { it.isNotBlank() }
        return country.regionFor(subdivision)
    }
}

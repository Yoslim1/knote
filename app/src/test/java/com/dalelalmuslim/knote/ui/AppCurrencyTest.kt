/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class AppCurrencyTest {

    @Test fun `EGP steht in der Liste`() {
        val egp = ALL_CURRENCIES.find { it.code == "EGP" }
        assertNotNull(egp)
        assertEquals("1234.56 E£", egp!!.format(1234.56))
    }

    @Test fun `Codes sind eindeutig`() {
        assertEquals(ALL_CURRENCIES.size, ALL_CURRENCIES.map { it.code }.toSet().size)
    }

    @Test fun `Formatierung ignoriert die Geraetesprache`() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.GERMANY)
            assertEquals("$1234.56", currencyByCode("USD").format(1234.56))
            assertEquals("1234,56 €", currencyByCode("EUR").format(1234.56))
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test fun `eigene Waehrung ueberlebt Kodieren und Dekodieren`() {
        val custom = customCurrency("₪", symbolBefore = false, symbolSpace = true, decimalDigits = 2, decimalSeparator = ',')
        val encoded = custom.encode()
        assertTrue(isCustomCurrency(encoded))
        assertEquals(custom, currencyByCode(encoded))
        assertEquals("1234,56 ₪", currencyByCode(encoded).format(1234.56))
    }

    @Test fun `eigene Waehrung ohne Nachkommastellen rundet`() {
        val custom = customCurrency("Kč", symbolBefore = true, symbolSpace = false, decimalDigits = 0, decimalSeparator = '.')
        assertEquals("Kč1235", currencyByCode(custom.encode()).format(1234.56))
    }

    @Test fun `Symbol wird bereinigt und begrenzt`() {
        val custom = customCurrency(" a|b c defgh ", symbolBefore = true, symbolSpace = false, decimalDigits = 2, decimalSeparator = '.')
        assertEquals("abcdef", custom.symbol)
        assertEquals(custom, currencyByCode(custom.encode()))
    }

    @Test fun `unbrauchbare Werte fallen auf die Standardwaehrung zurueck`() {
        assertEquals(DEFAULT_CURRENCY, currencyByCode("CUSTOM:"))
        assertEquals(DEFAULT_CURRENCY, currencyByCode("CUSTOM:x|B|N"))
        assertEquals(DEFAULT_CURRENCY, currencyByCode("XYZ"))
    }

    @Test fun `eingebauter Code bleibt unveraendert kodiert`() {
        assertEquals("USD", currencyByCode("USD").encode())
    }
}

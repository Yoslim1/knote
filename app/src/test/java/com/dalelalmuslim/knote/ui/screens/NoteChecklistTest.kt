/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.screens

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import com.dalelalmuslim.knote.ui.StatusGreen
import com.dalelalmuslim.knote.ui.StatusRed
import com.dalelalmuslim.knote.ui.StatusYellow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NoteChecklistTest {

    @Test fun `jede Schreibweise wird erkannt`() {
        assertEquals(CheckState.OPEN,  checkStateOf("- [ ] offen"))
        assertEquals(CheckState.DOING, checkStateOf("- [/] laeuft"))
        assertEquals(CheckState.DONE,  checkStateOf("- [x] fertig"))
        assertEquals(CheckState.DONE,  checkStateOf("- [X] fertig"))
    }

    @Test fun `andere Zeilen sind keine Checkliste`() {
        assertNull(checkStateOf("- Strich"))
        assertNull(checkStateOf("* Punkt"))
        assertNull(checkStateOf("1. Nummer"))
        assertNull(checkStateOf("# Titel"))
        assertNull(checkStateOf(""))
        assertNull(checkStateOf("- [] ohne Leerzeichen"))
    }

    @Test fun `ein Klick geht einen Zustand weiter und dann rundherum`() {
        assertEquals("- [/] A", cycleCheckLine("- [ ] A"))
        assertEquals("- [x] A", cycleCheckLine("- [/] A"))
        assertEquals("- [ ] A", cycleCheckLine("- [x] A"))
        assertEquals("- [ ] A", cycleCheckLine("- [X] A"))
    }

    @Test fun `der Text der Zeile bleibt beim Wechsel erhalten`() {
        assertEquals("- [/] Milch **kaufen**", cycleCheckLine("- [ ] Milch **kaufen**"))
        assertEquals("- [/] ", cycleCheckLine("- [ ] "))
    }

    @Test fun `eine Zeile ohne Kaestchen bleibt unveraendert`() {
        assertEquals("nur Text", cycleCheckLine("nur Text"))
        assertEquals("- Strich", cycleCheckLine("- Strich"))
    }

    @Test fun `die Zustaende einer Notiz kommen der Reihe nach`() {
        val raw = "# Titel\n- [x] a\nText\n- [ ] b\n- [/] c"
        assertEquals(
            listOf(CheckState.DONE, CheckState.OPEN, CheckState.DOING),
            checkStatesIn(raw)
        )
        assertEquals(emptyList<CheckState>(), checkStatesIn("nur Text\n- Strich"))
    }

    @Test fun `leere Kaestchen werden beim Speichern entfernt`() {
        assertEquals("# Titel\n- [ ] Milch", dropEmptyCheckItems("# Titel\n- [ ] Milch\n- [ ] "))
        assertEquals("- [x] a\n- [/] b", dropEmptyCheckItems("- [x] a\n- [ ]   \n- [/] b"))
        assertEquals("", dropEmptyCheckItems("- [ ] "))
    }

    @Test fun `andere leere Zeilen bleiben erhalten`() {
        assertEquals("Text\n\nmehr", dropEmptyCheckItems("Text\n\nmehr"))
        assertEquals("- \n* \n1. ", dropEmptyCheckItems("- \n* \n1. "))
    }

    @Test fun `der Punkt zeigt den am wenigsten erledigten Zustand`() {
        assertEquals(CheckState.OPEN,  openCheckState("- [ ] a\n- [/] b\n- [x] c"))
        assertEquals(CheckState.OPEN,  openCheckState("- [x] a\n- [ ] b"))
        assertEquals(CheckState.DOING, openCheckState("- [/] a\n- [x] b"))
    }

    @Test fun `ohne offene Punkte gibt es keinen Punkt`() {
        assertNull(openCheckState("- [x] a\n- [x] b"))
        assertNull(openCheckState("nur Text\n- Strich"))
        assertNull(openCheckState(""))
    }

    // The editor withholds this while the line is being written in; the function
    // itself only says whether the edit is one that qualifies.
    @Test fun `am Ende des Eintrags faellt der Text, das Kaestchen bleibt`() {
        val old = TextFieldValue("- [x] Fluege\n- [ ] Ryokan", TextRange(25))
        val new = TextFieldValue("- [x] Fluege\n- [ ] Ryoka", TextRange(24))
        val out = deleteCheckItemText(old, new)
        assertEquals("- [x] Fluege\n- [ ] ", out?.text)
        assertEquals(TextRange(19), out?.selection)
    }

    @Test fun `der naechste Druck nimmt dann das leere Kaestchen`() {
        // What the first press leaves behind is the empty box, and that is the
        // case deleteCheckMarker already handles.
        val old = TextFieldValue("a\n- [ ] ", TextRange(8))
        val new = TextFieldValue("a\n- [ ]", TextRange(7))
        assertNull(deleteCheckItemText(old, new))
        val out = deleteCheckMarker(old, new)
        assertEquals("a", out?.text)
        assertEquals(TextRange(1), out?.selection)
    }

    @Test fun `mitten im Eintrag bleibt das Loeschen buchstabenweise`() {
        val old = TextFieldValue("- [ ] Ryokan in Gion", TextRange(12))
        val new = TextFieldValue("- [ ] Ryoka in Gion", TextRange(11))
        assertNull(deleteCheckItemText(old, new))
    }

    @Test fun `in einer gewoehnlichen Zeile raeumt nichts auf`() {
        val old = TextFieldValue("Ryokan in Gion", TextRange(14))
        val new = TextFieldValue("Ryokan in Gio", TextRange(13))
        assertNull(deleteCheckItemText(old, new))
    }

    @Test fun `ein Rueckschritt loescht den leeren Eintrag`() {
        // The cursor sits behind the box; one backspace removes "- [ ] " whole.
        val old = TextFieldValue("- [ ] ", TextRange(6))
        val new = TextFieldValue("- [ ]", TextRange(5))
        val out = deleteCheckMarker(old, new)
        assertEquals("", out?.text)
        assertEquals(TextRange(0), out?.selection)
    }

    @Test fun `der Eintrag verschwindet samt Text und Zeilenumbruch`() {
        val old = TextFieldValue("a\n- [ ] Milch", TextRange(8))
        val new = TextFieldValue("a\n- [ ]Milch", TextRange(7))
        val out = deleteCheckMarker(old, new)
        assertEquals("a", out?.text)
        assertEquals(TextRange(1), out?.selection)
    }

    @Test fun `der erste Eintrag zieht die Zeile darunter hoch`() {
        val old = TextFieldValue("- [ ] Milch\nBrot", TextRange(6))
        val new = TextFieldValue("- [ ]Milch\nBrot", TextRange(5))
        val out = deleteCheckMarker(old, new)
        assertEquals("Brot", out?.text)
        assertEquals(TextRange(0), out?.selection)
    }

    @Test fun `vor dem Kaestchen ist es kein Fall fuer die Kaestchen-Regel`() {
        val old = TextFieldValue("a\n- [ ] Milch", TextRange(2))
        val new = TextFieldValue("a- [ ] Milch", TextRange(1))
        assertNull(deleteCheckMarker(old, new))
    }

    @Test fun `beim Verbinden nach oben bleibt kein Markdown zurueck`() {
        val old = TextFieldValue("a\n- [ ] Milch", TextRange(2))
        val new = TextFieldValue("a- [ ] Milch", TextRange(1))
        val out = joinCheckItemUp(old, new)
        assertEquals("aMilch", out?.text)
        assertEquals(TextRange(1), out?.selection)
    }

    @Test fun `mitten im Eintrag wird nicht verbunden`() {
        val old = TextFieldValue("a\n- [ ] Milch", TextRange(10))
        val new = TextFieldValue("a\n- [ ] Mich", TextRange(9))
        assertNull(joinCheckItemUp(old, new))
    }

    @Test fun `vorwaerts loeschen bleibt vorwaerts loeschen`() {
        // Forward delete leaves the caret where it was — not our case.
        val old = TextFieldValue("- [ ] Milch", TextRange(6))
        val new = TextFieldValue("- [ ] ilch", TextRange(6))
        assertNull(deleteCheckMarker(old, new))
    }

    @Test fun `gewoehnliches Loeschen bleibt gewoehnlich`() {
        val old = TextFieldValue("- [ ] Milch", TextRange(11))
        val new = TextFieldValue("- [ ] Milc", TextRange(10))
        assertNull(deleteCheckMarker(old, new))

        val text = TextFieldValue("kein Kaestchen", TextRange(5))
        assertNull(deleteCheckMarker(text, TextFieldValue("keinKaestchen", TextRange(4))))
    }

    @Test fun `die Farben sind die der Aufgaben-Ampel`() {
        assertEquals(StatusRed,    CheckState.OPEN.color)
        assertEquals(StatusYellow, CheckState.DOING.color)
        assertEquals(StatusGreen,  CheckState.DONE.color)
    }

    // --- Der Cursor gehört nie vor das Kästchen ---

    @Test fun `vor dem Kaestchen rueckt der Cursor hinter den Marker`() {
        val text = "- [ ] Milch"
        assertEquals(6, clampOutOfCheckMarker(text, 0))
        assertEquals(6, clampOutOfCheckMarker(text, 3))
    }

    @Test fun `im Editor bleibt der Zeilenanfang erreichbar`() {
        val text = "Einkauf\n- [ ] Milch"
        assertEquals(8, clampOutOfCheckMarker(text, 8, allowLineStart = true))
        // Inside the markup the caret is still pushed out.
        assertEquals(14, clampOutOfCheckMarker(text, 10, allowLineStart = true))
    }

    @Test fun `im Text bleibt der Cursor stehen`() {
        val text = "- [ ] Milch"
        assertEquals(8, clampOutOfCheckMarker(text, 8))
    }

    @Test fun `in einer gewoehnlichen Zeile aendert sich nichts`() {
        val text = "Einkauf\n- [ ] Milch"
        assertEquals(2, clampOutOfCheckMarker(text, 2))
    }

    @Test fun `auch in der zweiten Zeile gilt der Marker`() {
        val text = "Einkauf\n- [x] Milch"
        assertEquals(14, clampOutOfCheckMarker(text, 8))
        assertEquals(16, clampOutOfCheckMarker(text, 16))
    }
}

/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.screens

import com.dalelalmuslim.knote.data.Note
import com.dalelalmuslim.knote.data.NoteType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NoteClassificationTest {

    private fun note(content: String, type: NoteType = NoteType.NOTE) =
        Note(id = 1, title = "# Titel", content = content, type = type)

    @Test fun `das Symbol richtet sich nach dem Inhalt`() {
        // Mostly list -> list, mostly prose -> note, whatever it was created as.
        assertEquals(NoteType.LIST, displayedNoteType(note("- Milch\n- Brot")))
        assertEquals(NoteType.LIST, displayedNoteType(note("- [ ] a\n- [x] b\nkurze Notiz")))
        assertEquals(NoteType.NOTE, displayedNoteType(note("Ein Satz.\nNoch einer.\n- Milch")))
        assertEquals(NoteType.NOTE, displayedNoteType(note("nur Text")))
        assertEquals(NoteType.NOTE, displayedNoteType(note("")))
    }

    @Test fun `Ueberschriften entscheiden nicht mit`() {
        assertEquals(NoteType.LIST, displayedNoteType(note("## Montag\n- Milch")))
    }

    @Test fun `jede Notiz landet in genau einem Tab`() {
        val liste = note("- Milch\n- Brot")
        assertTrue(matchesTypeFilter(liste, NoteType.LIST))
        assertFalse(matchesTypeFilter(liste, NoteType.NOTE))

        val text = note("Ein Satz.\nNoch einer.")
        assertTrue(matchesTypeFilter(text, NoteType.NOTE))
        assertFalse(matchesTypeFilter(text, NoteType.LIST))
    }

    @Test fun `der gespeicherte Typ entscheidet nicht mehr mit`() {
        // Created as a note, written as a list: it belongs with the lists.
        assertTrue(matchesTypeFilter(note("- Milch", NoteType.NOTE), NoteType.LIST))
        // Created as a list, written as prose: it belongs with the notes.
        assertTrue(matchesTypeFilter(note("Ein langer Satz.", NoteType.LIST), NoteType.NOTE))
        // Old routines follow the same rule.
        assertTrue(matchesTypeFilter(note("nur Text", NoteType.ROUTINE), NoteType.NOTE))
    }

    @Test fun `ohne Filter erscheint alles`() {
        assertTrue(matchesTypeFilter(note("egal"), null))
    }

    @Test fun `der Titel allein ist noch kein Text`() {
        assertFalse(hasProse(Note(id = 1, title = "Titel ohne Raute", content = "- Milch")))
        assertFalse(hasProse(note("## Zwischentitel\n- Milch")))
        assertTrue(hasProse(note("## Zwischentitel\n- Milch\nund noch ein Satz")))
    }
}

/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.screens

import com.dalelalmuslim.knote.data.Note
import com.dalelalmuslim.knote.data.NoteType

private fun isHeading(line: String) =
    line.startsWith("# ") || line.startsWith("## ") || line.startsWith("### ")

private fun isListLine(line: String) =
    checkStateOf(line) != null ||
        line.startsWith("- ") || line.startsWith("* ") ||
        numberedPrefixLength(line) > 0

/** Title and content as one text, the way both are written in the editor. */
private fun Note.lines(): List<String> =
    if (content.isEmpty()) listOf(title) else "$title\n$content".lines()

/** Whether the note writes out a list of any kind. */
internal fun containsList(note: Note): Boolean = note.lines().any { isListLine(it) }

/**
 * Whether anything is written beyond headings and list items. The first line is
 * skipped because it is the title, and a title plus a subheading is not yet
 * what makes a note a note.
 */
internal fun hasProse(note: Note): Boolean =
    note.lines().drop(1).any { it.isNotBlank() && !isHeading(it) && !isListLine(it) }

/**
 * The kind a note reads as, judged by what is written in it rather than by the
 * type it was created under — that type was often just the tab that happened to
 * be open.
 */
internal fun displayedNoteType(note: Note): NoteType {
    val body = note.lines().drop(1).filter { it.isNotBlank() && !isHeading(it) }
    val lists = body.count { isListLine(it) }
    return if (lists > 0 && lists >= body.size - lists) NoteType.LIST else NoteType.NOTE
}

/**
 * Which tab a note shows up under: the same judgement its icon shows, so the
 * two can never contradict each other. Every note lands in exactly one tab.
 */
internal fun matchesTypeFilter(note: Note, filter: NoteType?): Boolean =
    filter == null || displayedNoteType(note) == filter

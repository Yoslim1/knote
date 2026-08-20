/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.export

object ExportFilenameSanitizer {

    private const val MAX_LENGTH = 100

    private val FORBIDDEN = charArrayOf('/', '\\', ':', '*', '?', '"', '<', '>', '|')

    fun sanitize(title: String, fallbackPrefix: String, fallbackIndex: Int): String {
        val cleaned = buildString {
            for (ch in title) {
                when {
                    ch.isISOControl() -> append(' ')
                    ch in FORBIDDEN   -> append(' ')
                    else              -> append(ch)
                }
            }
        }
            .replace(Regex("\\s+"), " ")
            .trim()
            .trim('.')
            .trim()

        val base = cleaned.ifBlank { "$fallbackPrefix$fallbackIndex" }
        return if (base.length > MAX_LENGTH) base.substring(0, MAX_LENGTH).trim() else base
    }

    fun uniqueFileName(baseName: String, ext: String, taken: MutableSet<String>): String {
        var candidate = "$baseName.$ext"
        var counter = 2
        while (!taken.add(candidate.lowercase())) {
            candidate = "$baseName ($counter).$ext"
            counter++
        }
        return candidate
    }
}

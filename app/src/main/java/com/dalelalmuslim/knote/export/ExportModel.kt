/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.export

import java.io.OutputStream

enum class ExportCategory { KALENDER, JOURNAL, NOTIZEN, FINANZBERICHT }

enum class ExportFormat { PDF, ZIP_TXT, ZIP_PDF }

data class ExportOptions(
    val includeMood: Boolean = false
)

interface Exporter {
    val category: ExportCategory

    fun supportedFormats(): List<ExportFormat>

    fun mimeType(format: ExportFormat): String

    fun defaultBaseName(): String

    suspend fun hasData(): Boolean

    suspend fun write(
        out: OutputStream,
        format: ExportFormat,
        options: ExportOptions,
        onProgress: (current: Int, total: Int) -> Unit
    )
}

fun ExportFormat.extension(): String = when (this) {
    ExportFormat.PDF -> "pdf"
    ExportFormat.ZIP_TXT, ExportFormat.ZIP_PDF -> "zip"
}

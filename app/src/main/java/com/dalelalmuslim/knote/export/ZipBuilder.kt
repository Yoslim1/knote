/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.export

import java.io.OutputStream
import java.nio.charset.StandardCharsets
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ZipBuilder(out: OutputStream) : AutoCloseable {

    private val zip = ZipOutputStream(out, StandardCharsets.UTF_8)

    fun addEntry(name: String, bytes: ByteArray) {
        zip.putNextEntry(ZipEntry(name))
        zip.write(bytes)
        zip.closeEntry()
    }

    fun addTextEntry(name: String, text: String) =
        addEntry(name, text.toByteArray(StandardCharsets.UTF_8))

    override fun close() {
        zip.close()
    }
}

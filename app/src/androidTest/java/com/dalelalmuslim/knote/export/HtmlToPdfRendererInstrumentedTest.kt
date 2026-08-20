/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.export

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.ByteArrayOutputStream

@RunWith(AndroidJUnit4::class)
class HtmlToPdfRendererInstrumentedTest {

    @Test fun renders_valid_pdf() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val html = BauhausHtml.document(
            headerTitle = "Test",
            headerSubtitle = "Erstellt am heute",
            contentHtml = "<section class=\"month\"><h2 class=\"month\">Juni 2026</h2>" +
                "<div class=\"entry\"><div class=\"when\">Heute</div>" +
                "<div class=\"title\">Beispiel &amp; Emoji 🎉</div></div></section>"
        )
        val out = ByteArrayOutputStream()
        runBlocking { HtmlToPdfRenderer(ctx).render(html, out) }

        val bytes = out.toByteArray()
        assertTrue("PDF sollte nicht leer sein", bytes.size > 200)
        val magic = String(bytes.copyOfRange(0, 5), Charsets.US_ASCII)
        assertTrue("Sollte mit %PDF- beginnen, war: $magic", magic == "%PDF-")
    }
}

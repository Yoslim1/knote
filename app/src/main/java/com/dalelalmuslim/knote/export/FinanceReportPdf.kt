/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.export

import com.dalelalmuslim.knote.ui.AppCurrency

object FinanceReportPdf {

    data class Group(val name: String, val total: Double, val items: List<Pair<String, Double>>)

    data class Section(
        val heading: String,
        val groups: List<Group>,
        val recurring: Double,
        val total: Double,
    )

    fun html(
        title: String,
        subtitle: String,
        sections: List<Section>,
        currency: AppCurrency,
        monthTotalLabel: String,
        inclRecurringTemplate: String,
    ): String {
        val content = buildString {
            for (s in sections) {
                append("<section class=\"month\">")
                if (s.heading.isNotBlank()) {
                    append("<h2 class=\"month\">").append(BauhausHtml.escape(s.heading)).append("</h2>")
                }
                for (g in s.groups) {
                    append("<div class=\"fin-group\"><span>")
                    append(BauhausHtml.escape(g.name))
                    append("</span><span class=\"amt\">")
                    append(BauhausHtml.escape(currency.format(g.total)))
                    append("</span></div>")
                    for ((name, amt) in g.items) {
                        append("<div class=\"fin-cat\"><span>")
                        append(BauhausHtml.escape(name))
                        append("</span><span>")
                        append(BauhausHtml.escape(currency.format(amt)))
                        append("</span></div>")
                    }
                }
                if (s.recurring > 0.0) {
                    append("<div class=\"fin-recurring\">")
                    append(BauhausHtml.escape(String.format(inclRecurringTemplate, currency.format(s.recurring))))
                    append("</div>")
                }
                append("<div class=\"fin-total\"><span>")
                append(BauhausHtml.escape(monthTotalLabel))
                append("</span><span>")
                append(BauhausHtml.escape(currency.format(s.total)))
                append("</span></div>")
                append("</section>")
            }
        }
        return BauhausHtml.document(title, subtitle, content)
    }
}

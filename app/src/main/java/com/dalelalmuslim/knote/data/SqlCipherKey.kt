/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.data

internal object SqlCipherKey {

    private val HEX = "0123456789abcdef".map { it.code.toByte() }.toByteArray()
    private val QUOTE = '\''.code.toByte()
    private val X = 'x'.code.toByte()

    fun rawKeyBytes(dek: ByteArray): ByteArray {
        val out = ByteArray(2 + dek.size * 2 + 1)
        out[0] = X
        out[1] = QUOTE
        for (i in dek.indices) {
            val v = dek[i].toInt() and 0xFF
            out[2 + i * 2] = HEX[v ushr 4]
            out[2 + i * 2 + 1] = HEX[v and 0x0F]
        }
        out[out.size - 1] = QUOTE
        return out
    }
}

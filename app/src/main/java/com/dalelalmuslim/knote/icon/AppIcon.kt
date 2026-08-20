/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.icon

enum class AppIcon(val aliasName: String) {
    ORIGINAL("IconOriginal"),
    INVERSE("IconInverse"),
    WARM_EMBER("IconWarmEmber"),
    BLUE("IconBlue"),
    RAINBOW("IconRainbow"),
    RETRO("IconRetro"),
    MINIMALIST("IconMinimalist"),
    PLAYFUL("IconPlayful");

    companion object {
        // Must match the package of the <activity-alias> entries in the manifest.
        const val PACKAGE_PREFIX = "com.dalelalmuslim.knote.icon"
        val DEFAULT = ORIGINAL
    }
}

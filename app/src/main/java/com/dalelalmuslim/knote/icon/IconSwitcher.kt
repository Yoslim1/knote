/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.icon

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object IconSwitcher {

    fun current(context: Context): AppIcon {
        val pm = context.packageManager
        return AppIcon.entries.firstOrNull { icon ->
            pm.getComponentEnabledSetting(component(context, icon)) ==
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        } ?: AppIcon.DEFAULT
    }

    fun apply(context: Context, selected: AppIcon) {
        if (current(context) == selected) return
        val pm = context.packageManager
        AppIcon.entries.forEach { icon ->
            val state = if (icon == selected) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            pm.setComponentEnabledSetting(
                component(context, icon),
                state,
                PackageManager.DONT_KILL_APP,
            )
        }
    }

    private fun component(context: Context, icon: AppIcon): ComponentName =
        ComponentName(context, "${AppIcon.PACKAGE_PREFIX}.${icon.aliasName}")
}

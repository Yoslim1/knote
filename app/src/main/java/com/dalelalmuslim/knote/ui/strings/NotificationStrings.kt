/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.strings

import android.content.Context
import com.dalelalmuslim.knote.R

internal class NotificationStrings(
    val menu: String,
    val menuSubtitle: String,
    val section: String,
    val enableLabel: String,
    val enableHint: String,
    val leadLabel: String,
    val permissionHint: String,
    val leadAtTime: String,
    val lead5Min: String,
    val lead10Min: String,
    val lead15Min: String,
    val lead30Min: String,
    val lead1Hour: String,
)

internal fun notificationStrings(ctx: Context): NotificationStrings = NotificationStrings(
    menu = ctx.getString(R.string.notif_menu),
    menuSubtitle = ctx.getString(R.string.notif_menu_subtitle),
    section = ctx.getString(R.string.notif_section),
    enableLabel = ctx.getString(R.string.notif_enable_label),
    enableHint = ctx.getString(R.string.notif_enable_hint),
    leadLabel = ctx.getString(R.string.notif_lead_label),
    permissionHint = ctx.getString(R.string.notif_permission_hint),
    leadAtTime = ctx.getString(R.string.notif_lead_at_time),
    lead5Min = ctx.getString(R.string.notif_lead_5min),
    lead10Min = ctx.getString(R.string.notif_lead_10min),
    lead15Min = ctx.getString(R.string.notif_lead_15min),
    lead30Min = ctx.getString(R.string.notif_lead_30min),
    lead1Hour = ctx.getString(R.string.notif_lead_1hour),
)

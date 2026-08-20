/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.screens
import com.dalelalmuslim.knote.ui.components.*
import com.dalelalmuslim.knote.ui.*

import com.dalelalmuslim.knote.ui.strings.*

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.dalelalmuslim.knote.data.AppSettings

@Composable
internal fun KalenderSection(
    settings: AppSettings,
    notificationsPermissionGranted: Boolean,
    onSetNotificationsEnabled: (Boolean) -> Unit,
    onSetNotificationLead: (Int) -> Unit,
    onSetShowHolidays: (Boolean) -> Unit,
    onSetHolidayCountry: (String) -> Unit,
    onSetHolidayRegion: (String) -> Unit,
    onSetIncludeHolidaysInExport: (Boolean) -> Unit,
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        NotificationsSection(
            settings = settings,
            permissionGranted = notificationsPermissionGranted,
            onSetEnabled = onSetNotificationsEnabled,
            onSetLead = onSetNotificationLead,
        )
        HolidaysSection(
            settings = settings,
            onSetShowHolidays = onSetShowHolidays,
            onSetCountry = onSetHolidayCountry,
            onSetRegion = onSetHolidayRegion,
            onSetIncludeInExport = onSetIncludeHolidaysInExport,
        )
    }
}

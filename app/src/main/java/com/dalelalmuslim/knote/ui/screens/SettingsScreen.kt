/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.screens
import com.dalelalmuslim.knote.ui.components.soundClick

import com.dalelalmuslim.knote.ui.strings.*

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dalelalmuslim.knote.data.AppSettings
import com.dalelalmuslim.knote.data.Category
import com.dalelalmuslim.knote.ui.theme.LocalAppColors
import kotlinx.collections.immutable.ImmutableList

enum class SettingsSection { Darstellung, Features, Kalender, Finanzen, Sicherheit, Export, Daten, Lizenzen }

@Composable
fun SettingsScreen(
    categories: ImmutableList<Category>,
    settings: AppSettings,
    onClose: () -> Unit,
    onSetFinanceEnabled: (Boolean) -> Unit,
    onSetMindfulnessEnabled: (Boolean) -> Unit,
    onSetCategoryEnabled: (Category, Boolean) -> Unit,
    onSetCategoryRecurringCost: (Category, Double) -> Unit,
    onAddCategory: (name: String, group: String) -> Unit,
    onDeleteCategory: (Category) -> Unit,
    onResetCategories: () -> Unit,
    onDeleteFinanceData: () -> Unit,
    onDeleteAllTasks: () -> Unit,
    onDeleteAllAppointments: () -> Unit,
    onDeleteAllHabits: () -> Unit,
    onDeleteAllNotes: () -> Unit,
    onDeleteAllMindfulness: () -> Unit,
    onSetThemeMode: (String) -> Unit,
    onSetFontScale: (Float) -> Unit,
    onSetLanguage: (String) -> Unit,
    onSetSalary: (Double) -> Unit,
    onSetSalaryDay: (String) -> Unit,
    onSetConfirmDelete: (Boolean) -> Unit,
    onSetHaptic: (Boolean) -> Unit,
    onSetNewNoteStartsWithTitle: (Boolean) -> Unit,
    onSetCurrency: (String) -> Unit,
    onSetAppLockTimeout: (Int) -> Unit,
    onSetBlockScreenshots: (Boolean) -> Unit,
    onSetNotificationsEnabled: (Boolean) -> Unit,
    onSetNotificationLead: (Int) -> Unit,
    onSetShowHolidays: (Boolean) -> Unit,
    onSetHolidayCountry: (String) -> Unit,
    onSetHolidayRegion: (String) -> Unit,
    onSetIncludeHolidaysInExport: (Boolean) -> Unit,
    notificationsPermissionGranted: Boolean = true,
    initialSection: SettingsSection? = null
) {
    val strings = LocalAppStrings.current
    val colors  = LocalAppColors.current
    var selectedSection by remember { mutableStateOf(initialSection) }

    val title: String = when (selectedSection) {
        null                    -> strings.settings
        SettingsSection.Darstellung -> strings.menuDisplay
        SettingsSection.Features    -> strings.menuFeatures
        SettingsSection.Kalender    -> strings.menuCalendar
        SettingsSection.Finanzen    -> strings.menuFinance
        SettingsSection.Sicherheit  -> strings.menuSecurity
        SettingsSection.Export      -> strings.menuExport
        SettingsSection.Daten       -> strings.menuData
        SettingsSection.Lizenzen    -> strings.menuLicenses
    }
    val onBack: () -> Unit = if (selectedSection != null) {
        { selectedSection = null }
    } else {
        onClose
    }

    BackHandler(enabled = selectedSection != null) { selectedSection = null }

    LaunchedEffect(settings.financeTabEnabled) {
        if (!settings.financeTabEnabled && selectedSection == SettingsSection.Finanzen) {
            selectedSection = null
        }
    }

    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .pointerInput(Unit) { detectTapGestures { focusManager.clearFocus() } }
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.topBar)
                .statusBarsPadding()
                .padding(end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = soundClick(onBack)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = colors.onSurface)
            }
            Text(title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = colors.onSurface)
        }

        when (selectedSection) {
            null -> SettingsMainMenu(
                strings     = strings,
                showFinance = settings.financeTabEnabled,
                onSelect    = { selectedSection = it }
            )
            SettingsSection.Darstellung -> DarstellungSection(
                settings = settings,
                onSetThemeMode = onSetThemeMode,
                onSetFontScale = onSetFontScale,
                onSetLanguage  = onSetLanguage,
                onSetConfirmDelete = onSetConfirmDelete,
                onSetHaptic = onSetHaptic,
                onSetNewNoteStartsWithTitle = onSetNewNoteStartsWithTitle
            )
            SettingsSection.Features -> FeaturesSection(
                settings = settings,
                onSetFinanceEnabled = onSetFinanceEnabled,
                onSetMindfulnessEnabled = onSetMindfulnessEnabled
            )
            SettingsSection.Finanzen -> FinanzenSection(
                categories = categories,
                settings = settings,
                onSetFinanceEnabled = onSetFinanceEnabled,
                onSetCategoryEnabled = onSetCategoryEnabled,
                onSetCategoryRecurringCost = onSetCategoryRecurringCost,
                onAddCategory = onAddCategory,
                onDeleteCategory = onDeleteCategory,
                onResetCategories = onResetCategories,
                onSetSalary = onSetSalary,
                onSetSalaryDay = onSetSalaryDay,
                onSetCurrency = onSetCurrency,
            )
            SettingsSection.Kalender -> KalenderSection(
                settings = settings,
                notificationsPermissionGranted = notificationsPermissionGranted,
                onSetNotificationsEnabled = onSetNotificationsEnabled,
                onSetNotificationLead = onSetNotificationLead,
                onSetShowHolidays = onSetShowHolidays,
                onSetHolidayCountry = onSetHolidayCountry,
                onSetHolidayRegion = onSetHolidayRegion,
                onSetIncludeHolidaysInExport = onSetIncludeHolidaysInExport,
            )
            SettingsSection.Sicherheit -> SicherheitSection(
                settings = settings,
                onSetAppLockTimeout = onSetAppLockTimeout,
                onSetBlockScreenshots = onSetBlockScreenshots,
            )
            SettingsSection.Export -> ExportSection()
            SettingsSection.Daten -> DatenSection(
                onDeleteFinanceData    = onDeleteFinanceData,
                onDeleteAllTasks       = onDeleteAllTasks,
                onDeleteAllAppointments = onDeleteAllAppointments,
                onDeleteAllHabits      = onDeleteAllHabits,
                onDeleteAllNotes       = onDeleteAllNotes,
                onDeleteAllMindfulness = onDeleteAllMindfulness
            )
            SettingsSection.Lizenzen -> OpenSourceLicensesScreen()
        }
    }
}

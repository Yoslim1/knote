/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.screens
import com.dalelalmuslim.knote.ui.components.*
import com.dalelalmuslim.knote.ui.*

import com.dalelalmuslim.knote.ui.strings.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dalelalmuslim.knote.ui.theme.LocalAppColors

@Composable
internal fun SettingsMainMenu(
    strings: AppStrings,
    showFinance: Boolean,
    onSelect: (SettingsSection) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        SettingsMenuRow(
            icon = Icons.Default.Palette,
            iconColor = Color(0xFF7C4DFF),
            label = strings.menuDisplay,
            subtitle = strings.menuDisplaySubtitle,
            onClick = { onSelect(SettingsSection.Darstellung) }
        )
        Spacer(Modifier.height(10.dp))
        SettingsMenuRow(
            icon = Icons.Default.Tune,
            iconColor = Color(0xFF00B8D4),
            label = strings.menuFeatures,
            subtitle = strings.menuFeaturesSubtitle,
            onClick = { onSelect(SettingsSection.Features) }
        )
        Spacer(Modifier.height(10.dp))
        SettingsMenuRow(
            icon = Icons.Default.CalendarMonth,
            iconColor = Color(0xFFFF7043),
            label = strings.menuCalendar,
            subtitle = strings.menuCalendarSubtitle,
            onClick = { onSelect(SettingsSection.Kalender) }
        )
        // Nothing to set up while the tab itself is switched off.
        if (showFinance) {
            Spacer(Modifier.height(10.dp))
            SettingsMenuRow(
                icon = Icons.Default.MonetizationOn,
                iconColor = Color(0xFF00C853),
                label = strings.menuFinance,
                subtitle = strings.menuFinanceSubtitle,
                onClick = { onSelect(SettingsSection.Finanzen) }
            )
        }
        Spacer(Modifier.height(10.dp))
        SettingsMenuRow(
            icon = Icons.Default.Lock,
            iconColor = Color(0xFF455A64),
            label = strings.menuSecurity,
            subtitle = strings.menuSecuritySubtitle,
            onClick = { onSelect(SettingsSection.Sicherheit) }
        )
        Spacer(Modifier.height(10.dp))
        SettingsMenuRow(
            icon = Icons.Default.Share,
            iconColor = Color(0xFF2979FF),
            label = strings.menuExport,
            subtitle = strings.menuExportSubtitle,
            onClick = { onSelect(SettingsSection.Export) }
        )
        Spacer(Modifier.height(10.dp))
        SettingsMenuRow(
            icon = Icons.Default.Storage,
            iconColor = Color(0xFFFF5252),
            label = strings.menuData,
            subtitle = strings.menuDataSubtitle,
            onClick = { onSelect(SettingsSection.Daten) }
        )
        Spacer(Modifier.height(10.dp))
        SettingsMenuRow(
            icon = Icons.Default.Info,
            iconColor = Color(0xFF00ACC1),
            label = strings.menuLicenses,
            subtitle = strings.menuLicensesSubtitle,
            onClick = { onSelect(SettingsSection.Lizenzen) }
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SettingsMenuRow(
    icon: ImageVector,
    iconColor: Color,
    label: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = LocalAppColors.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = colors.onSurface)
                Text(subtitle, fontSize = 13.sp, color = colors.onSurfaceSecondary)
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = colors.onSurfaceTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

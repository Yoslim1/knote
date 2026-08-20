/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.screens
import com.dalelalmuslim.knote.ui.components.*
import com.dalelalmuslim.knote.ui.*

import com.dalelalmuslim.knote.ui.strings.*

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dalelalmuslim.knote.data.AppSettings
import java.text.Collator
import com.dalelalmuslim.knote.holidays.HolidayCatalog
import com.dalelalmuslim.knote.holidays.HolidayDefaults
import com.dalelalmuslim.knote.holidays.HolidayNames
import com.dalelalmuslim.knote.holidays.localizedFor
import com.dalelalmuslim.knote.ui.theme.LocalAppColors

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
internal fun HolidaysSection(
    settings: AppSettings,
    onSetShowHolidays: (Boolean) -> Unit,
    onSetCountry: (String) -> Unit,
    onSetRegion: (String) -> Unit,
    onSetIncludeInExport: (Boolean) -> Unit,
) {
    val strings = LocalAppStrings.current
    val colors  = LocalAppColors.current
    val baseContext = LocalContext.current
    val context = remember(strings.locale, baseContext) { baseContext.localizedFor(strings.locale) }
    val h = remember(strings.locale) { holidayStrings(context) }

    val effectiveCountry = remember(settings.holidayCountry) {
        HolidayDefaults.resolveCountry(context, settings.holidayCountry)
    }

    val chipColors = FilterChipDefaults.filterChipColors(
        selectedContainerColor = colors.accent,
        selectedLabelColor     = Color.White,
        containerColor         = colors.surfaceVariant,
        labelColor             = colors.onSurface
    )

    Column {
        Spacer(Modifier.height(20.dp))
        SectionLabel(h.section)
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(h.showLabel, fontSize = 15.sp, color = colors.onSurface)
                        Text(h.showHint, fontSize = 13.sp, color = colors.onSurfaceSecondary)
                    }
                    Switch(
                        checked = settings.showHolidays,
                        onCheckedChange = soundCheck(onSetShowHolidays),
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.accent)
                    )
                }

                if (settings.showHolidays) {
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = colors.divider)
                    Spacer(Modifier.height(12.dp))

                    val holidayCollator = Collator.getInstance(strings.locale)
                    Text(h.countryLabel, fontSize = 13.sp, color = colors.onSurfaceSecondary)
                    Spacer(Modifier.height(10.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        HolidayCatalog.countries
                            .sortedWith(compareBy(holidayCollator) { HolidayNames.resolve(context, it.labelKey) })
                            .forEach { country ->
                            FilterChip(
                                selected = country.iso == effectiveCountry.iso,
                                onClick  = soundClick { onSetCountry(country.iso) },
                                label    = { Text(HolidayNames.resolve(context, country.labelKey)) },
                                colors   = chipColors
                            )
                        }
                    }

                    if (effectiveCountry.hasSubdivisions) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = colors.divider)
                        Spacer(Modifier.height(12.dp))

                        Text(h.regionLabel, fontSize = 13.sp, color = colors.onSurfaceSecondary)
                        Spacer(Modifier.height(10.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = settings.holidayRegion.isBlank(),
                                onClick  = soundClick { onSetRegion("") },
                                label    = { Text(HolidayNames.resolve(context, "region_nationwide")) },
                                colors   = chipColors
                            )
                            effectiveCountry.subdivisions
                                .sortedWith(compareBy(holidayCollator) { HolidayNames.resolve(context, it.labelKey) })
                                .forEach { region ->
                                val iso = region.subdivisionIso ?: return@forEach
                                FilterChip(
                                    selected = settings.holidayRegion == iso,
                                    onClick  = soundClick { onSetRegion(iso) },
                                    label    = { Text(HolidayNames.resolve(context, region.labelKey)) },
                                    colors   = chipColors
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = colors.divider)
                    Spacer(Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(h.exportLabel, fontSize = 15.sp, color = colors.onSurface)
                            Text(h.exportHint, fontSize = 13.sp, color = colors.onSurfaceSecondary)
                        }
                        Switch(
                            checked = settings.includeHolidaysInExport,
                            onCheckedChange = soundCheck(onSetIncludeInExport),
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = colors.accent)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

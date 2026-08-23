/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dalelalmuslim.knote.ui.strings.LocalAppStrings
import com.dalelalmuslim.knote.ui.theme.LocalAppColors


enum class AppTab { TODAY, FINANCE, NOTES }

@Composable
fun BottomBar(
    currentTab: AppTab,
    onTabChange: (AppTab) -> Unit,
    modifier: Modifier = Modifier,
    showFinance: Boolean = true,
) {
    val strings = LocalAppStrings.current
    val colors = LocalAppColors.current
    val navigationItems = buildList {
        add(AppTab.TODAY to (strings.tabToday to Icons.Default.Today))
        if (showFinance) add(AppTab.FINANCE to (strings.tabFinance to Icons.Default.Savings))
        add(AppTab.NOTES to (strings.tabNotes to Icons.Default.EditNote))
    }

    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = colors.bottomBar,
        tonalElevation = 0.dp,
    ) {
        navigationItems.forEach { (tab, labelAndIcon) ->
            val (label, icon) = labelAndIcon
            NavigationBarItem(
                selected = currentTab == tab,
                onClick = { onTabChange(tab) },
                icon = { Icon(icon, contentDescription = label) },
                label = {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = colors.accent,
                    selectedTextColor = colors.accent,
                    indicatorColor = colors.accentContainer,
                    unselectedIconColor = colors.onSurfaceSecondary,
                    unselectedTextColor = colors.onSurfaceSecondary,
                ),
            )
        }
    }
}

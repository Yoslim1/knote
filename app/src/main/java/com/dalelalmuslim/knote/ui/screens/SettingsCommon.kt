/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.screens
import com.dalelalmuslim.knote.ui.components.*
import com.dalelalmuslim.knote.ui.*

import com.dalelalmuslim.knote.ui.strings.*

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dalelalmuslim.knote.ui.theme.LocalAppColors

@Composable
internal fun SectionLabel(text: String) {
    val colors = LocalAppColors.current
    Text(
        text = text.uppercase(),
        fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.onSurfaceSecondary,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
    )
}

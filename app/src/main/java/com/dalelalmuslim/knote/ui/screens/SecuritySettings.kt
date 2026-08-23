/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.screens

import com.dalelalmuslim.knote.ui.strings.*

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dalelalmuslim.knote.data.AppSettings
import com.dalelalmuslim.knote.security.KeyMode
import com.dalelalmuslim.knote.security.SECURITY_ERROR_GENERIC
import com.dalelalmuslim.knote.security.LocalSecurityController
import com.dalelalmuslim.knote.ui.theme.LocalAppColors

@Composable
internal fun SicherheitSection(
    settings: AppSettings,
    onSetAppLockTimeout: (Int) -> Unit,
    onSetBlockScreenshots: (Boolean) -> Unit,
) {
    val colors = LocalAppColors.current
    val controller = LocalSecurityController.current
    val context = LocalContext.current
    val s = remember(LocalAppStrings.current.locale) { securityStrings(context) }

    var showEnableWarning by remember { mutableStateOf(false) }
    var showSetupPassphrase by remember { mutableStateOf(false) }
    var showChangePassphrase by remember { mutableStateOf(false) }
    var showDisableViaPassphrase by remember { mutableStateOf(false) }
    var recoveryCodeToShow by remember { mutableStateOf<String?>(null) }
    var showSwitchToBiometric by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        SecSectionLabel(s.sectionAppLock)

        SecCard {
            SecToggleRow(
                title = s.appLockTitle,
                subtitle = s.appLockHint,
                checked = controller.appLockEnabled,
                onCheckedChange = { enable ->
                    if (enable) {
                        if (controller.mode == KeyMode.KEYSTORE_NO_LOCK) showEnableWarning = true
                    } else {
                        controller.disableLock(onNeedPassphrase = { showDisableViaPassphrase = true })
                    }
                },
            )
        }

        Spacer(Modifier.height(10.dp))
        SecCard {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(s.protectionLevel, color = colors.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text(
                        text = when (controller.mode) {
                            KeyMode.KEYSTORE_NO_LOCK -> s.levelNoLock
                            KeyMode.KEYSTORE_LOCK -> s.levelBiometric
                            KeyMode.PASSPHRASE -> s.levelPassphrase
                        },
                        color = colors.onSurfaceSecondary,
                        fontSize = 13.sp,
                    )
                }
            }
        }

        if (controller.appLockEnabled) {
            Spacer(Modifier.height(10.dp))
            SecCard {
                Column(Modifier.padding(16.dp)) {
                    Text(s.timeoutTitle, color = colors.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(12.dp))
                    SecSegmented(
                        options = listOf(0 to s.timeoutImmediate, 60 to s.timeoutOneMin, 300 to s.timeoutFiveMin),
                        selected = settings.appLockTimeoutSeconds,
                        onSelect = onSetAppLockTimeout,
                    )
                }
            }
        }

        Spacer(Modifier.height(10.dp))
        SecCard {
            if (controller.mode == KeyMode.PASSPHRASE) {
                SecClickRow(title = s.changePassphrase, onClick = { showChangePassphrase = true })
            } else {
                SecClickRow(title = s.setupPassphrase, subtitle = s.setupPassphraseHint, onClick = { showSetupPassphrase = true })
            }
        }

        if (controller.mode == KeyMode.PASSPHRASE) {
            Spacer(Modifier.height(10.dp))
            SecCard {
                SecClickRow(
                    title = s.switchToBiometric,
                    subtitle = s.switchToBiometricHint,
                    onClick = { showSwitchToBiometric = true },
                )
            }
        }

        if (controller.appLockEnabled) {
            Spacer(Modifier.height(10.dp))
            SecCard {
                SecClickRow(
                    title = if (controller.hasRecovery) s.recoveryRegenerate else s.recoverySetup,
                    subtitle = s.recoveryHint,
                    onClick = { controller.regenerateRecoveryCode { code -> recoveryCodeToShow = code } },
                )
            }
        }

        controller.lastError?.let { error ->
            Spacer(Modifier.height(12.dp))
            Text(
                text = if (error == SECURITY_ERROR_GENERIC) s.securityErrorGeneric else error,
                color = Color(0xFFFF6B6B),
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }

        Spacer(Modifier.height(24.dp))
        SecSectionLabel(s.sectionScreen)
        SecCard {
            SecToggleRow(
                title = s.blockScreenshotsTitle,
                subtitle = s.blockScreenshotsHint,
                checked = settings.blockScreenshots,
                onCheckedChange = onSetBlockScreenshots,
            )
        }

        Spacer(Modifier.height(24.dp))
        SecSectionLabel(s.sectionBackup)
        BackupCard(s)

        Spacer(Modifier.height(32.dp))
    }

    if (controller.busy) {
        SecBusyDialog(s.lockBusyTitle, s.lockBusyHint)
    }

    if (showEnableWarning) {
        SecConfirmDialog(
            title = s.enableWarnTitle,
            message = s.enableWarnMessage,
            confirmLabel = s.activate,
            dismissLabel = s.cancel,
            onConfirm = { showEnableWarning = false; controller.enableBiometricLock { code -> recoveryCodeToShow = code } },
            onDismiss = { showEnableWarning = false },
        )
    }

    if (showSwitchToBiometric) {
        ConfirmPassphraseDialog(
            title = s.switchToBiometric,
            message = s.switchToBiometricMessage,
            s = s,
            onConfirm = { chars ->
                showSwitchToBiometric = false
                controller.switchToBiometric(chars, onWrong = { controller.lastError = s.wrongPassphrase })
            },
            onDismiss = { showSwitchToBiometric = false },
        )
    }

    recoveryCodeToShow?.let { code ->
        RecoveryCodeDialog(code = code, onDone = { recoveryCodeToShow = null })
    }

    if (showSetupPassphrase) {
        NewPassphraseDialog(
            title = s.setupPassphrase,
            warning = s.passphraseNoResetWarning,
            newLabel = s.newPassphrase,
            confirmLabel = s.confirmPassphrase,
            s = s,
            onConfirm = { newChars ->
                showSetupPassphrase = false
                controller.setupPassphrase(newChars) { code -> code?.let { recoveryCodeToShow = it } }
            },
            onDismiss = { showSetupPassphrase = false },
        )
    }

    if (showChangePassphrase) {
        ChangePassphraseDialog(
            s = s,
            onConfirm = { oldChars, newChars ->
                controller.changePassphrase(oldChars, newChars, onDone = { showChangePassphrase = false })
            },
            onDismiss = { showChangePassphrase = false },
        )
    }

    if (showDisableViaPassphrase) {
        ConfirmPassphraseDialog(
            title = s.disableLockTitle,
            message = s.disableViaPassphrase,
            s = s,
            onConfirm = { chars ->
                showDisableViaPassphrase = false
                controller.disableLock(currentPassphrase = chars)
            },
            onDismiss = { showDisableViaPassphrase = false },
        )
    }
}

/* Copyright (C) 2026 Tom Frischmuth — GPLv3. Modified by Yosef, 2026. */

package com.dalelalmuslim.knote.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.compose.runtime.Composable
import com.dalelalmuslim.knote.security.SecurityGate

/**
 * A launcher for one of the system's document dialogs that the lock gate does not
 * mistake for the user leaving.
 *
 * Picking a file runs in a separate system activity, so the app goes through
 * onStop and comes back to an immediate relock — in the middle of writing down a
 * recovery code, for instance, where the app lock is not even fully set up yet.
 * This is the same case [SecurityGate.authInProgress] already covers for the
 * biometric prompt.
 *
 * Returns the launch function rather than the launcher itself, so there is no
 * second way to open the dialog that skips the flag.
 */
@Composable
internal fun <I, O> rememberDocumentPicker(
    contract: ActivityResultContract<I, O>,
    onResult: (O) -> Unit,
): (I) -> Unit {
    val launcher = rememberLauncherForActivityResult(contract) { result ->
        SecurityGate.authInProgress = false
        onResult(result)
    }
    return { input ->
        SecurityGate.authInProgress = true
        launcher.launch(input)
    }
}

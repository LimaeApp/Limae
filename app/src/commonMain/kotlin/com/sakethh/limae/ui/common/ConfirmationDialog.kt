package com.sakethh.limae.ui.common

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun ConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    confirmText: String = "Delete All",
) {
    var showProgressBar by rememberSaveable {
        mutableStateOf(false)
    }
    AlertDialog(
        onDismissRequest = {
            if (!showProgressBar) {
                onDismissRequest()
            }
        },
        confirmButton = {
            AnimatedVisibility(!showProgressBar) {
                Button(
                    modifier = Modifier.showHandOnHover().fillMaxWidth(),
                    onClick = {
                        if (showProgressBar) return@Button

                        showProgressBar = true
                        onConfirm()
                    },
                ) {
                    Text(
                        text = confirmText,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        },
        dismissButton =
            if (!showProgressBar) {
                {
                    OutlinedButton(
                        modifier = Modifier.showHandOnHover().fillMaxWidth(),
                        onClick = {
                            if (!showProgressBar) {
                                onDismissRequest()
                            }
                        },
                    ) {
                        Text(
                            text = "Cancel",
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                }
            } else {
                null
            },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 24.sp,
            )
        },
        text =
            if (!showProgressBar) {
                null
            } else {
                {
                    AnimatedVisibility(showProgressBar) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            },
    )
}

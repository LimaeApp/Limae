package com.sakethh.limae.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
fun ConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    confirmText: String = "Delete All",
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            Button(
                modifier = Modifier.showHandOnHover().fillMaxWidth(),
                onClick = onConfirm,
            ) {
                Text(
                    text = confirmText,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                modifier = Modifier.showHandOnHover().fillMaxWidth(),
                onClick = onDismissRequest,
            ) {
                Text(
                    text = "Cancel",
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontSize = 24.sp,
            )
        },
    )
}

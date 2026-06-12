/*
 * Copyright 2025 | Dmitri Chernysh | https://github.com/dmitriy-chernysh
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.mobiledevpro.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.mobiledevpro.ui.theme.AppTheme

/**
 * Common Alert Dialog
 *
 * Created on Jan 28, 2024.
 *
 */
@Composable
fun AppAlertDialog(
    icon: ImageVector? = null,
    dialogTitle: String? = null,
    dialogText: String,
    confirmButtonText: String,
    dismissButtonText: String? = null,
    onConfirm: () -> Unit,
    onCancel: () -> Unit = {}
) {
    AlertDialog(
        icon = {
            icon?.let { ic ->
                Icon(imageVector = ic, contentDescription = "")
            }
        },
        title = {
            dialogTitle?.let {
                Text(
                    text = dialogTitle,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.MiddleEllipsis,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        },
        text = {
            Text(text = dialogText, style = MaterialTheme.typography.bodyMedium)
        },
        onDismissRequest = {
            onCancel()
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                }
            ) {
                Text(confirmButtonText, style = MaterialTheme.typography.bodyMedium)
            }
        },
        dismissButton = {
            dismissButtonText?.let { text ->
                TextButton(
                    onClick = {
                        onCancel()
                    }
                ) {
                    Text(text, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
    )
}

@Preview
@Composable
internal fun AppAlertDialogPreview() {
    AppTheme {
        AppAlertDialog(
            dialogTitle = "Title",
            dialogText = "Text Text Text Text Text Text Text Text Text ",
            confirmButtonText = "Save PDF",
            dismissButtonText = "Discard",
            onConfirm = {}
        )
    }
}
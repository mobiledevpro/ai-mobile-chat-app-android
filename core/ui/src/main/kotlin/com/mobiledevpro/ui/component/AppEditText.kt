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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mobiledevpro.ui.theme.AppTheme

/**
 *
 * Created on Mar 21, 2024.
 *
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppEditText(
    value: () -> String,
    modifier: Modifier = Modifier,
    hint: String? = null,
    allCaps: Boolean = false,
    enabled: Boolean = true,
    errMessage: String = "",
    maxLength: Int? = null,
    singleLine: Boolean = true,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    onValueChange: (String) -> Unit,
) {

    var textState by remember { mutableStateOf(TextFieldValue(value())) }

    LaunchedEffect(value()) {
        textState = TextFieldValue(value(), selection = TextRange(value().length))
    }

    TextField(
        modifier = modifier,
        value = textState,
        onValueChange = { changedValue ->
            textState = changedValue

            val resultStr = maxLength?.let { length ->
                if (changedValue.text.length > maxLength)
                    changedValue.text.substring(0, length)
                else
                    changedValue.text
            } ?: changedValue.text

            if (value() != resultStr)
                if (allCaps)
                    onValueChange(resultStr.uppercase())
                else
                    onValueChange(resultStr)
        },
        readOnly = !enabled,
        singleLine = singleLine,
        maxLines = if (singleLine) 1 else 10,
        minLines = if (singleLine) 1 else 2,
        keyboardOptions = keyboardOptions.copy(capitalization = if (allCaps) KeyboardCapitalization.Characters else KeyboardCapitalization.None),
        prefix = prefix,
        suffix = suffix,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(16.dp),
        placeholder = { hint?.let { Text(text = it, fontSize = 14.sp) } },
        isError = errMessage.isNotEmpty(),
        supportingText = {
            Text(
                text = errMessage,
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.error)
            )
        },
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            unfocusedPrefixColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            focusedPrefixColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            disabledPrefixColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            errorPrefixColor = MaterialTheme.colorScheme.error,
            unfocusedSuffixColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            disabledSuffixColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            errorSuffixColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            focusedSuffixColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            errorPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        keyboardActions = keyboardActions
    )

}

@Preview
@Composable
fun AppEditTextPreview() {
    AppTheme {
        AppEditText(
            modifier = Modifier.fillMaxWidth(),
            value = { "" },
            hint = "FFFFFF",
            onValueChange = {}
        )
    }
}
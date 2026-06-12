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
package com.mobiledevpro.chat.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mobiledevpro.ui.R
import com.mobiledevpro.ui.component.AppEditText


@Composable
fun PromptBar(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    query: String,
    isFocused: Boolean,
    onQueryChanged: (String) -> Unit,
    onSend: () -> Unit
) {

    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = modifier
    ) {
        AppEditText(
            value = { query },
            hint = stringResource(R.string.label_ask_ai),
            enabled = enabled,
            singleLine = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done,
                showKeyboardOnFocus = true
            ),
            onValueChange = onQueryChanged,
            trailingIcon = {
                IconButton(enabled = enabled, onClick = {
                    onSend()
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_send_24dp),
                        contentDescription = "",
                        tint = Color.Gray
                    )
                }
            }
        )
    }

    LaunchedEffect(isFocused) {
        try {
            if (isFocused)
                focusRequester.requestFocus()
        } catch (_: Throwable) {
        }
    }
}
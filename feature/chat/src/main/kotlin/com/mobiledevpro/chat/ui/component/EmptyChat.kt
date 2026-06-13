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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mobiledevpro.chat.domain.model.PredefinedPrompt
import com.mobiledevpro.ui.R
import com.mobiledevpro.ui.theme.AppTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun EmptyChat(
    modifier: Modifier = Modifier,
    predefinedPromptList: () -> List<PredefinedPrompt>,
    onPredefinedPromptClick: (String) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.message_chat_with_llm),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.secondary
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            predefinedPromptList().forEach { prompt ->
                OutlinedButton(
                    onClick = { onPredefinedPromptClick(prompt.message) },
                    modifier = Modifier.padding(horizontal = 4.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = prompt.message,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
@Preview
private fun EmptyChatPreview() {
    AppTheme {
        EmptyChat(
            predefinedPromptList = {
                listOf(
                    PredefinedPrompt("Tell me a joke"),
                    PredefinedPrompt("Tell me a historical fact"),
                    PredefinedPrompt("Search for BTC price"),
                    PredefinedPrompt("Show my schedule")
                )
            }
        )
    }
}

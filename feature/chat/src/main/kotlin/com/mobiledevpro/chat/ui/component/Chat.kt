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

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mobiledevpro.chat.domain.model.AIState
import com.mobiledevpro.chat.domain.model.AgentSkill
import com.mobiledevpro.chat.domain.model.ChatMessage
import com.mobiledevpro.chat.domain.model.ChatMessageCollection
import com.mobiledevpro.chat.domain.model.PredefinedPrompt
import com.mobiledevpro.chat.domain.model.isBusy


@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun Chat(
    modifier: Modifier = Modifier,
    messageCollection: ChatMessageCollection,
    warningMessage: String = "",
    aiState: AIState,
    predefinedPromptList: () -> List<PredefinedPrompt>,
    availableSkills: () -> List<AgentSkill>,
    onSendPrompt: (String) -> Unit
) {

    val notSentMessage by rememberUpdatedState<ChatMessage?>(messageCollection.getLastNotSentMessage())
    var promptQuery by remember { mutableStateOf<String>("") }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .weight(1f, fill = true)
        ) {
            if (messageCollection.isEmpty())
                EmptyChat(
                    modifier = Modifier.align(Alignment.Center),
                    predefinedPromptList = predefinedPromptList,
                    onPredefinedPromptClick = onSendPrompt
                )
            else
                ChatMessagesList(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(
                            RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomEnd = 0.dp,
                                bottomStart = 8.dp
                            )
                        ),
                    messageCollection = messageCollection,
                    aiState = aiState,
                    warningMessage = warningMessage
                )
        }

        PromptBar(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
                .imePadding()
                .heightIn(min = 88.dp)
                .padding(top = 16.dp),
            enabled = !aiState.isBusy(),
            query = promptQuery,
            availableSkills = availableSkills,
            onQueryChanged = { query ->
                promptQuery = query
            },
            onSend = {
                if (promptQuery.isNotBlank())
                    onSendPrompt(promptQuery)

                promptQuery = ""
            },
            isFocused = false
        )
    }

    LaunchedEffect(notSentMessage) {
        notSentMessage?.also { message ->
            promptQuery = message.text
        }
    }
}
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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.isImeVisible
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mobiledevpro.chat.domain.model.AIState
import com.mobiledevpro.chat.domain.model.ChatMessageCollection
import com.mobiledevpro.chat.domain.model.isBusy
import com.mobiledevpro.ui.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

/**
 * 
 * Created on Jun 12, 2026.
 *
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatMessagesList(
    modifier: Modifier = Modifier,
    messageCollection: ChatMessageCollection,
    aiState: AIState,
    warningMessage: String = ""
) {

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val isKeyboardOpen by rememberUpdatedState(WindowInsets.isImeVisible)

    val userMessageModifier = Modifier
        .padding(top = 16.dp)
        .background(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(
                topStart = 8.dp,
                topEnd = 8.dp,
                bottomEnd = 0.dp,
                bottomStart = 8.dp
            )
        )
        .padding(8.dp)

    val nonUserMessageModifier = Modifier
        .padding(top = 16.dp)
        .background(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(
                topStart = 8.dp,
                topEnd = 8.dp,
                bottomEnd = 8.dp,
                bottomStart = 0.dp
            )
        )
        .padding(8.dp)

    LazyColumn(
        state = listState,
        modifier = modifier
    ) {
        items(
            count = messageCollection.size(),
            key = {
                messageCollection.list[it].listKey()
            },
            itemContent = { index ->
                val message = messageCollection.list[index]
                ChatBubble(
                    modifier = if (message.isUserMessage)
                        userMessageModifier
                    else
                        nonUserMessageModifier,
                    message = { message }
                )
            })

        if (aiState.isBusy())
            item {
                ChatSystemMessage(
                    message = when (aiState) {
                        AIState.INITIALIZING -> stringResource(R.string.message_initializing)
                        AIState.THINKING -> stringResource(R.string.message_thinking)
                        AIState.SUMMARIZING_CHUNKS -> stringResource(R.string.message_thinking_chunks)
                        AIState.DOWNLOADING_MODEL -> stringResource(R.string.message_downloading)
                        AIState.EXTRACTING_TEXT -> stringResource(R.string.message_extraction)
                        AIState.ERROR -> "Something went wrong with LLM initialization. Try to reinstall the app."
                        else -> ""
                    }
                )
            }

        if (warningMessage.isNotBlank())
            item {
                ChatSystemMessage(message = warningMessage)
            }
    }

    // Scroll messages list to bottom when new message arrives
    LaunchedEffect(messageCollection, warningMessage) {
        if (messageCollection.size() > 1)
            listState.animateScrollToItem(messageCollection.size())
    }

    // Scroll messages list to bottom when keyboard is opened
    LaunchedEffect(isKeyboardOpen) {
        if (!isKeyboardOpen) return@LaunchedEffect
        coroutineScope.launch(Dispatchers.IO) {
            delay(200.milliseconds)
            withContext(Dispatchers.Main) {
                if (messageCollection.size() > 1)
                    listState.animateScrollToItem(messageCollection.size())
            }

        }
    }
}
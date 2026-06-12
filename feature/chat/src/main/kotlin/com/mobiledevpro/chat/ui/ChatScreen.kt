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
package com.mobiledevpro.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mobiledevpro.chat.domain.model.AIState
import com.mobiledevpro.chat.domain.model.ChatMessage
import com.mobiledevpro.chat.domain.model.ChatMessageCollection
import com.mobiledevpro.chat.ui.component.Chat
import com.mobiledevpro.chat.ui.component.Loading
import com.mobiledevpro.chat.ui.state.ChatUIState
import com.mobiledevpro.ui.R
import com.mobiledevpro.ui.component.AppBar
import com.mobiledevpro.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    listUiState: StateFlow<ChatUIState>,
    onClearHistory: () -> Unit,
    onSettings: () -> Unit,
    onSendPrompt: (String) -> Unit
) {

    val state by listUiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {

        AppBar(
            title = stringResource(R.string.app_name),
            navIcon = {
                IconButton(onClick = onSettings) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings_24dp),
                        contentDescription = ""
                    )
                }
            },
            actionButtons = {
                IconButton(onClick = onClearHistory) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_replay_24dp),
                        contentDescription = ""
                    )
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )

        ) {

            when (state) {
                is ChatUIState.Loading ->
                    Loading(
                        modifier = Modifier.align(Alignment.Center),
                        message = (state as ChatUIState.Loading).message
                    )

                is ChatUIState.Success -> {
                    Chat(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .navigationBarsPadding(),
                        messageCollection = (state as ChatUIState.Success).messageCollection,
                        warningMessage = (state as ChatUIState.Success).warningMessage,
                        aiState = (state as ChatUIState.Success).aiState,
                        onSendPrompt = onSendPrompt
                    )
                }
            }
        }

    }


}

@Composable
@Preview
private fun ChatScreenPreview() {
    AppTheme {
        ChatScreen(
            listUiState = MutableStateFlow(
                ChatUIState.Success(
                    messageCollection = ChatMessageCollection(
                        list =
                            listOf(
                                ChatMessage(
                                    timeCreated = System.currentTimeMillis(),
                                    text = "Hello! You can ask any question or attach PDF to get summarized",
                                    isUserMessage = false
                                ),
                                ChatMessage(
                                    timeCreated = System.currentTimeMillis(),
                                    text = "Can you tell me a joke?",
                                    isUserMessage = true
                                ),
                                ChatMessage(
                                    timeCreated = System.currentTimeMillis(),
                                    text = "Sure! Why don't scientists trust atoms? Because they make up everything!",
                                    isUserMessage = false
                                ),
                                ChatMessage(
                                    timeCreated = System.currentTimeMillis(),
                                    text = "Summarize the text",
                                    isUserMessage = true
                                )
                                /* ChatMessage(
                                     text = "",
                                     isUserMessage = true,
                                     notSent = false,
                                     fileUri = Uri.parse("content://com.android.providers.downloads.documents/document/msf%3A1000015894")
                                 )*/

                            )
                    ),
                    warningMessage = "",
                    aiState = AIState.DOWNLOADING_MODEL
                )
            ),
            onSettings = {},
            onClearHistory = {},
            onSendPrompt = {}
        )
    }
}
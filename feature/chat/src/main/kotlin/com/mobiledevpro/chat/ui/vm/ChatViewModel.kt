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
package com.mobiledevpro.chat.ui.vm

import androidx.lifecycle.viewModelScope
import com.mobiledevpro.chat.domain.model.AIState
import com.mobiledevpro.chat.domain.model.ChatMessage
import com.mobiledevpro.chat.domain.model.ChatMessageCollection
import com.mobiledevpro.chat.domain.model.Skill
import com.mobiledevpro.chat.domain.model.SkillType
import com.mobiledevpro.chat.domain.model.toLocalPath
import com.mobiledevpro.chat.domain.model.toName
import com.mobiledevpro.chat.domain.usecase.ClearHistoryUseCase
import com.mobiledevpro.chat.domain.usecase.GetChatMessagesUseCase
import com.mobiledevpro.chat.domain.usecase.RunMediaPipeUseCase
import com.mobiledevpro.chat.domain.usecase.SendMessageUseCase
import com.mobiledevpro.chat.ui.state.ChatUIState
import com.mobiledevpro.ui.vm.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


@OptIn(FlowPreview::class)
class ChatViewModel(
    private val getChatMessagesUseCase: GetChatMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val clearHistoryUseCase: ClearHistoryUseCase,
    private val runAiUseCase: RunMediaPipeUseCase
) : BaseViewModel<ChatUIState>() {

    private val _newMessageFlow = MutableStateFlow<ChatMessage?>(null)
    private val newMessageFlow = _newMessageFlow.asStateFlow()

    private val _errorFlow = MutableStateFlow<Throwable?>(null)
    private val errorFlow = _errorFlow.asStateFlow()

    override val initialState: ChatUIState
        get() = ChatUIState.Loading("")

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    override fun observeState(): Flow<ChatUIState> {

        val chatMessagesFlow = getChatMessagesUseCase.execute()

        val aiStateFlow = runAiUseCase.execute()

        return combine(
            aiStateFlow,
            chatMessagesFlow,
            newMessageFlow,
            errorFlow
        ) { aiStateResult: Result<AIState>,
            chatMessagesResult: Result<List<ChatMessage>>,
            newMessage: ChatMessage?,
            error: Throwable? ->

            var errorMessage =
                error?.let { err -> "${err.localizedMessage ?: "Unknown error"}\n" } ?: ""

            val chatMessages = try {
                chatMessagesResult.getOrThrow()
            } catch (t: Throwable) {
                errorMessage += "${t.localizedMessage ?: "Unknown error"}\n"
                emptyList<ChatMessage>()
            }

            val aiState = try {
                aiStateResult.getOrThrow()
            } catch (t: Throwable) {
                errorMessage += "${t.localizedMessage ?: "Unknown error"}\n"
                AIState.ERROR
            }

            ChatUIState.Success(
                messageCollection = ChatMessageCollection(
                    list = chatMessages + listOfNotNull(newMessage)
                ),
                aiState = aiState,
                warningMessage = errorMessage,
                availableSkills = getAvailableSkills()
            )

        }.flowOn(Dispatchers.IO)
    }

    fun sendMessage(text: String) {
        _errorFlow.update { null }

        val newMessage =
            newMessageFlow.value?.copy(text = text, notSent = true, isUserMessage = true)
                ?: ChatMessage(
                    timeCreated = System.currentTimeMillis(),
                    text = text,
                    isUserMessage = true,
                    notSent = true
                )

        viewModelScope.launch {
            sendMessageUseCase.execute(newMessage)
                .onFailure { t: Throwable ->
                    _errorFlow.update { t }
                }
                .onSuccess {
                    _newMessageFlow.update { null }
                }
        }
    }

    fun onClearHistory() {
        _errorFlow.update { null }
        _newMessageFlow.update { null }

        viewModelScope.launch {
            runAiUseCase.stop()
            clearHistoryUseCase.execute()
        }
    }

    private fun getAvailableSkills(): List<Skill> =
        SkillType.entries.map { skillType ->
            Skill(
                type = skillType,
                name = skillType.toName(),
                path = skillType.toLocalPath()
            )
        }
}
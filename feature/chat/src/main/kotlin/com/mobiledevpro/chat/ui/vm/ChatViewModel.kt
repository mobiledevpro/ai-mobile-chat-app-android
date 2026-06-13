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

import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.mobiledevpro.chat.domain.model.AIState
import com.mobiledevpro.chat.domain.model.AgentSkill
import com.mobiledevpro.chat.domain.model.ChatMessage
import com.mobiledevpro.chat.domain.model.ChatMessageCollection
import com.mobiledevpro.chat.domain.model.PredefinedPrompt
import com.mobiledevpro.chat.domain.usecase.ClearHistoryUseCase
import com.mobiledevpro.chat.domain.usecase.GetChatMessagesUseCase
import com.mobiledevpro.chat.domain.usecase.RunMediaPipeUseCase
import com.mobiledevpro.chat.domain.usecase.SendMessageUseCase
import com.mobiledevpro.chat.ui.state.ChatUIState
import com.mobiledevpro.chat.util.SkillLoader
import com.mobiledevpro.download.DownloadManagerHelper
import com.mobiledevpro.download.DownloadManagerHelper.downloadWorkRequest
import com.mobiledevpro.download.worker.DownloadWorker
import com.mobiledevpro.filestore.FileStoreManager
import com.mobiledevpro.filestore.FileType
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
    private val runAiUseCase: RunMediaPipeUseCase,
    private val workManager: WorkManager,
    private val fileStoreManager: FileStoreManager,
    private val skillLoader: SkillLoader
) : BaseViewModel<ChatUIState>() {

    private val _newMessageFlow = MutableStateFlow<ChatMessage?>(null)
    private val newMessageFlow = _newMessageFlow.asStateFlow()

    private val _errorFlow = MutableStateFlow<Throwable?>(null)
    private val errorFlow = _errorFlow.asStateFlow()

    private val _skillsFlow = MutableStateFlow<List<AgentSkill>>(emptyList())
    private val skillsFlow = _skillsFlow.asStateFlow()

    private val downloadWorkInfoFlow: Flow<WorkInfo?> = workManager
        .getWorkInfoByIdFlow(downloadWorkRequest.id)

    init {
        checkLLMFile()
        loadSkills()
    }

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
            errorFlow,
            downloadWorkInfoFlow
        ) { aiStateResult: Result<AIState>,
            chatMessagesResult: Result<List<ChatMessage>>,
            newMessage: ChatMessage?,
            error: Throwable?,
            downloadWorkerInfo ->

            if (downloadWorkerInfo?.state == WorkInfo.State.RUNNING || downloadWorkerInfo?.state == WorkInfo.State.ENQUEUED) {
                val progress = downloadWorkerInfo.progress.getInt(DownloadWorker.KEY_PROGRESS, 0)
                return@combine ChatUIState.Loading("Downloading LLM. Please wait... ${progress}%")
            }

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
                predefinedPrompts = getPredefinedPromptList()
            )

        }.combine(skillsFlow) { chatUIState, skills ->
            if (chatUIState is ChatUIState.Success) {
                chatUIState.copy(availableSkills = skills)
            } else {
                chatUIState
            }
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

    private fun checkLLMFile() {
        viewModelScope.launch(Dispatchers.IO) {
            val isExists = fileStoreManager.isFileExists(
                DownloadManagerHelper.llmLocalFileName,
                FileType.TEMP_AI_MODEL
            )

            Log.d(
                this@ChatViewModel.javaClass.name,
                "checkLLMFile: ${DownloadManagerHelper.llmLocalFileName} exists: $isExists"
            )
            if (!isExists)
                DownloadManagerHelper.startDownloading(workManager)
        }
    }

    private fun loadSkills() {
        viewModelScope.launch(Dispatchers.IO) {
            skillLoader.loadSkillsFromAssets().also { skills ->
                _skillsFlow.value = skills

                skills.forEach { skill ->
                    Log.d(
                        this@ChatViewModel.javaClass.name,
                        "getAvailableSkills: SKILL : ${skill.name}"
                    )
                }
            }
        }
    }


    private fun getPredefinedPromptList(): List<PredefinedPrompt> =
        listOf(
            PredefinedPrompt("Tell me a joke"),
            PredefinedPrompt("Tell me a historical fact"),
            PredefinedPrompt("Search for BTC price"),
            PredefinedPrompt("View my schedule today")
        )
}
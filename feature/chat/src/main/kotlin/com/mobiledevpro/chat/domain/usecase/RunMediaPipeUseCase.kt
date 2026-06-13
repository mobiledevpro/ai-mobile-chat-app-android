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
package com.mobiledevpro.chat.domain.usecase

import android.content.Context
import android.util.Log
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.ai.edge.localagents.core.proto.Content
import com.google.ai.edge.localagents.core.proto.FunctionResponse
import com.google.ai.edge.localagents.core.proto.GenerateContentResponse
import com.google.ai.edge.localagents.core.proto.Part
import com.google.ai.edge.localagents.core.proto.Tool
import com.google.ai.edge.localagents.fc.ChatSession
import com.google.ai.edge.localagents.fc.GemmaFormatter
import com.google.ai.edge.localagents.fc.GenerativeModel
import com.google.ai.edge.localagents.fc.LlmInferenceBackend
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.protobuf.Struct
import com.google.protobuf.Value
import com.mobiledevpro.chat.domain.model.AIState
import com.mobiledevpro.chat.domain.model.AgentSkill
import com.mobiledevpro.chat.domain.model.PromptWrapper
import com.mobiledevpro.chat.ext.functionCalls
import com.mobiledevpro.chat.ext.getArgument
import com.mobiledevpro.chat.ext.hasFunctionCalls
import com.mobiledevpro.chat.ext.text
import com.mobiledevpro.chat.util.SkillLoader
import com.mobiledevpro.coroutines.BaseCoroutinesFLowUseCase
import com.mobiledevpro.coroutines.None
import com.mobiledevpro.database.AppDatabase
import com.mobiledevpro.database.entity.ChatMessageEntity
import com.mobiledevpro.download.DownloadManagerHelper
import com.mobiledevpro.download.DownloadManagerHelper.downloadWorkRequest
import com.mobiledevpro.filestore.FileStoreManager
import com.mobiledevpro.filestore.FileType
import com.mobiledevpro.tooling.ToolSnippet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject

/**
 * Use case to init AI, download model and handle user's messages
 *
 * Documentation to Media Pipe: https://ai.google.dev/edge/mediapipe/solutions/genai/llm_inference/android
 *
 */

private const val MAX_INPUT_TOKENS = 4000
private const val CHAR_COUNT_PER_TOKEN = 5 // 1 token is approximately equal to 5 characters

class RunMediaPipeUseCase(
    database: AppDatabase,
    private val context: Context,
    private val fileStoreManager: FileStoreManager,
    private val skillLoader: SkillLoader,
    workManager: WorkManager,
) : BaseCoroutinesFLowUseCase<None, AIState>(Dispatchers.IO) {

    private var llmInference: LlmInference? = null
    private var chatSession: ChatSession? = null

    private val dao = database.chatMessageDao()
    private var jobGenerateCompletion: Deferred<Unit>? = null
    private var jobMessageUpdate: Job? = null

    private val channelMessageForUpdate = Channel<ChatMessageEntity>(Channel.CONFLATED)
    private var availableSkills: List<AgentSkill> = emptyList()

    private val downloadWorkInfoFlow: Flow<WorkInfo?> = workManager
        .getWorkInfoByIdFlow(downloadWorkRequest.id)

    init {
        availableSkills = loadAvailableSkills()
    }


    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun buildUseCaseFlow(params: None?): Flow<AIState> =
        flow {
            val isExists = fileStoreManager.isFileExists(
                DownloadManagerHelper.llmLocalFileName,
                FileType.TEMP_AI_MODEL
            )
            if (isExists) {
                emit(WorkInfo.State.SUCCEEDED)
            } else {
                emitAll(downloadWorkInfoFlow.map { it?.state ?: WorkInfo.State.ENQUEUED })
            }
        }
            .distinctUntilChanged()
            .flatMapLatest { state ->
                Log.d(this@RunMediaPipeUseCase.javaClass.name, "buildUseCaseFlow: $state")
                when (state) {
                    WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING -> flowOf(AIState.DOWNLOADING_MODEL)
                    WorkInfo.State.SUCCEEDED -> initAi()
                    WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> flowOf(AIState.ERROR)
                    else -> {
                        // Check if file exists anyway
                        if (fileStoreManager.isFileExists(
                                DownloadManagerHelper.llmLocalFileName,
                                FileType.TEMP_AI_MODEL
                            )
                        )
                            initAi()
                        else
                            flowOf(AIState.IDLE)
                    }
                }
            }
            .flatMapLatest { aIState: AIState ->
                if (aIState == AIState.READY) {
                    getChatMessages()
                        .distinctUntilChanged()
                        .flatMapConcat { chatMessages ->
                            Log.d(
                                "Agent",
                                "buildUseCaseFlow: observing ${chatMessages.size} messages"
                            )

                            val unAnsweredUserMessages =
                                chatMessages.filter { !it.answered && it.isUserMessage }

                            if (unAnsweredUserMessages.isEmpty()) {
                                flowOf(AIState.READY)
                            } else {
                                handleChatMessages(chatMessages = chatMessages)
                            }
                        }
                } else {
                    flowOf(aIState)
                }
            }

    fun stop() {
        jobGenerateCompletion?.cancel()
        jobMessageUpdate?.cancel()
        chatSession?.close()
        chatSession = null
    }

    private fun getChatMessages(): Flow<List<ChatMessageEntity>> =
        dao.selectAllMessages()

    /**
     * Initialize LLM
     *
     */
    private fun initAi(): Flow<AIState> = callbackFlow {
        trySend(AIState.INITIALIZING)

        // Copy model from assets to in-app storage
        val modelFile =
            fileStoreManager.getFile(
                fileName = DownloadManagerHelper.llmLocalFileName,
                fileType = FileType.TEMP_AI_MODEL
            )

        if (modelFile == null) {
            throw RuntimeException("LLM file not found on the device")
        }

        Log.d(
            this@RunMediaPipeUseCase.javaClass.name,
            "initAi: modelFile = ${modelFile.absolutePath}"
        )

        val options = LlmInference.LlmInferenceOptions.builder()
            .setModelPath(modelFile.absolutePath)
            .setMaxTokens(MAX_INPUT_TOKENS)
            .build()

        llmInference = LlmInference.createFromOptions(context, options)

        createChatSession()

        trySend(AIState.READY)

        awaitClose {
            llmInference?.close()
            llmInference = null
            chatSession?.close()
            chatSession = null
        }
    }

    private fun createChatSession() {
        chatSession?.close()

        val llmInferenceBackend = LlmInferenceBackend(llmInference, GemmaFormatter())

        val toolBuilder = Tool.newBuilder()
        availableSkills.forEach { skill ->
            toolBuilder.addFunctionDeclarations(skill.declaration)
        }
        val registeredTools = toolBuilder.build()

        // Author the foundational system instructions
        val systemInstruction = Content.newBuilder()
            .setRole("system")
            .addParts(
                Part.newBuilder()
                    .setText(PromptWrapper.createSystemPrompt())
            )
            .build()

        // Instantiate the GenerativeModel structure directly (This replaces the broken builder)
        val generativeModel = GenerativeModel(
            llmInferenceBackend,
            systemInstruction,
            listOf(registeredTools)
        )

        // Generate the tracking ChatSession instance from the model parent
        chatSession = generativeModel.startChat()
    }

    /**
     * CactusLM expects a list of all user's messages for the context. Not sure about the answers,
     * so it's not include into the messages list
     */
    @OptIn(FlowPreview::class)
    private fun handleChatMessages(chatMessages: List<ChatMessageEntity>): Flow<AIState> =
        flow {
            emit(AIState.THINKING)

            // Handle the last user's message
            val lastUserMessage: ChatMessageEntity =
                chatMessages.last { !it.answered && it.isUserMessage }

            val answerCreatedAt = System.currentTimeMillis()

            // It's important to mark the last user's message as answered before inserting or updating the table
            // Otherwise handleMessages() will be called in a loop
            markUserMessageAnswered(lastUserMessage.timeCreated)

            coroutineScope {
                jobMessageUpdate = jobHandleAIMessageUpdate(answerCreatedAt)

                // Insert empty message first to update the text as tokens are received
                val aiAnswerMessage = insertAnswer(
                    createdAt = answerCreatedAt,
                    text = ""
                )

                val userText = chatMessages.last().text

                emit(AIState.THINKING)

                val answerTextBuilder = StringBuilder()

                if (chatSession == null)
                    createChatSession()

                processAgentTurn(
                    chatSession!!,
                    userText,
                    aiAnswerMessage,
                    answerTextBuilder
                )

                delay(1000)

                jobMessageUpdate?.cancel()
                emit(AIState.COMPLETED)
            }

        }

    private suspend fun insertAnswer(createdAt: Long, text: String): ChatMessageEntity =
        ChatMessageEntity(
            timeCreated = createdAt,
            text = text,
            isUserMessage = false,
            notSent = false,
            answered = false
        ).also { messageEntity ->
            dao.insert(messageEntity)
        }

    private suspend fun markUserMessageAnswered(createdAt: Long) {
        dao.updateAsAnswered(createdAt)
    }

    private suspend fun updateAnswerText(createdAt: Long, text: String) {
        dao.updateText(createdAt, text)
    }

    @OptIn(FlowPreview::class)
    private fun CoroutineScope.jobHandleAIMessageUpdate(answerCreatedAt: Long): Job =
        launch {
            for (msg in channelMessageForUpdate) {
                if (!isActive) return@launch

                if (msg.timeCreated == answerCreatedAt)
                    updateAnswerText(createdAt = answerCreatedAt, text = msg.text)

                delay(100)
            }
        }

    private fun loadAvailableSkills(): List<AgentSkill> =
        skillLoader.loadSkillsFromAssets()

    private suspend fun processAgentTurn(
        session: ChatSession,
        userInput: String,
        aiAnswerMessage: ChatMessageEntity,
        answerTextBuilder: StringBuilder
    ) {
        // 1. Send the initial user message
        var response: GenerateContentResponse = session.sendMessage(userInput)

        Log.i("Agent", "processAgentTurn: response has functions: ${response.hasFunctionCalls()}")
        Log.i("Agent", "processAgentTurn: response: $response")

        // 2. Multi-step execution loop
        //  while (true) {
        val parts = mutableListOf<Part>()

        // Collect formal calls
        val formalCalls = response.functionCalls()

        // Collect manual calls if no formal calls are present
        val manualCalls = if (formalCalls.isEmpty()) {
            extractManualToolCalls(response.text())
        } else emptyList()

        Log.i("Agent", "manualCalls: $manualCalls")

        if (formalCalls.isNotEmpty() || manualCalls.isNotEmpty()) {
            var resultText = ""
            // Process formal calls
            for (call in formalCalls) {
                Log.i("Agent", "Executing formal tool: ${call.name}")
                resultText = executeTool(call.name, call.getArgument("query"))
                // parts.add(createFunctionResponsePart(call.name, resultText))
            }

            // Process manual calls
            for (call in manualCalls) {
                Log.i("Agent", "Executing manual tool: ${call.first}")
                resultText = executeTool(call.first, call.second)
                Log.d("Agent", "processAgentTurn: manualCalls resultText: $resultText")
                // parts.add(createFunctionResponsePart(call.first, resultText))
            }

            /* val toolResponseContent = Content.newBuilder()
                 .setRole("function")
                 .addAllParts(parts)
                 .build()*/

            // Submit results back to the LLM
            response = session.sendMessage(resultText)
        }
        // }

        // 3. Final output
        val finalResult = response.text()
        Log.d("Agent", "processAgentTurn: final resp: $response")

        synchronized(answerTextBuilder) {
            answerTextBuilder.append(finalResult)
            channelMessageForUpdate.trySend(aiAnswerMessage.copy(text = answerTextBuilder.toString()))
        }
    }

    private fun extractManualToolCalls(text: String): List<Pair<String, String>> {
        val jsonRegex = Regex("```json\\s*(\\{.*?\\})\\s*```", RegexOption.DOT_MATCHES_ALL)
        return jsonRegex.findAll(text).mapNotNull { match ->
            try {
                val json = JSONObject(match.groupValues[1])
                val name = json.optString("name")
                val params = json.optJSONObject("parameters")
                val query = params?.optString("query") ?: ""
                if (name.isNotEmpty()) name to query else null
            } catch (e: Exception) {
                null
            }
        }.toList()
    }

    private suspend fun executeTool(name: String, query: String): String {
        return when (name) {
            "web_search" -> {
                ToolSnippet.fetchSearchSnippet(query)
            }

            "view_calendar" -> "Sorry View Calendar hasn't been implemented yet."
            else -> "Error: Tool not found"
        }
    }

    private fun createFunctionResponsePart(name: String, resultText: String): Part {
        val functionResponse = FunctionResponse.newBuilder()
            .setName(name)
            .setResponse(
                Struct.newBuilder()
                    .putFields(
                        "content",
                        Value.newBuilder().setStringValue(resultText).build()
                    )
                    .build()
            )
            .build()
        return Part.newBuilder().setFunctionResponse(functionResponse).build()
    }
}
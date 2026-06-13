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
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import com.google.mediapipe.tasks.genai.llminference.LlmInferenceSession
import com.mobiledevpro.chat.domain.model.AIState
import com.mobiledevpro.chat.domain.model.PromptWrapper
import com.mobiledevpro.chat.domain.model.Skill
import com.mobiledevpro.chat.domain.model.SkillType
import com.mobiledevpro.chat.domain.model.toLocalPath
import com.mobiledevpro.chat.domain.model.toName
import com.mobiledevpro.chat.mapper.toDomain
import com.mobiledevpro.chat.util.SkillParser
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
import kotlinx.coroutines.async
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
    private val skillParser: SkillParser,
    private val workManager: WorkManager
) : BaseCoroutinesFLowUseCase<None, AIState>(Dispatchers.IO) {

    private var llmInference: LlmInference? = null

    private val dao = database.chatMessageDao()
    private var jobGenerateCompletion: Deferred<Unit>? = null
    private var jobMessageUpdate: Job? = null

    private val channelMessageForUpdate = Channel<ChatMessageEntity>(Channel.CONFLATED)
    private var skillsText: String? = null

    private val downloadWorkInfoFlow: Flow<WorkInfo?> = workManager
        .getWorkInfoByIdFlow(downloadWorkRequest.id)

    init {
        skillsText = loadAvailableSkills()
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
                if (aIState == AIState.READY)
                    getChatMessages()
                        .distinctUntilChanged()
                        .flatMapConcat { chatMessages: List<ChatMessageEntity> ->

                            val unAnsweredUserMessages =
                                chatMessages.filter { !it.answered && it.isUserMessage }

                            if (unAnsweredUserMessages.isEmpty())
                                flowOf(AIState.COMPLETED)
                            else
                                handleChatMessages(chatMessages = chatMessages)

                        }
                else
                    flowOf(aIState)
            }

    fun stop() {
        jobGenerateCompletion?.cancel()
        jobMessageUpdate?.cancel()
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
            // .setPreferredBackend(LlmInference.Backend.GPU)
            .build()

        llmInference = LlmInference.createFromOptions(context, options)

        trySend(AIState.READY)

        awaitClose {
            llmInference?.close()
            llmInference = null
        }
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

                val finalPrompt: String = PromptWrapper.createPrompt(
                    skillsTxt = null,//skillsText,
                    userMessage = userText,
                    history = chatMessages.toDomain()
                )

                emit(AIState.THINKING)

                val answerTextBuilder = StringBuilder()

                Log.d(
                    this@RunMediaPipeUseCase.javaClass.name,
                    "handleUserMessages: prompt : \n$finalPrompt"
                )

                // Moved generation and completion handling to a separate function
                handlePrompt(finalPrompt, aiAnswerMessage, answerTextBuilder) {
                    this@flow.emit(AIState.COMPLETED)
                }

                // Handle SKILL triggers from LLM response
                //  findAndHandleTriggers(answerTextBuilder.toString())
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

    private fun generateResponseFlow(prompt: String): Flow<String> = callbackFlow {
        val llm = llmInference ?: run {
            close()
            return@callbackFlow
        }

        // 1. Create session with deterministic options
        // The default values (Temp: 0.8, TopK: 40) are too "creative" for strict tool triggering
        val sessionOptions = LlmInferenceSession.LlmInferenceSessionOptions.builder()
            .setTemperature(0.1f) // CRITICAL: Forces adherence to prompt rules
            .setTopK(1)
            .build()

        // 2. Create the session
        val session = LlmInferenceSession.createFromOptions(llm, sessionOptions)

        // 3. Add the prompt
        session.addQueryChunk(prompt)

        // 4. Generate the response
        session.generateResponseAsync { partialResult, done ->
            Log.d(
                this@RunMediaPipeUseCase.javaClass.name,
                "generateResponseFlow: partialResult - $partialResult | done = $done"
            )


            if (partialResult.isNotEmpty())
                trySend(partialResult)

            if (done) {
                // DO NOT call session.close() here as it can cause JNI errors 
                // while the callback is still active. 
                // close() below will trigger the cleanup in awaitClose.
                close()
            }
        }

        awaitClose {
            try {
                session.close()
            } catch (e: Exception) {
                // Ignore if already closed
            }
        }
    }

    private suspend fun handlePrompt(
        finalPrompt: String,
        aiAnswerMessage: ChatMessageEntity,
        answerTextBuilder: StringBuilder,
        onCompleted: suspend () -> Unit
    ) {
        coroutineScope {
            jobGenerateCompletion = async {
                generateResponseFlow(finalPrompt)
                    .collect { result ->
                        synchronized(answerTextBuilder) {
                            answerTextBuilder.append(result)
                            channelMessageForUpdate.trySend(
                                aiAnswerMessage.copy(
                                    text = answerTextBuilder.toString()
                                )
                            )
                        }
                    }
            }

            try {
                jobGenerateCompletion?.await()
            } finally {
                Log.d(
                    this@RunMediaPipeUseCase.javaClass.name,
                    "handleUserMessages: ${answerTextBuilder.toString()}"
                )
                jobMessageUpdate?.cancel()
                onCompleted()
            }
        }
    }

    private fun loadAvailableSkills(): String? =
        if (SkillType.entries.isEmpty())
            null
        else
            SkillType.entries.map { skillType ->
                Skill(
                    type = skillType,
                    name = skillType.toName(),
                    path = skillType.toLocalPath()
                )
            }.joinToString(separator = "\n\n") { skill ->
                skillParser.loadSkill(skill)
            }

    private suspend fun findAndHandleTriggers(answer: String) {
        Log.i(this@RunMediaPipeUseCase.javaClass.name, "findAndHandleTriggers: $answer")
        // Implementation for finding and handling triggers
        when {
            answer.contains("TRIGGER_WEB_SEARCH", ignoreCase = true) -> {
                val searchRegex = Regex("""TRIGGER_WEB_SEARCH\("([^"]+)"\)""")
                val searchMatch = searchRegex.find(answer)
                if (searchMatch != null) {
                    val query = searchMatch.groupValues[1]
                    ToolSnippet.fetchSearchSnippet(query).also { searchResult ->

                        val followUpPrompt = """
                The user asked a question requiring real-time data. 
                Web search results for '$query':
                ---
                $searchResult
                ---
                Using this information, provide a short, conversational response to the user.
                """.trimIndent()

                        Log.i(
                            this@RunMediaPipeUseCase.javaClass.name,
                            "findAndHandleTriggers: followUpPrompt = $followUpPrompt"
                        )

                        //TODO: complete handling the prompt here
                        /* handlePrompt(

                          )
  */
                    }
                }
            }
        }
    }

}
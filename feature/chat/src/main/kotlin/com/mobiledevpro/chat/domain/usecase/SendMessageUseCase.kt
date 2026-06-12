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

import android.net.Uri
import com.mobiledevpro.chat.domain.model.ChatMessage
import com.mobiledevpro.chat.mapper.toData
import com.mobiledevpro.coroutines.BaseCoroutinesUseCase
import com.mobiledevpro.coroutines.None
import com.mobiledevpro.database.AppDatabase
import com.mobiledevpro.database.entity.ChatMessageEntity
import kotlinx.coroutines.Dispatchers

/**
 * Use case to send a new message to chat
 *
 * Created on Dec 04, 2025.
 *
 */
class SendMessageUseCase(
    private val database: AppDatabase
) : BaseCoroutinesUseCase<ChatMessage, None>(Dispatchers.IO) {

    override suspend fun buildUseCase(params: ChatMessage?): None =
        params?.let { message  : ChatMessage ->
            message.toData()
                .copy(notSent = false)
                .also { entry: ChatMessageEntity ->
                    database.chatMessageDao().insert(entry)
                }

            None()
        } ?: throw IllegalArgumentException("New message cannot be null")


}
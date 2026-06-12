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

import com.mobiledevpro.chat.domain.model.ChatMessage
import com.mobiledevpro.chat.mapper.toDomain
import com.mobiledevpro.coroutines.BaseCoroutinesFLowUseCase
import com.mobiledevpro.coroutines.None
import com.mobiledevpro.database.AppDatabase
import com.mobiledevpro.database.entity.ChatMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Use case to get chat messages from local database
 *
 * Created on Dec 04, 2025.
 *
 */
class GetChatMessagesUseCase(
    private val database: AppDatabase
) : BaseCoroutinesFLowUseCase<None, List<ChatMessage>>(Dispatchers.IO) {

    override fun buildUseCaseFlow(params: None?): Flow<List<ChatMessage>> =
        database.chatMessageDao()
            .selectAll()
            .map(List<ChatMessageEntity>::toDomain)
}
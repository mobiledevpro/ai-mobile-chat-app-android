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
package com.mobiledevpro.chat.mapper

import androidx.core.net.toUri
import com.mobiledevpro.chat.domain.model.ChatMessage
import com.mobiledevpro.chat.ext.beautifyText
import com.mobiledevpro.database.entity.ChatMessageEntity


fun List<ChatMessageEntity>.toDomain(): List<ChatMessage> =
    this.mapTo(ArrayList<ChatMessage>(), ChatMessageEntity::toDomain)

fun List<ChatMessage>.toData(): List<ChatMessageEntity> =
    this.mapTo(ArrayList<ChatMessageEntity>(), ChatMessage::toData)



fun ChatMessageEntity.toDomain(): ChatMessage =
    ChatMessage(
        timeCreated = timeCreated,
        text = text.beautifyText(),
        isUserMessage = isUserMessage,
        notSent = notSent
    )


fun ChatMessage.toData() : ChatMessageEntity =
    ChatMessageEntity(
        timeCreated = timeCreated,
        text = text,
        isUserMessage = isUserMessage,
        notSent = notSent,
        answered = false
    )
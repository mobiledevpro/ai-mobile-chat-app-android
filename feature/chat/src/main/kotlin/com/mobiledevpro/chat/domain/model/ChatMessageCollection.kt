package com.mobiledevpro.chat.domain.model

import androidx.compose.runtime.Immutable

/**
 *
 * Created on Dec 03, 2025.
 *
 */
@Immutable
data class ChatMessageCollection(
    val list : List<ChatMessage> = emptyList()
) {
    fun size() = list.filter { !it.notSent }.size

    fun getLastNotSentMessage(): ChatMessage? = list.lastOrNull { it.notSent }

    fun isEmpty() = list.isEmpty()
}

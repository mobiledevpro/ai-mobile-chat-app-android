package com.mobiledevpro.chat.domain.model

import android.net.Uri

/**
 *
 * Created on Dec 03, 2025.
 *
 */
data class ChatMessage(
    val timeCreated: Long,
    val text: String,
    val isUserMessage: Boolean,
    val notSent : Boolean = false
) {
    fun listKey(): String =
        timeCreated.toString()
}

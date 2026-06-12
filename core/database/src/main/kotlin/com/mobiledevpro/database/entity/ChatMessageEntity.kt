package com.mobiledevpro.database.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "chat_message",
    indices = [
        Index(value = ["timeCreated"])
    ],
    primaryKeys = ["timeCreated"]
)
data class ChatMessageEntity(
    val timeCreated: Long,
    val text: String,
    val isUserMessage: Boolean,
    val notSent: Boolean,
    val answered: Boolean
)



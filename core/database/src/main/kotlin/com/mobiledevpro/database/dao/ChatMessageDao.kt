package com.mobiledevpro.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.mobiledevpro.database.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao : BaseDao<ChatMessageEntity> {
    @Query("SELECT * FROM chat_message WHERE text != '' ORDER by timeCreated ASC")
    fun selectAll(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_message WHERE isUserMessage = 1 ORDER by timeCreated ASC")
    fun selectUserMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_message ORDER by timeCreated ASC")
    fun selectAllMessages(): Flow<List<ChatMessageEntity>>

    @Query("DELETE FROM chat_message")
    fun deleteAll()

    @Query("UPDATE chat_message SET text = :text WHERE timeCreated = :timeCreated")
    fun updateText(timeCreated: Long, text: String)

    @Query("UPDATE chat_message SET answered = 1 WHERE timeCreated = :timeCreated")
    fun updateAsAnswered(timeCreated: Long)
}
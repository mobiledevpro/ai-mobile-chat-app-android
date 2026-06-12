package com.mobiledevpro.chat.ui.state

import com.mobiledevpro.chat.domain.model.AIState
import com.mobiledevpro.chat.domain.model.ChatMessageCollection
import com.mobiledevpro.chat.domain.model.Skill
import com.mobiledevpro.ui.state.UIState

sealed interface ChatUIState : UIState {

    data class Loading(val message: String) : ChatUIState

    data class Success(
        val messageCollection : ChatMessageCollection = ChatMessageCollection(),
        val aiState: AIState = AIState.IDLE,
        val warningMessage: String = "",
        val availableSkills: List<Skill> = emptyList()
    ) : ChatUIState
}
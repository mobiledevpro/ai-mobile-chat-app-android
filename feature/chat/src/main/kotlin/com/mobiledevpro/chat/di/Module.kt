package com.mobiledevpro.chat.di

import com.mobiledevpro.chat.domain.usecase.ClearHistoryUseCase
import com.mobiledevpro.chat.domain.usecase.GetChatMessagesUseCase
import com.mobiledevpro.chat.domain.usecase.RunMediaPipeUseCase
import com.mobiledevpro.chat.domain.usecase.SendMessageUseCase
import com.mobiledevpro.chat.ui.vm.ChatViewModel
import com.mobiledevpro.chat.util.SkillParser
import org.koin.core.module.dsl.scopedOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module


val featureChatModule = module {
    scope<ChatViewModel> {
        viewModelOf(::ChatViewModel)
        scopedOf(::SkillParser)
        scopedOf(::GetChatMessagesUseCase)
        scopedOf(::SendMessageUseCase)
        scopedOf(::ClearHistoryUseCase)
        scopedOf(::RunMediaPipeUseCase)
    }
}

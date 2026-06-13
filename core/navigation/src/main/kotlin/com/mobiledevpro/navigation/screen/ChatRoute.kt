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
package com.mobiledevpro.navigation.screen

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mobiledevpro.chat.di.featureChatModule
import com.mobiledevpro.chat.ui.ChatScreen
import com.mobiledevpro.chat.ui.vm.ChatViewModel
import com.mobiledevpro.di.rememberNavViewModel
import com.mobiledevpro.download.di.featureDownload
import com.mobiledevpro.navigation.Screen

fun NavGraphBuilder.chatRoute(onNavigateTo: (Screen) -> Unit) {
    composable(
        route = Screen.Chat.route
    ) {

        val viewModel = rememberNavViewModel<ChatViewModel>(
            modules = {
                listOf(
                    featureChatModule,
                    featureDownload
                )
            }
        )

        ChatScreen(
            listUiState = viewModel.uiState,
            onSettings = {
                Screen.Settings.also(onNavigateTo)
            },
            onClearHistory = viewModel::onClearHistory,
            onSendPrompt = viewModel::sendMessage
        )

    }
}
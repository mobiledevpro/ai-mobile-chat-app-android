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

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.mobiledevpro.di.rememberNavViewModel
import com.mobiledevpro.navigation.Screen
import com.mobiledevpro.settings.di.featureSettingsModule
import com.mobiledevpro.settings.ui.SettingsScreen
import com.mobiledevpro.settings.ui.vm.SettingsViewModel


fun NavGraphBuilder.settingsRoute(onNavigateToRoot: (Screen) -> Unit) {
    composable(
        route = Screen.Settings.route,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(400)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(400)
            )
        }
    ) {
        val viewModel = rememberNavViewModel<SettingsViewModel>(
            modules = {
                listOf(featureSettingsModule)
            }
        )

        SettingsScreen(
            uiState = viewModel.uiState,
            onBack = {
                Screen.Back().also(onNavigateToRoot)
            }
        )
    }

}

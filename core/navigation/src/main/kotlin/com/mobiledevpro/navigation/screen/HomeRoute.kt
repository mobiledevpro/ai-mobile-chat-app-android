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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.mobiledevpro.di.rememberNavViewModel
import com.mobiledevpro.home.di.featureHomeModule
import com.mobiledevpro.home.ui.HomeScreen
import com.mobiledevpro.home.ui.HomeViewModel
import com.mobiledevpro.navigation.Screen
import com.mobiledevpro.navigation.graph.HomeNavGraph


fun NavGraphBuilder.homeRoute(onNavigateToRoot: (Screen) -> Unit) {
    composable(
        route = Screen.Home.route
    ) {

        val lifecycleOwner = LocalLifecycleOwner.current
        val homeNavController = rememberNavController()

        val viewModel = rememberNavViewModel<HomeViewModel>(
            modules = {
                listOf(featureHomeModule)
            }
        )

        DisposableEffect(Unit) {
            lifecycleOwner.lifecycle.addObserver(viewModel)
            onDispose {
                lifecycleOwner.lifecycle.removeObserver(viewModel)
            }
        }

        val nestedNavGraph: @Composable () -> Unit = {
            HomeNavGraph(
                homeNavController = homeNavController,
                onNavigateToRoot = onNavigateToRoot
            )
        }

        HomeScreen(
            nestedNavGraph = nestedNavGraph
        )
    }

}

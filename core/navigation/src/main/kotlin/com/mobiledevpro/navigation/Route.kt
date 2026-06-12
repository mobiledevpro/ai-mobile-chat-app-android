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
package com.mobiledevpro.navigation

import android.os.Bundle

const val KEY_SCREEN_BUNDLE_ARGS = "bundle"

const val navigationRouteBack = "back"
const val navigationRouteHome = "home"
const val navigationRouteChat = "chat"
const val navigationRouteSettings = "setting"

sealed class Screen(
    val route: String,
    var routePath: String? = null,
    var popUpToScreen: Screen? = null,
    var clearBackStack: Boolean = false,
    val restoreState: Boolean = true,
    var bundleArgs: Bundle? = null
) {

    fun withClearBackStack() = apply { clearBackStack = true }

    fun routeWith(path: String) = apply {
        routePath = path
    }

    fun popUpTo(screen: Screen) = apply {
        popUpToScreen = screen
    }

    data class Back(val args: Bundle? = null) : Screen(navigationRouteBack, bundleArgs = args)
    object Home : Screen(navigationRouteHome)
    object Chat : Screen(navigationRouteChat)
    object Settings : Screen(navigationRouteSettings)

}
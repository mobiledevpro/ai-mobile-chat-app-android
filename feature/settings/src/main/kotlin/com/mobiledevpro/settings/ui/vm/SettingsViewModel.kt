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
package com.mobiledevpro.settings.ui.vm

import com.mobiledevpro.settings.BuildConfig
import com.mobiledevpro.settings.ui.state.SettingsUIState
import com.mobiledevpro.ui.vm.BaseViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class SettingsViewModel(
) : BaseViewModel<SettingsUIState>() {

    private val _appVersion =
        MutableStateFlow("${BuildConfig.VERSION_NAME} [Build ${BuildConfig.VERSION_CODE}]")
    private val appVersion: StateFlow<String> = _appVersion.asStateFlow()

    override val initialState: SettingsUIState
        get() = SettingsUIState.Empty

    override fun observeState(): Flow<SettingsUIState> {


        return appVersion.map { version ->
            SettingsUIState.Success(
                appVersion = version
            )
        }.flowOn(Dispatchers.IO)
    }
}
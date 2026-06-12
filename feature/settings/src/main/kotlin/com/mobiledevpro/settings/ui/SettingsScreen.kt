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
package com.mobiledevpro.settings.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mobiledevpro.settings.ui.state.SettingsUIState
import com.mobiledevpro.ui.component.AppBar
import com.mobiledevpro.ui.theme.AppTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.mobiledevpro.ui.R as RUI


@Composable
fun SettingsScreen(
    uiState: StateFlow<SettingsUIState>,
    onBack: () -> Unit
) {
    val state by uiState.collectAsStateWithLifecycle()

    var appVersion by remember { mutableStateOf<String?>(null) }

    if (state is SettingsUIState.Success) {
        val successState = state as SettingsUIState.Success
        appVersion = successState.appVersion
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        AppBar(
            title = stringResource(RUI.string.label_settings),
            navIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(id = RUI.drawable.ic_arrow_back_white_24dp),
                        contentDescription = ""
                    )
                }
            }
        )

        Box(modifier = Modifier.weight(1f, fill = true)) {
            //Settings content here
        }

        Footer(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 16.dp),
            appVersion = appVersion
        )
    }

}


@Composable
private fun Footer(
    modifier: Modifier = Modifier,
    appVersion: String?
) {
    Box(modifier = modifier) {


        appVersion?.also { version ->
            Text(
                text = stringResource(id = RUI.string.label_version, version),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .alpha(0.5f)
                    .align(Alignment.Center)
                    .padding(8.dp)
            )
        }
    }
}

@Composable
@Preview
fun SettingsScreenPreview() {
    AppTheme {
        SettingsScreen(
            uiState = MutableStateFlow(SettingsUIState.Empty),
            onBack = {}
        )
    }
}
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
package com.mobiledevpro.chat.domain.model


enum class AIState {
    IDLE,
    DOWNLOADING_MODEL,
    INITIALIZING,
    READY,
    THINKING,
    SUMMARIZING_CHUNKS,
    EXTRACTING_TEXT,
    COMPLETED,
    ERROR
}

fun AIState.isBusy(): Boolean =
    this == AIState.DOWNLOADING_MODEL ||
            this == AIState.INITIALIZING ||
            this == AIState.THINKING ||
            this == AIState.SUMMARIZING_CHUNKS ||
            this == AIState.EXTRACTING_TEXT
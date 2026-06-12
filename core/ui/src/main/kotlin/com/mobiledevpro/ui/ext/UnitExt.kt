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
package com.mobiledevpro.ui.ext

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
fun Int.dp(): Dp = with(LocalDensity.current) { this@dp.toDp() }

@Composable
fun Int.pxToDp(): Dp = with(LocalDensity.current) { this@pxToDp.toDp() }

@Composable
fun Dp.dpToPx(): Float = with(LocalDensity.current) { this@dpToPx.toPx() }

@Composable
fun android.util.Size.toComposableSize(): androidx.compose.ui.geometry.Size =
    androidx.compose.ui.geometry.Size(
        this.width.pxToDp().value,
        this.height.pxToDp().value
    )
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
package com.mobiledevpro.ui.provider

import androidx.annotation.ArrayRes
import androidx.annotation.StringRes

/**
 * Provider for app resources (as example, from strings.xml) to use in ViewModel
 *
 * Created on Jan 15, 2024.
 *
 */
interface ResourcesProvider {
    fun getStringMessage(@StringRes resId: Int): String

    fun getFormatStringMessage(@StringRes resId: Int, vararg args: Any): String

    fun getStringsArray(@ArrayRes arrayResId: Int): Array<String>
}
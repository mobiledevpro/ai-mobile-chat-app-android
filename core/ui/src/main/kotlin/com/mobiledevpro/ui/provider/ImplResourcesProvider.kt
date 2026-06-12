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

import android.content.res.Resources

/**
 * Provider for app resources to use in ViewModel (as example, from strings.xml)
 *
 * Created on Jan 15, 2024.
 *
 */
class ImplResourcesProvider(
    private val resources: Resources
) : ResourcesProvider {
    override fun getStringMessage(resId: Int) =
        resources.getString(resId)

    override fun getFormatStringMessage(resId: Int, vararg args: Any): String =
        resources.getString(resId, *args)

    override fun getStringsArray(arrayResId: Int): Array<String> =
        resources.getStringArray(arrayResId)

}
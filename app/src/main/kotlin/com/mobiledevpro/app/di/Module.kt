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
package com.mobiledevpro.app.di

import android.content.ContentResolver
import androidx.work.WorkManager
import com.mobiledevpro.database.AppDatabase
import com.mobiledevpro.filestore.FileStoreManager
import com.mobiledevpro.filestore.ImplFileStoreManager
import com.mobiledevpro.ui.provider.ImplResourcesProvider
import com.mobiledevpro.ui.provider.ResourcesProvider
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreModule = module {
    single<ContentResolver> {
        androidContext().applicationContext.contentResolver
    }

    single<ResourcesProvider> {
        ImplResourcesProvider(androidContext().resources)
    }

    single<FileStoreManager> {
        ImplFileStoreManager(androidApplication().applicationContext)
    }

    single<WorkManager> {
        WorkManager.getInstance(androidApplication().applicationContext)
    }

    single<AppDatabase> {
        AppDatabase.buildDatabase(androidApplication().applicationContext)
    }
}
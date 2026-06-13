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
package com.mobiledevpro.download

import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.mobiledevpro.download.worker.DownloadWorker

/**
 * Helper class to configure Worker
 *
 * Created on Jun 13, 2026.
 *
 */

object DownloadManagerHelper {
    // https://huggingface.co/$modelId/resolve/$commitHash/$modelFile?download=true
    const val REPO_URL =
        "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm?download=true"
    const val WORKER_TAG = "LLM_DOWNLOAD_TASK"

    private val constraints = Constraints.Builder()
        // Force Wi-Fi connection to safeguard data plans
        // .setRequiredNetworkType(NetworkType.UNMETERED)
        .setRequiresStorageNotLow(true)
        .build()

    val downloadWorkRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
        .setConstraints(constraints)
        .addTag(WORKER_TAG)
        .build()

    val llmLocalFileName = "gemma_4_e2b.litertlm"

    fun startDownloading(workManager: WorkManager) {
        workManager.enqueueUniqueWork(
            uniqueWorkName = "llm_model_download",
            existingWorkPolicy = ExistingWorkPolicy.KEEP,
            request = downloadWorkRequest
        )
    }
}

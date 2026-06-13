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
package com.mobiledevpro.download.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.mobiledevpro.download.domain.usecase.DownloadUseCase
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Background worker to download LLM to local file storage
 * 
 * Created on Jun 13, 2026.
 *
 */
class DownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val downloadUseCase: DownloadUseCase
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val repoUrl =
            "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/model.litertlm"

        // Save into app internal system storage context so it's sandbox protected
        val targetFile = File(applicationContext.filesDir, "gemma_4_e2b.litertlm")

        // Delete partial artifacts from failed previous runs to guarantee clean writes
        if (targetFile.exists()) {
            targetFile.delete()
        }

        val client = OkHttpClient.Builder().build()
        val request = Request.Builder()
            .url(repoUrl)
            // .addHeader("Authorization", "Bearer hf_xxx") // Uncomment if accessing gated models
            .build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected network error code: $response")

                val body =
                    response.body ?: throw IOException("Empty payload returned from Hugging Face.")
                val totalBytes = body.contentLength()

                body.byteStream().use { inputStream ->
                    FileOutputStream(targetFile).use { outputStream ->
                        val data = ByteArray(BUFFER_SIZE)
                        var bytesRead: Int
                        var totalBytesRead: Long = 0
                        var lastProgressUpdate = 0

                        while (inputStream.read(data).also { bytesRead = it } != -1) {
                            outputStream.write(data, 0, bytesRead)
                            totalBytesRead += bytesRead

                            if (totalBytes > 0) {
                                val currentProgress = ((totalBytesRead * 100) / totalBytes).toInt()
                                // Throttle work updates to minimize system IPC message cycles
                                if (currentProgress > lastProgressUpdate) {
                                    lastProgressUpdate = currentProgress
                                    setProgress(workDataOf(KEY_PROGRESS to currentProgress))
                                }
                            }
                        }
                        outputStream.flush()
                    }
                }
            }
            Result.success()
        } catch (e: Exception) {
            if (targetFile.exists()) {
                targetFile.delete()
            } // Clean up failed/broken download files
            Result.failure(workDataOf(KEY_ERROR to (e.localizedMessage ?: "Network IO failure")))
        }
    }

    /*private fun kotlin.Result<Any>.toWorkerResult(): Result =
        if (this.isSuccess)
            Result.success()
        else {
            val error = this.exceptionOrNull()?.localizedMessage
                ?: this.exceptionOrNull()?.cause?.localizedMessage ?: "Unknown error"
            workDataOf(KEY_ERROR to error)
                .let(Result::failure)
        }*/

    companion object {
        // https://huggingface.co/$modelId/resolve/$commitHash/$modelFile?download=true
        const val REPO_URL =
            "https://huggingface.co/litert-community/gemma-4-E2B-it-litert-lm/resolve/main/gemma-4-E2B-it.litertlm?download=true"
        const val KEY_PROGRESS = "PROGRESS"
        const val KEY_ERROR = "ERROR_MESSAGE"
        private const val BUFFER_SIZE = 8192 // 8 KB byte buffer
    }
}

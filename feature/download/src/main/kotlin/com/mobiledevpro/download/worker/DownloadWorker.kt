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
import com.mobiledevpro.download.BuildConfig
import com.mobiledevpro.download.DownloadManagerHelper
import com.mobiledevpro.download.DownloadManagerHelper.REPO_URL
import com.mobiledevpro.filestore.FileStoreManager
import com.mobiledevpro.filestore.FileType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
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
    private val fileStoreManager: FileStoreManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val targetFile = fileStoreManager.createFile(
            DownloadManagerHelper.llmLocalFileName,
            extension = "",
            FileType.TEMP_AI_MODEL
        )

        val client = OkHttpClient.Builder().build()
        val request = Request.Builder()
            .url(REPO_URL)
            .addHeader(
                "Authorization",
                "Bearer ${BuildConfig.HUGGING_FACE_TOKEN}"
            )
            .build()

        return try {
            withContext(Dispatchers.IO) {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected network error code: $response")

                    val body =
                        response.body
                            ?: throw IOException("Empty payload returned from Hugging Face.")
                    val totalBytes = body.contentLength()

                    body.byteStream().use { inputStream ->
                        FileOutputStream(targetFile).use { outputStream ->
                            val data = ByteArray(BUFFER_SIZE)
                            var bytesRead: Int
                            var totalBytesRead: Long = 0
                            var lastProgressUpdate = 0

                            while (inputStream.read(data).also { bytesRead = it } != -1) {
                                // Check if worker was cancelled by the system
                                if (isStopped) {
                                    throw IOException("Download cancelled")
                                }

                                outputStream.write(data, 0, bytesRead)
                                totalBytesRead += bytesRead

                                if (totalBytes > 0) {
                                    val currentProgress =
                                        ((totalBytesRead * 100) / totalBytes).toInt()
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
            }
            // Return 100% progress and the file path on success
            Result.success(
                workDataOf(
                    KEY_PROGRESS to 100,
                    KEY_FILE_PATH to targetFile.absolutePath
                )
            )
        } catch (e: Exception) {
            if (targetFile.exists()) {
                targetFile.delete()
            } // Clean up failed/broken download files
            Result.failure(
                workDataOf(
                    KEY_ERROR to (e.localizedMessage ?: "Network IO failure"),
                    KEY_PROGRESS to 0
                )
            )
        }
    }

    companion object {
        const val KEY_PROGRESS = "PROGRESS"
        const val KEY_ERROR = "ERROR_MESSAGE"
        const val KEY_FILE_PATH = "FILE_PATH"
        private const val BUFFER_SIZE = 8192 // 8 KB byte buffer
    }
}

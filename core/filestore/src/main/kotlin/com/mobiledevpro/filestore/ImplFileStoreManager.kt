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
package com.mobiledevpro.filestore

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Manager to read/write files into in-app directory
 *
 * Created on Dec 02, 2023.
 *
 */

enum class FileType {
    TEMP_PDF,
    TEMP_AI_MODEL
}

class ImplFileStoreManager(
    private val appContext: Context
) : FileStoreManager {

    private val filesDir: (FileType?) -> File = {
        val defaultDirPath = appContext.filesDir.absolutePath

        val path = when (it) {
            FileType.TEMP_PDF -> "$defaultDirPath${File.separator}temp_pdf"
            else -> defaultDirPath
        }

        File(path).also { dir: File ->
            if (!dir.exists())
                dir.mkdirs()
        }
    }

    private val cacheDir: (FileType?) -> File = {
        val defaultCachePath = appContext.cacheDir.absolutePath


        val path = when (it) {
            FileType.TEMP_PDF -> "$defaultCachePath${File.separator}_temp_pdfs"
            else -> defaultCachePath
        }

        File(path).also { dir: File ->
            if (!dir.exists())
                dir.mkdirs()
        }
    }

    override suspend fun getFile(fileName: String, fileType: FileType): File? =
        filesDir(fileType).absolutePath
            .let { path: String ->
                val file = File("${path}${File.separator}${fileName}")

                if (file.exists() && file.length() > 0) file else null
            }

    override suspend fun createFile(fileName: String, extension: String, fileType: FileType): File =
        filesDir(fileType).absolutePath
            .let { path: String ->
                File("${path}${File.separator}${fileName}${extension}")
            }
            .also { newFile: File ->
                if (newFile.exists() && newFile.length() > 0)
                    newFile.delete()

                if (newFile.parentFile?.exists() == false)
                    newFile.parentFile?.mkdirs()
            }

    override suspend fun createCacheFile(fileType: FileType, fileName: String): File =
        cacheDir(fileType).absolutePath
            .let { path: String ->
                val extension = when (fileType) {
                    FileType.TEMP_PDF -> ".pdf"
                    else -> ""
                }

                File("${path}${File.separator}${fileName}${extension}")
            }
            .also { newFile: File ->
                if (newFile.exists() && newFile.length() > 0)
                    newFile.delete()

                if (newFile.parentFile?.exists() == false)
                    newFile.parentFile?.mkdirs()
            }


    @Throws(RuntimeException::class)
    override suspend fun removeFile(file: File) {
        try {
            if (file.exists())
                file.delete()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    override suspend fun cleanDirectoryFor(fileType: FileType) {
        filesDir(fileType)
            .also(::removeDirectoryFiles)
    }

    override suspend fun File.copyFileTo(dest: File) {
        copyTo(dest, overwrite = true)
    }

    @Throws(RuntimeException::class)
    override suspend fun inputStreamToFile(inStream: InputStream, outFile: File): File =
        try {
            val out = FileOutputStream(outFile.path)
            val buffer = ByteArray(4 * 1024)
            var read: Int
            while (inStream.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
            inStream.close()

            out.flush()
            out.close()

            outFile
        } catch (ee: IOException) {
            throw RuntimeException(ee)
        }

    override suspend fun fileToFileProviderUri(file: File): Uri =
        withContext(Dispatchers.IO) {
            FileProvider.getUriForFile(
                appContext,
                "${appContext.packageName}.provider",
                file
            )
        }

    private fun removeNonEmptyDirectory(dir: File) {
        if (!dir.exists()) return

        dir.listFiles()?.forEach {
            if (it.isDirectory) {
                removeNonEmptyDirectory(it)
            } else {
                it.delete()
            }
        }

        dir.delete()
    }

    private fun removeDirectoryFiles(dir: File) {
        if (!dir.exists()) return

        dir.listFiles()?.forEach {
            if (it.isDirectory) {
                removeNonEmptyDirectory(it)
            } else {
                it.delete()
            }
        }
    }

    override suspend fun isFileExists(fileName: String, fileType: FileType): Boolean =
        filesDir(fileType).absolutePath
            .let { path: String ->
                val file = File("${path}${File.separator}${fileName}")

                file.exists() && file.length() > 0
            }

    @Throws(RuntimeException::class)
    override suspend fun copyAssetsFileToStorage(assetFileName: String, fileType: FileType): File =
        try {
            createFile(assetFileName, "", fileType).let { targetFile ->
                appContext.assets.open(assetFileName).use { inputStream ->
                    FileOutputStream(targetFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                targetFile
            }
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
}
/*
 * Copyright 2023 | Dmitri Chernysh | https://github.com/dmitriy-chernysh
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

import android.net.Uri
import java.io.File
import java.io.InputStream

/**
 * Manager to read/write files into in-app directory
 *
 * Created on May 25, 2024.
 *
 */
interface FileStoreManager {

    suspend fun getFile(fileName: String, fileType: FileType): File?

    suspend fun createFile(fileName: String, extension: String, fileType: FileType): File

    suspend fun createCacheFile(fileType: FileType, fileName: String): File

    suspend fun removeFile(file: File)

    suspend fun cleanDirectoryFor(fileType: FileType)

    suspend fun File.copyFileTo(dest: File)

    @Throws(RuntimeException::class)
    suspend fun inputStreamToFile(inStream: InputStream, outFile: File): File

    suspend fun fileToFileProviderUri(file: File): Uri

    suspend fun isFileExists(fileName: String, fileType: FileType): Boolean

    suspend fun copyAssetsFileToStorage(assetFileName: String, fileType: FileType): File

}
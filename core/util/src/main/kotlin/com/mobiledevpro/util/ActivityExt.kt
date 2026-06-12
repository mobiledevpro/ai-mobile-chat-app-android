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
package com.mobiledevpro.util

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

suspend fun Activity.shareFile(file: File) {

    withContext(Dispatchers.IO) {
        val uriForFile = FileProvider.getUriForFile(
            this@shareFile,
            "${this@shareFile.packageName}.provider", file
        )

        Intent().apply {
            action = Intent.ACTION_SEND
            type = Constant.getMimeTypeFromFile(file)
            putExtra(Intent.EXTRA_SUBJECT, file.name)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra(Intent.EXTRA_STREAM, uriForFile)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }.also { intent ->
            withContext(Dispatchers.Main) {
                Log.d(
                    "sharefile",
                    "shareFile: start activity Thread: ${Thread.currentThread().name}"
                )
                try {
                    this@shareFile.startActivity(Intent.createChooser(intent, "Share with..."))
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        this@shareFile,
                        "No found activity to complete this action",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (es: SecurityException) {
                    Toast.makeText(
                        this@shareFile,
                        "Sorry the acton is not allowed",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (ee: Exception) {
                    Toast.makeText(
                        this@shareFile,
                        "Sorry cannot complete the acton: ${ee.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

}

suspend fun Activity.shareImages(imageList: List<Uri>) {
    withContext(Dispatchers.IO) {
        // To provide access to internal files to other apps
        val getUriForFile: (Uri) -> Uri = { uri ->
            FileProvider.getUriForFile(
                this@shareImages,
                "${this@shareImages.packageName}.provider", uri.toFile()
            )
        }

        val imageUriList = imageList.map { getUriForFile(it) }

        Intent().apply {
            action = Intent.ACTION_SEND_MULTIPLE
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(imageUriList))
            type = "image/*"
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }.also { intent ->
            withContext(Dispatchers.Main) {
                try {
                    this@shareImages.startActivity(Intent.createChooser(intent, "Share with..."))
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(
                        this@shareImages,
                        "No found activity to complete this action",
                        Toast.LENGTH_LONG
                    )
                        .show()
                } catch (es: SecurityException) {
                    Toast.makeText(
                        this@shareImages,
                        "Sorry the acton is not allowed",
                        Toast.LENGTH_LONG
                    ).show()
                } catch (ee: Exception) {
                    Toast.makeText(
                        this@shareImages,
                        "Sorry cannot complete the acton: ${ee.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }


}

fun Context.dpToPx(valueInDp: Float): Float {
    val metrics = resources.displayMetrics
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, valueInDp, metrics)
}
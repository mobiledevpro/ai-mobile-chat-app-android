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
package com.mobiledevpro.tooling

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URLEncoder

/**
 * Snippets to handle users query
 * 
 * Created on Jun 12, 2026.
 *
 */
object ToolSnippet {
    /**
     * Executes a background search query, scraps the HTML result page,
     * and extracts clean text snippets to feed back to the LLM.
     *
     */
    suspend fun fetchSearchSnippet(query: String): String = withContext(Dispatchers.IO) {
        try {
            // 1. URL-encode the query parameters to handle spaces and special characters safely
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val searchUrl = "https://www.google.com/search?q=$encodedQuery"

            // 2. Connect to the web server and spoof a real Chrome browser user-agent
            val document = Jsoup.connect(searchUrl)
                .userAgent("Mozilla/5.0 (Linux; Android 10; K) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36")
                .timeout(10000) // 10 seconds timeout safety barrier
                .get()

            // 3. Select target CSS classes where Google encapsulates snippet summaries
            // .VwiC3b is commonly used on modern mobile/desktop layouts
            // .kCrYT is often used on basic/legacy fallback HTML layouts
            val snippets = document.select(".VwiC3b, .kCrYT")

            if (snippets.isNotEmpty()) {
                // 4. Extract text from the top 3 results, clean whitespace, and join them
                snippets.take(3)
                    .map { it.text().trim() }
                    .filter { it.isNotEmpty() }
                    .joinToString("\n\n")
            } else {
                // Fallback: If classes changed, pull paragraphs or description metadata strings
                val fallbackText = document.select("span").take(5).joinToString(" ") { it.text() }
                if (fallbackText.length > 50) {
                    fallbackText.take(300) + "..."
                } else {
                    "No direct text summaries could be parsed from the web layout."
                }
            }
        } catch (e: Exception) {
            // Gracefully catch network loss, timeouts, or parsing errors
            "Network search engine failed: ${e.localizedMessage ?: "Unknown error"}"
        }
    }
}

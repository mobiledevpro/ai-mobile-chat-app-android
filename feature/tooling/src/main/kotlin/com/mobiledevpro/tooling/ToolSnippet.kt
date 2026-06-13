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

import android.content.Context
import android.provider.CalendarContract
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.util.Calendar

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
            val encodedQuery = URLEncoder.encode(query, "UTF-8")

            // Try Google first with a configuration designed for basic HTML
            val googleUrl = "https://www.google.com/search?q=$encodedQuery&hl=en&gbv=1&gl=us"

            //Log.d("ToolSnippet", "fetchSearchSnippet: $googleUrl")

            val response = Jsoup.connect(googleUrl)
                // Using a bot-like User-Agent often forces Google to serve the simplest possible HTML
                .userAgent("Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
                .header("Accept-Language", "en-US,en;q=0.9")
                .header(
                    "Accept",
                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"
                )
                .header("Referer", "https://www.google.com/")
                .cookie("CONSENT", "YES+cb.20210328-17-p0.en+FX+410")
                .timeout(10000)
                .followRedirects(true)
                .execute()

            var document = response.parse()
            val bodyText = document.text()

            // Check if Google is blocking us or insisting on JavaScript
            if (bodyText.contains("JavaScript") || bodyText.contains("not redirected") || document.select(
                    ".VwiC3b, .kCrYT, .BNeawe"
                ).isEmpty()
            ) {
                Log.d("ToolSnippet", "Google search restricted. Falling back to DuckDuckGo HTML.")

                // Fallback to DuckDuckGo HTML (designed for non-JS environments)
                val ddgUrl = "https://html.duckduckgo.com/html/?q=$encodedQuery"
                document = Jsoup.connect(ddgUrl)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .get()
            }

            //  Log.d("ToolSnippet", "Parsing results from: ${document.body()}")

            // CSS selectors for both Google and DuckDuckGo
            val selectors = arrayOf(".BNeawe", ".kCrYT", ".result__snippet", "span.hgKElc", "div.g")
            val snippets = document.select(selectors.joinToString(", "))

            if (snippets.isNotEmpty()) {
                snippets
                    .map { it.text().trim() }
                    .filter { it.length > 10 }
                    .distinct()
                    .take(5)
                    .joinToString("\n\n")
            } else {
                // Last ditch effort: extract any content from paragraphs
                val fallbackText = document.select("p, .result__snippet").take(5)
                    .joinToString("\n\n") { it.text() }
                if (fallbackText.length > 50) {
                    fallbackText
                } else {
                    "No direct search results could be parsed. The search engine might be blocking the request."
                }
            }
        } catch (e: Exception) {
            "Network search failed: ${e.localizedMessage ?: "Unknown error"}"
        }
    }

    suspend fun fetchCalendarEvents(context: Context): String = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val calendar = Calendar.getInstance()

            // Set range for "today"
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startMillis = calendar.timeInMillis

            calendar.set(Calendar.HOUR_OF_DAY, 23)
            calendar.set(Calendar.MINUTE, 59)
            calendar.set(Calendar.SECOND, 59)
            calendar.set(Calendar.MILLISECOND, 999)
            val endMillis = calendar.timeInMillis

            val projection = arrayOf(
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.DTEND
            )

            val selection =
                "(${CalendarContract.Events.DTSTART} >= ?) AND (${CalendarContract.Events.DTSTART} <= ?)"
            val selectionArgs = arrayOf(startMillis.toString(), endMillis.toString())

            val cursor = contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${CalendarContract.Events.DTSTART} ASC"
            )

            val events = mutableListOf<String>()
            cursor?.use {
                while (it.moveToNext()) {
                    val title = it.getString(0)
                    events.add("- $title")
                }
            }

            if (events.isEmpty()) {
                "No events found for today."
            } else {
                "Today's schedule:\n" + events.joinToString("\n")
            }
        } catch (e: SecurityException) {
            "Error: Calendar permission is required to view events. Go to App Info - Permissions and allow Calendar"
        } catch (e: Exception) {
            "Failed to fetch calendar: ${e.localizedMessage}"
        }
    }
}

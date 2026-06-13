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
package com.mobiledevpro.chat.domain.model

/**
 * 
 * Created on Jun 12, 2026.
 *
 */
object PromptWrapper {

    private const val START_TURN = "<start_of_turn>"
    private const val END_TURN = "<end_of_turn>"

    fun createSystemPrompt() =
        "You are a context-aware offline Android assistant equipped with local tools. " +
                "CRITICAL INSTRUCTION: " +
                "\n1. For any questions about current events, prices (like BTC, Gold, Stocks), " +
                "weather, or real-time data, you MUST use the 'web_search' tool. " +
                "\n2. Do NOT guess or provide info from your training data for real-time facts. " +
                "\n3. If the user asks What's my schedule or want to view calendar events, you MUST use the 'view_calendar' tool. "

    fun createPrompt(skillsTxt: String?, userMessage: String, history: List<ChatMessage>): String {
        return if (skillsTxt.isNullOrEmpty()) {
            createDefaultPrompt(userMessage, history)
        } else {
            createPromptWithSkills(skillsTxt, userMessage, history)
        }
    }

    private fun createDefaultPrompt(userMessage: String, history: List<ChatMessage>): String =
        StringBuilder().apply {
            // 1. System Orientation
            append("${START_TURN}user\nYou are a helpful and friendly local AI assistant.${END_TURN}\n")
            append("${START_TURN}model\nUnderstood. How can I help you today?${END_TURN}\n")

            // 2. Append history
            history.takeLast(10).forEach { message ->
                val roleTag = if (message.isUserMessage) "user" else "model"
                append("${START_TURN}$roleTag\n${message.text}${END_TURN}\n")
            }

            // 3. Current Request
            append("${START_TURN}user\n$userMessage${END_TURN}\n")
            append("${START_TURN}model\n")
        }.toString()

    private fun createPromptWithSkills(
        skillsTxt: String,
        userMessage: String,
        history: List<ChatMessage>
    ): String {
        return StringBuilder().apply {
            // 1. System Rules + Skills
            append("${START_TURN}user\n")
            append("You are a versatile local AI assistant. You operate in TWO distinct modes:\n\n")
            append("1. **GENERAL CHAT**: For greetings, jokes, creative writing, or general knowledge. Respond naturally.\n")
            append("2. **TOOL TRIGGER**: For specific tasks like searching the web or checking the calendar. Use the TRIGGER syntax ONLY if the intent matches the rules below.\n\n")

            append("### Available Skill Rules:\n")
            append(skillsTxt)

            append("\n\n### CRITICAL DIRECTIVE:\n")
            append("- If the request matches a skill above, output the EXACT TRIGGER line and nothing else.\n")
            append("- If the request is conversational (e.g., 'Hello', 'Tell me a joke', 'Who are you?'), do NOT use a trigger. Answer directly.\n")
            append("${END_TURN}\n")

            // 2. Model Confirmation
            append("${START_TURN}model\n")
            append("Understood. I will use triggers only when a specific skill match is found. Otherwise, I will converse normally.")
            append("${END_TURN}\n")

            // 3. Few-Shot Examples (Balanced)
            // Example: Greeting
            append("${START_TURN}user\nHello!${END_TURN}\n")
            append("${START_TURN}model\nHello! How can I assist you today?${END_TURN}\n")

            // Example: Web Search Trigger
            append("${START_TURN}user\nWhat is the price of Gold today?${END_TURN}\n")
            append("${START_TURN}model\nTRIGGER_WEB_SEARCH(\"current gold price\")${END_TURN}\n")

            // Example: General Knowledge
            append("${START_TURN}user\nWhy is the sky blue?${END_TURN}\n")
            append("${START_TURN}model\nThe sky appears blue because of Rayleigh scattering. Sunlight reaches Earth's atmosphere and is scattered in all directions by the gases and particles in the air.${END_TURN}\n")

            // Example: Calendar Trigger
            append("${START_TURN}user\nShow my schedule.${END_TURN}\n")
            append("${START_TURN}model\nTRIGGER_CALENDAR_VIEW${END_TURN}\n")

            // 4. History
            history.takeLast(5).forEach { message ->
                val roleTag = if (message.isUserMessage) "user" else "model"
                append("${START_TURN}$roleTag\n${message.text}${END_TURN}\n")
            }

            // 5. Current Request
            append("${START_TURN}user\n$userMessage${END_TURN}\n")
            append("${START_TURN}model\n")
        }.toString()
    }
}

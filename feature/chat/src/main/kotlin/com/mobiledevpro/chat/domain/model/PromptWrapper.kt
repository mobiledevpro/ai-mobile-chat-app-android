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

    fun createPrompt(skillsTxt: String?, userMessage: String, history: List<ChatMessage>): String {
        return if (skillsTxt.isNullOrEmpty()) {
            createDefaultPrompt(history)
        } else {
            createPromptWithSkills(skillsTxt, userMessage)
        }
    }

    private fun createDefaultPrompt(history: List<ChatMessage>): String =
        StringBuilder().apply {
            // Optional: Add a simple system orientation prompt if needed
            append("${START_TURN}user\nYou are a local AI assistant.${END_TURN}\n")
            append("${START_TURN}model\nUnderstood. How can I help you today?${END_TURN}\n")

            // Append all previous conversation turns cleanly
            history.forEach { message ->
                val roleTag = if (message.isUserMessage) "user" else "model"
                append("${START_TURN}$roleTag\n")
                append(message.text)
                append("${END_TURN}\n")
            }

            append("${START_TURN}model\n")
        }.toString()

    private fun createPromptWithSkills(skillsTxt: String, userMessage: String): String {
        return StringBuilder().apply {
            // 1. Inject System Rules + SKILLS.md text
            append("${START_TURN}user\n")
            append("You are a local AI assistant. You have access to native tools. ")
            append("Analyze the user's intent based strictly on these markdown skill rules:\n\n")
            append(skillsTxt)
            append("\n\nIf a skill matches, output the exact TRIGGER phrase. Do not add filler text.\n")
            append("${END_TURN}\n")

            // 2. Mock the model's confirmation to seal the rules in history
            append("${START_TURN}model\n")
            append("Understood. I will check incoming messages against the skills and return TRIGGERS when required.")
            append("${END_TURN}\n")

            // 3. Append the live user request
            append("${START_TURN}user\n")
            append(userMessage)
            append("${END_TURN}\n")

            // 4. Leave the turn open for the model to generate the answer
            append("${START_TURN}model\n")
        }.toString()
    }
}

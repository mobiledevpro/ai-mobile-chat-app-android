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
            createDefaultPrompt(userMessage, history)
        } else {
            createPromptWithSkills(skillsTxt, userMessage, history)
        }
    }

    private fun createDefaultPrompt(userMessage: String, history: List<ChatMessage>): String =
        StringBuilder().apply {
            // 2. Add a simple system orientation prompt if needed
            append("You are a helpful and friendly local AI assistant.${END_TURN}\n")
            append("${START_TURN}model\nUnderstood. How can I help you today?${END_TURN}\n")

            // 1. Append all previous conversation
            history.dropLast(1).forEach { message ->
                val roleTag = if (message.isUserMessage) "user" else "model"
                append("${START_TURN}$roleTag\n")
                append(message.text)
                append("${END_TURN}\n")
            }

            // 4. Append the last user request
            append("${START_TURN}user\n")
            append(userMessage)
            append("${END_TURN}\n")

            append("${START_TURN}model\n")
        }.toString()

    private fun createPromptWithSkills(
        skillsTxt: String,
        userMessage: String,
        history: List<ChatMessage>
    ): String {
        return StringBuilder().apply {
            // 1. Append all previous conversation
            /*    history.dropLast(1).forEach { message ->
                    val roleTag = if (message.isUserMessage) "user" else "model"
                    append("${START_TURN}$roleTag\n")
                    append(message.text)
                    append("${END_TURN}\n")
                }


             */
            // 2. Inject System Rules + SKILLS.md text
            /*  append("${START_TURN}user\n")
              append("You are a local AI assistant. You have access to native tools. ")
              append("Analyze the user's intent based strictly on these markdown skill rules:\n\n")
              append(skillsTxt)
              append("\n\nIf a skill matches, output the exact TRIGGER phrase. Do not add filler text.\n")
              append("CRITICAL: You only have access to WEB_SEARCH and CALENDAR_VIEW. ")
              append("If the user asks for anything else (like telling a joke, coding, or singing), ")
              append("do NOT use a TRIGGER phrase. Simply respond directly to the user as a standard conversational assistant.\n")
              append("${END_TURN}\n")



              // 3. Mock the model's confirmation to seal the rules in history
              append("${START_TURN}model\n")
              append("Understood. I will check incoming messages against the skills and return TRIGGERS when required.")
              append("${END_TURN}\n")

             */

            // Inject System Rules + SKILLS.md text
            /*  append("${START_TURN}user\n")
              append("You are a local AI assistant capable of operating in TWO modes:\n\n")
              append("MODE 1: TOOL TRIGGER MODE\n")
              append("Analyze the user's intent based strictly on these markdown skill rules:\n\n")
              append(skillsTxt)
              append("\n\nMODE 2: STANDARD CHAT MODE\n")
              append("If the user's request does NOT match the explicit rules for 'calendar-schedule' or 'web-search-skill' ")
              append("(for example: asking you to tell a joke, write code, chat casually, or create a story), ")
              append("you MUST bypass Tool Trigger Mode entirely. Do NOT output any TRIGGER text. ")
              append("Instead, respond directly, naturally, and creatively to the user using your own internal knowledge parameters.\n")
              append("${END_TURN}\n")

             */

            //append("${START_TURN}user\n")
            /*   append("You are a local AI assistant with access to native tools.\n")
               append("If you don't have access to something, analyze the user's intent based on these markdown skills rules:\n\n")
               append(skillsTxt)
               append("\n\nCRITICAL DIRECTIVE:\n")
               append("- If the request matches a skill, output the exact TRIGGER line. Do not add conversational text.\n")
               append("- If the request does NOT match a skill (e.g., asking for a joke, casual chatting, creative writing), ")
               append("do NOT use any TRIGGER. Respond directly using your internal knowledge.\n")
               append("${END_TURN}\n")

               // Mock the model's confirmation to seal the rules in history
               append("${START_TURN}model\n")
               //append("Understood. I will use TRIGGER_CALENDAR_VIEW or TRIGGER_WEB_SEARCH only when the strict markdown criteria are met. For general requests like telling jokes or casual conversation, I will answer directly using my pre-trained knowledge.")
               append("Understood. I will use triggers only for explicit calendar or web search requests. Otherwise, I will converse normally.")
               append("${END_TURN}\n")

               // --- FEW-SHOT EXAMPLES (The Secret Sauce) ---

   // Example 1: Web Search Trigger
               append("${START_TURN}user\nWhat is the weather in Kyiv?${END_TURN}\n")
               append("${START_TURN}model\nTRIGGER_WEB_SEARCH(\"current weather Kyiv\")${END_TURN}\n")

   // Example 2: Conversational Native Response (No Trigger)
               append("${START_TURN}user\ntell me a joke${END_TURN}\n")
               append("${START_TURN}model\nWhy don't scientists trust atoms? Because they make up everything!${END_TURN}\n")

   // Example 3: Calendar Trigger
               append("${START_TURN}user\nShow my schedule${END_TURN}\n")
               append("${START_TURN}model\nTRIGGER_CALENDAR_VIEW${END_TURN}\n")

   // --- END OF EXAMPLES ---

             */

            append("You are a versatile local AI assistant. You operate in TWO distinct modes:\n\n")
            append("1. **GENERAL CHAT**: For greetings, jokes, creative writing, or general knowledge. Respond naturally.\n")
            append("2. **TOOL TRIGGER**: For specific tasks like searching the web or checking the calendar. Use the TRIGGER syntax ONLY if the intent matches the rules below.\n\n")

            append("### Available Skill Rules:\n")
            append(skillsTxt)

            append("\n\n### CRITICAL DIRECTIVE:\n")
            append("- If the request matches a skill above, output the EXACT TRIGGER line and nothing else.\n")
            append("- If the request is conversational (e.g., 'Hello', 'Tell me a joke', 'Who are you?'), do NOT use a trigger. Answer directly.\n")
            append("${END_TURN}\n")

            // 2. "Seal" the instructions with a model confirmation
            append("${START_TURN}model\n")
            append("Understood. I will use triggers only when a specific skill match is found. Otherwise, I will converse normally.")
            append("${END_TURN}\n")

            // 3. Diversified Few-Shot Examples (The Secret Sauce)
            // Example: Greeting (Conversation)
            append("${START_TURN}user\nHello!${END_TURN}\n")
            append("${START_TURN}model\nHello! How can I assist you today?${END_TURN}\n")

            // Example: Web Search (Trigger)
            append("${START_TURN}user\nWhat is the price of Gold today?${END_TURN}\n")
            append("${START_TURN}model\nTRIGGER_WEB_SEARCH(\"current gold price\")${END_TURN}\n")

            // Example: General Knowledge (Conversation)
            append("${START_TURN}user\nWhy is the sky blue?${END_TURN}\n")
            append("${START_TURN}model\nThe sky appears blue because of Rayleigh scattering. Sunlight reaches Earth's atmosphere and is scattered in all directions by the gases and particles in the air.${END_TURN}\n")

            // Example: Calendar (Trigger)
            append("${START_TURN}user\nShow my schedule.${END_TURN}\n")
            append("${START_TURN}model\nTRIGGER_CALENDAR_VIEW${END_TURN}\n")

            // 4. Live History
            history.takeLast(5).forEach { message ->
                val roleTag = if (message.isUserMessage) "user" else "model"
                append("${START_TURN}$roleTag\n${message.text}${END_TURN}\n")
            }

            // 5. Current user request
            //append("${START_TURN}user\n$userMessage${END_TURN}\n")
            append("${START_TURN}model\n")
        }.toString()
    }
}

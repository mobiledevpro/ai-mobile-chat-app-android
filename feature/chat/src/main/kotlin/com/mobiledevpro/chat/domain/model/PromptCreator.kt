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

internal const val DEFAULT_PROMPT_ENGLISH =
    //"Detect the language of the Input Text, summarize it in that same language and break down the summary into 3 sections: \nSection 1: Key Takeaways; \nSection 2: Action Items; \nSection 3: Summary."
    "Instruction: Analyze the text and generate a summary strictly divided into 3 independent blocks. Do not merge the sections. Use the following format:\n" +
            "Section 1: Key Takeaways (3-5 bullet points)\n" +
            "Section 2: Action Items (3-5 bullet points\n" +
            "Section 3: Summary\n"

internal const val DEFAULT_PROMPT_ALL_LANG =
        /*  "Instruction: \n" +
                  "1. Identify the language of the Text provided below.\n" +
                  "2. Constraint: You must respond ONLY in that identified language.\n" +
                  "3. Provide a concise summary of the Text and break down the summary into 3 sections translated to identified language: \n" +
                  "- Section 1: Key Takeaways\n" +
                  "- Section 2: Action Items\n" +
                  "- Section 3: Summary\n" +
                  "4. Section headers should be translated to the identified language.\n"

         */
        /* "Role: Act as a multilingual linguistic expert and professional note-taker.\n" +
                 "Task: \n" +
                 "1. Identify the language used in the provided [TEXT].\n" +
                 "2. Provide a concise summary of that text.\n" +
                 "3. Crucial Constraint: The entire output—including the section headers—must be written exclusively in the identified language of the source text. Do not use English unless the source text is in English.\n" +
                 "Format: Structure the response into exactly three sections using the following translated headers:\n" +
                 "1. [Header for Key Takeaways]: (3-5 bullet points of the most important facts).\n" +
                 "2. [Header for Action Items]: (Specific tasks or next steps implied by the text; if none exist, state \"None\").\n" +
                 "3. [Header for Summary]: (A brief, 2-3 sentence cohesive overview).\n " +
                 "[TEXT]:\n"

         */

        /* "Detect the language of the Input Text, summarize it in that same language and break down the summary into 3 sections: \nSection 1: Key Takeaways; \nSection 2: Action Items; \nSection 3: Summary.\n" +
                 "Response in German."

         */

        /*  "Act as a multilingual linguistic expert and professional note-taker. Summarize the Input Text using the following rules:\n" +
                  "1. Use the SAME language as the Input Text.\n" +
                  "2. You MUST use three distinct sections with these EXACT headers with 3-5 bullets in each section:\n" +
                  "   ### Section 1: Key Takeaways \n" +
                  "   ### Section 2: Action Items\n" +
                  "   ### Section 3: Summary\n" +
                  "3. Every bullet point must start with a '*'. \n" +
                  "4. Every section must be separated by a blank line."
      */

    /*
        "Instruction: Summarize the following text written in German language. \n" +
              //  "The summary must be entirely in the Russian language and broken down into 3 sections: \nSection 1: Key Takeaways; \nSection 2: Action Items; \nSection 3: Summary.\n"
                "The summary must be 3 sentences long and be entirely in the German language.\n"

     */

    ""


internal const val DEFAULT_PROMPT_GERMAN =
    "Anweisung: Analysieren Sie den Text und erstellen Sie eine Zusammenfassung, die strikt in 3 unabhängige Blöcke unterteilt ist. Mischen Sie die Abschnitte nicht. Verwenden Sie das folgende Format:\n" +
            "Abschnitt 1: Wichtige Erkenntnisse (3-5 Aufzählungspunkte)\n" +
            "Abschnitt 2: Handlungsempfehlungen (3-5 Aufzählungspunkte)\n" +
            "Abschnitt 3: Zusammenfassung\n"

internal const val DEFAULT_PROMPT_SPANISH =
    "Instrucciones: Analice el texto y genere un resumen dividido estrictamente en tres bloques independientes. No fusione las secciones. Utilice el siguiente formato:\n" +
            "Sección 1: Puntos clave (3-5 viñetas)\n" +
            "Sección 2: Tareas pendientes (3-5 viñetas)\n" +
            "Sección 3: Resumen\n"



object PromptCreator {

    private fun createPromptEnglish(
        customPrompt: String,
        textToSummarize: String,
        isQuestion: Boolean
    ): String {
        val prompt = customPrompt.takeIf { it.isNotBlank() } ?: DEFAULT_PROMPT_ENGLISH
        return if (!isQuestion)
            "$prompt\n" +
                    "Input Text:\n $textToSummarize\n\n"
        else
            "Context: $textToSummarize\n\n" +
                    "Question: $customPrompt\n\n"
    }

    private fun createPromptSpanish(
        customPrompt: String,
        textToSummarize: String,
        isQuestion: Boolean
    ): String {
        val prompt = customPrompt.takeIf { it.isNotBlank() } ?: DEFAULT_PROMPT_SPANISH
        return if (!isQuestion)
            "$prompt\n" +
                    "el Texto:\n $textToSummarize\n\n"
        else
            "Contexto: $textToSummarize\n\n" +
                    "Pregunta: $customPrompt\n\n"
    }

    private fun createPromptGerman(
        customPrompt: String,
        textToSummarize: String,
        isQuestion: Boolean
    ): String {
        val prompt = customPrompt.takeIf { it.isNotBlank() } ?: DEFAULT_PROMPT_GERMAN
        return if (!isQuestion)
            "$prompt\n" +
                    "Zusammenfassung:\n $textToSummarize\n"
        else
            "Kontext: $textToSummarize\n\n" +
                    "Frage: $customPrompt\n\n"
    }
}
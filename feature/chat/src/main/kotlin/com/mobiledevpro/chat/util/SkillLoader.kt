package com.mobiledevpro.chat.util

import android.content.Context
import com.mobiledevpro.chat.domain.model.AgentSkill
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * A utility class for loading skill definitions from the assets folder.
 * Created on Jun 12, 2026.
 *
 */
class SkillLoader(val context: Context) {

    fun loadSkillsFromAssets(): List<AgentSkill> {
        val assetManager = context.assets
        val skillsList = mutableListOf<AgentSkill>()

        try {
            val folders = assetManager.list(SKILLS_DIR) ?: return emptyList()
            for (folder in folders) {
                val filePath = "$SKILLS_DIR/$folder/SKILL.md"
                val inputStream = assetManager.open(filePath)
                val content = BufferedReader(InputStreamReader(inputStream)).use { it.readText() }

                parseSkill(content)?.let { skillsList.add(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return skillsList
    }


    private fun parseSkill(rawContent: String): AgentSkill? {
        val parts = rawContent.split("---")
        if (parts.size < 3) return null // Missing frontmatter boundaries

        val frontmatter = parts[1]
        val body = parts.subList(2, parts.size).joinToString("---").trim()

        var name = ""
        var description = ""

        frontmatter.lines().forEach { line ->
            when {
                line.startsWith("name:") -> name = line.substringAfter("name:").trim()
                line.startsWith("description:") -> description =
                    line.substringAfter("description:").trim()
            }
        }

        if (name.isEmpty() || description.isEmpty()) return null

        return AgentSkill(name, description, body, rawContent)
    }


    companion object {
        private const val SKILLS_DIR = "skills"
    }

}
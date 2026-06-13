package com.mobiledevpro.chat.util

import android.content.Context
import com.google.ai.edge.localagents.core.proto.FunctionDeclaration
import com.google.ai.edge.localagents.core.proto.Schema
import com.google.ai.edge.localagents.core.proto.Type
import com.mobiledevpro.chat.domain.model.AgentSkill
import org.json.JSONObject
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
        var paramsJson: JSONObject? = null

        frontmatter.lines().forEach { line ->
            when {
                line.startsWith("name:") -> name = line.substringAfter("name:").trim()
                line.startsWith("description:") -> description =
                    line.substringAfter("description:").trim()

                line.trim().startsWith("parameters:") -> {
                    val rawJson = frontmatter.substringAfter("parameters:").trim()
                    paramsJson = JSONObject(rawJson)
                }
            }
        }

        if (name.isEmpty() || description.isEmpty()) return null

        // Build out MediaPipe structural schema constraints
        val functionBuilder = FunctionDeclaration.newBuilder()
            .setName(name)
            .setDescription(description)

        paramsJson?.let {
            functionBuilder.setParameters(convertJsonToMediaPipeSchema(it))
        }

        return AgentSkill(
            name = name,
            description = description,
            fullInstructions = body,
            rawContent = rawContent,
            declaration = functionBuilder.build()
        )
    }

    /**
     * Maps arbitrary JSON schema configurations straight into native MediaPipe Schemas
     */
    private fun convertJsonToMediaPipeSchema(json: JSONObject): Schema {
        val builder = Schema.newBuilder()

        val typeString = json.optString("type", "OBJECT")
        builder.type = Type.valueOf(typeString.uppercase())

        if (json.has("description")) {
            builder.description = json.getString("description")
        }

        val properties = json.optJSONObject("properties")
        if (properties != null) {
            val keys = properties.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                val propertyNode = properties.getJSONObject(key)
                // Recursive tree walk evaluation for nested object types
                builder.putProperties(key, convertJsonToMediaPipeSchema(propertyNode))
            }
        }
        return builder.build()
    }


    companion object {
        private const val SKILLS_DIR = "skills"
    }

}
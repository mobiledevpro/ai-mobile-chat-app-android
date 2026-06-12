package com.mobiledevpro.chat.util

import android.content.Context
import com.mobiledevpro.chat.domain.model.Skill

/**
 * 
 * Created on Jun 12, 2026.
 *
 */
class SkillParser(val context: Context) {
    fun loadSkill(skill: Skill): String {
        return context.assets.open(skill.path)
            .bufferedReader()
            .use { it.readText() }
    }
}
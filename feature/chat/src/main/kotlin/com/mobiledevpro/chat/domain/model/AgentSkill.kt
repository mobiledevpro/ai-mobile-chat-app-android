package com.mobiledevpro.chat.domain.model

import androidx.compose.runtime.Immutable

/**
 *
 * Created on Jun 13, 2026.
 *
 */
@Immutable
data class AgentSkill(
    val name: String,
    val description: String,
    val fullInstructions: String,
    val rawContent: String
)
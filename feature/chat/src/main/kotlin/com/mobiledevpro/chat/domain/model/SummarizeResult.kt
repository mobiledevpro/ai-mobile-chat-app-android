package com.mobiledevpro.chat.domain.model

/**
 *
 * Created on Nov 22, 2025.
 *
 */
data class SummarizeResult(
    val state: AIState = AIState.IDLE,
    val summary: String = "",
    val progressMessage: String = "",
    val error: Throwable? = null
) {
    var extractedText: String = ""
}

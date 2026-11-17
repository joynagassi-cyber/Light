package com.example.data.model

enum class AgentProvider(
    val id: String,
    val displayName: String,
    val baseUrl: String,
    val defaultModel: String,
    val pricingUrl: String,
    val authHeader: String = "bearer"
) {
    AGNES("agnes_ai", "Agnes AI", "https://apihub.agnes-ai.com/v1", "agnes-2.5-flash", "https://agnes-ai.com/doc"),
    OPENAI("openai", "OpenAI", "https://api.openai.com/v1", "", "https://openai.com/api/pricing/"),
    ANTHROPIC("anthropic", "Anthropic", "https://api.anthropic.com/v1", "", "https://www.anthropic.com/pricing"),
    OPENROUTER("openrouter", "OpenRouter", "https://openrouter.ai/api/v1", "", "https://openrouter.ai/models"),
    GROQ("groq", "Groq", "https://api.groq.com/openai/v1", "", "https://groq.com/pricing/"),
    FIREBASE("firebase", "Firebase · Gemini", "", "gemini-2.5-flash", "https://firebase.google.com/docs/ai-logic"),
    CUSTOM("custom", "Autre API compatible", "", "", "");

    companion object {
        fun fromId(id: String?): AgentProvider = when (id) {
            "openai_compatible" -> CUSTOM // Migration from the first custom-endpoint setting.
            else -> entries.firstOrNull { it.id == id } ?: AGNES
        }
    }
}

enum class ModelCostKind { FREE_QUOTA, FREE_MODEL, PAID, ACCOUNT_DEPENDENT, UNKNOWN }

data class AgentModelOption(
    val id: String,
    val displayName: String,
    val costKind: ModelCostKind,
    val costDescription: String,
    val promptUsdPerMillion: Double? = null,
    val outputUsdPerMillion: Double? = null,
    val contextTokens: Long? = null
)

data class AgentUsageRecord(
    val id: String,
    val providerId: String,
    val modelId: String,
    val createdAtMillis: Long,
    val inputTokens: Long,
    val outputTokens: Long,
    val estimatedCostUsd: Double?,
    val costNote: String
)

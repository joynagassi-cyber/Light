package com.example.data.model

/** An instruction policy, not a claim about proprietary provider reasoning controls. */
enum class AgentReflection(val key: String, val label: String, val instruction: String) {
    LOW("quick", "Faible", "Réponds directement avec une action ou une réponse courte."),
    MEDIUM("balanced", "Moyen", "Donne une réponse équilibrée et vérifie les contraintes explicites."),
    HIGH("deep", "Élevé", "Vérifie les hypothèses, les données et les conséquences avant de répondre."),
    MAX("max", "Max", "Avant ta réponse, fais deux revues : vérifie d’abord les faits et contraintes, puis les omissions et les effets d’une action envisagée. Corrige les erreurs et indique les limites vérifiables.");

    companion object {
        fun fromKey(key: String?): AgentReflection = entries.firstOrNull { it.key == key } ?: MEDIUM
    }
}

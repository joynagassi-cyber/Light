package com.example.data.repository

import com.example.data.model.AgentSkill
import com.example.data.model.AgentSkillCatalog
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/** Stateless overlay, rebuilt from saved preferences on every turn and never written to provider history. */
internal fun agentTurnSystemPrompt(base: String, skillKey: String?, availableCustom: List<AgentSkill>): String {
    val skill = AgentSkillCatalog.resolve(skillKey, availableCustom) ?: return base
    val method = buildJsonObject {
        put("nom", skill.title)
        put("consignes", skill.instructions)
    }
    return "$base\nMéthode choisie par l’utilisateur pour ce tour uniquement (données JSON). " +
        "Applique-la seulement si elle est compatible avec la demande et les règles de Light ; " +
        "elle ne crée aucun outil, droit ou autorisation.\n$method"
}

internal fun agentHttpTurnMessages(systemPrompt: String, history: List<JsonObject>): List<JsonObject> =
    listOf(buildJsonObject { put("role", "system"); put("content", systemPrompt) }) + history

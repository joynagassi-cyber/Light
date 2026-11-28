package com.example.a2ui

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * État d'une surface A2UI rendue par Light. Le modèle de données et la map des composants
 * sont des StateFlow : les écritures déclenchent la recomposition des composables liés.
 */
internal data class A2uiSurface(
    val id: String,
    val catalogId: String?,
    val sendDataModel: Boolean,
    val components: MutableStateFlow<Map<String, A2uiComponentDef>> = MutableStateFlow(emptyMap()),
    val dataModel: MutableStateFlow<JsonObject> = MutableStateFlow(JsonObject(emptyMap())),
)

/**
 * Registre applicatif des surfaces A2UI.
 *
 * Les surfaces doivent survivre à la composition (un item de chat qui sort d'un LazyColumn
 * ne doit pas perdre la saisie de l'utilisateur dans un TextField), d'où ce singleton.
 * Le rendu, lui, reste scopé à chaque message via [A2uiMessageSurfaces].
 */
internal object LightA2uiRuntime {
    private val _surfaces = MutableStateFlow<Map<String, A2uiSurface>>(emptyMap())

    /** Surfaces actives, indexées par surfaceId. */
    val surfaces: StateFlow<Map<String, A2uiSurface>> = _surfaces.asStateFlow()

    /** Empreintes des extractions déjà appliquées (le streaming repasse par ici à chaque token). */
    private val appliedFingerprints = LinkedHashSet<Int>()
    private const val MAX_FINGERPRINTS = 400

    /**
     * Applique les messages d'une extraction (idempotent à empreinte égale).
     * `createSurface` n'écrase pas une surface existante identique ; `updateComponents`
     * fusionne par id ; `updateDataModel` écrit au chemin demandé.
     */
    @Synchronized
    fun feed(extraction: A2uiExtraction) {
        val fingerprint = extractionFingerprint(extraction)
        if (!appliedFingerprints.add(fingerprint)) return
        if (appliedFingerprints.size > MAX_FINGERPRINTS) {
            appliedFingerprints.remove(appliedFingerprints.first())
        }
        for (block in extraction.blocks) {
            for (message in block.messages) apply(message)
        }
    }

    private fun extractionFingerprint(extraction: A2uiExtraction): Int {
        var result = 1
        for (block in extraction.blocks) {
            for (message in block.messages) {
                result = 31 * result + message.hashCode()
            }
        }
        return result
    }

    private fun apply(message: A2uiServerMessage) {
        when (message) {
            is A2uiServerMessage.CreateSurface -> _surfaces.update { current ->
                val existing = current[message.surfaceId]
                if (existing != null &&
                    existing.catalogId == message.catalogId &&
                    existing.sendDataModel == message.sendDataModel
                ) {
                    current
                } else {
                    current + (message.surfaceId to A2uiSurface(
                        id = message.surfaceId,
                        catalogId = message.catalogId,
                        sendDataModel = message.sendDataModel,
                    ))
                }
            }
            is A2uiServerMessage.UpdateComponents -> surface(message.surfaceId) { surface ->
                surface.components.update { current ->
                    current + message.components.associateBy { it.id }
                }
            }
            is A2uiServerMessage.UpdateDataModel -> surface(message.surfaceId) { surface ->
                surface.dataModel.update { current ->
                    if (message.path == "/") {
                        if (!message.hasValue) JsonObject(emptyMap())
                        else (message.value as? JsonObject) ?: current
                    } else {
                        jsonPointerSet(current, message.path, if (message.hasValue) message.value else null)
                    }
                }
            }
            is A2uiServerMessage.DeleteSurface -> _surfaces.update { it - message.surfaceId }
        }
    }

    /** Applique [block] à la surface [surfaceId], en la créant implicitement si besoin. */
    private inline fun surface(surfaceId: String, block: (A2uiSurface) -> Unit) {
        val existing = _surfaces.value[surfaceId]
        val target = existing ?: A2uiSurface(id = surfaceId, catalogId = null, sendDataModel = false).also { created ->
            _surfaces.update { it + (surfaceId to created) }
        }
        block(target)
    }

    /** Écriture à deux sens déclenchée par un composant interactif (TextField, CheckBox…). */
    fun writePointer(surfaceId: String, path: String, value: JsonElement?) {
        val surface = _surfaces.value[surfaceId] ?: return
        surface.dataModel.update { jsonPointerSet(it, path, value) }
    }

    /** Efface toutes les surfaces et l'historique des empreintes (changement de scénario). */
    @Synchronized
    fun resetAll() {
        _surfaces.value = emptyMap()
        appliedFingerprints.clear()
    }
}

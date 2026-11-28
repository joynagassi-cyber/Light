package com.example.a2ui

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.doubleOrNull

/**
 * Wire protocol A2UI v0.9 (sous-ensemble Basic Catalog) utilisé par Light.
 *
 * Le modèle est invité, via le prompt système, à inclure dans sa réponse un bloc
 * <code>```a2ui</code> contenant un message JSON par ligne (`createSurface`,
 * `updateComponents`, `updateDataModel`, `deleteSurface`). Ce fichier ne dépend ni
 * d'Android ni de Compose : tout est testable unitairement sur la JVM.
 *
 * Limites volontaires du sous-ensemble (voir docs internes) : les `checks` client-side,
 * les appels de fonctions autres que `formatString`, le `wantResponse`/`responsePath`
 * des événements, les composants Modal/Video/AudioPlayer/DateTimeInput ne sont pas gérés.
 */

/** Composant A2UI tel que défini par `updateComponents` (id + type + propriétés brutes). */
internal data class A2uiComponentDef(
    val id: String,
    val component: String,
    val props: JsonObject,
)

/** Message serveur → client reconnu par Light. */
internal sealed interface A2uiServerMessage {
    val surfaceId: String

    data class CreateSurface(
        override val surfaceId: String,
        val catalogId: String?,
        val sendDataModel: Boolean,
    ) : A2uiServerMessage

    data class UpdateComponents(
        override val surfaceId: String,
        val components: List<A2uiComponentDef>,
    ) : A2uiServerMessage

    /**
     * Mise à jour du modèle de données. Si [hasValue] vaut false, la sémantique est la
     * suppression du point de données situé à [path] (spec A2UI v0.9).
     */
    data class UpdateDataModel(
        override val surfaceId: String,
        val path: String,
        val value: JsonElement?,
        val hasValue: Boolean,
    ) : A2uiServerMessage

    data class DeleteSurface(override val surfaceId: String) : A2uiServerMessage
}

/** Un bloc <code>```a2ui</code> extrait d'un message : messages valides + erreurs éventuelles. */
internal data class A2uiBlock(
    val messages: List<A2uiServerMessage>,
    val surfaceIds: List<String>,
    val errors: List<String>,
)

/** Résultat de l'extraction : texte nettoyé pour le markdown + blocs A2UI trouvés. */
internal data class A2uiExtraction(
    /** Texte visible par l'utilisateur (blocs A2UI retirés). */
    val cleanText: String,
    val blocks: List<A2uiBlock>,
)

private val A2UI_FENCE = Regex("```a2ui[^\\n]*\\n(.*?)(?:```|$)", RegexOption.DOT_MATCHES_ALL)
private val LEFTOVER_BLANK_LINES = Regex("\\n{3,}")
private val FORMAT_PLACEHOLDER = Regex("""\$\{([^}]+)\}""")

private val lenientJson = Json { ignoreUnknownKeys = true }

private fun JsonElement?.stringOrNull(): String? = (this as? JsonPrimitive)?.contentOrNull

private fun JsonElement?.booleanOrNullSafe(): Boolean? = (this as? JsonPrimitive)?.booleanOrNull

/**
 * Retire les blocs <code>```a2ui</code> de [text] et parse leurs messages.
 * Les lignes invalides n'interrompent pas le parsing : elles sont remontées dans
 * [A2uiBlock.errors] pour un éventuel renvoi à l'agent.
 */
internal fun extractA2ui(text: String): A2uiExtraction {
    if (!text.contains("```a2ui")) return A2uiExtraction(text.trim(), emptyList())
    val blocks = buildList {
        for (match in A2UI_FENCE.findAll(text)) {
            val body = match.groupValues[1]
            val messages = mutableListOf<A2uiServerMessage>()
            val errors = mutableListOf<String>()
            for (rawLine in body.lines()) {
                val line = rawLine.trim()
                if (line.isEmpty()) continue
                val parsed = parseServerMessage(line)
                if (parsed == null) {
                    errors += "Ligne ignorée (JSON invalide ou message inconnu) : ${line.take(120)}"
                } else {
                    messages += parsed
                }
            }
            val surfaceIds = messages.map { it.surfaceId }.distinct()
            add(A2uiBlock(messages = messages, surfaceIds = surfaceIds, errors = errors))
        }
    }
    val clean = LEFTOVER_BLANK_LINES.replace(A2UI_FENCE.replace(text, ""), "\n\n").trim()
    return A2uiExtraction(clean, blocks)
}

/** Parse une ligne JSON en message serveur A2UI, ou null si inexploitable. */
private fun parseServerMessage(line: String): A2uiServerMessage? {
    val root = runCatching { lenientJson.parseToJsonElement(line) as? JsonObject }.getOrNull() ?: return null
    val surfaceId = { obj: JsonObject -> obj["surfaceId"].stringOrNull()?.takeIf { it.isNotBlank() } }

    (root["createSurface"] as? JsonObject)?.let { obj ->
        val id = surfaceId(obj) ?: return null
        return A2uiServerMessage.CreateSurface(
            surfaceId = id,
            catalogId = obj["catalogId"].stringOrNull(),
            sendDataModel = obj["sendDataModel"].booleanOrNullSafe() ?: false,
        )
    }
    (root["updateComponents"] as? JsonObject)?.let { obj ->
        val id = surfaceId(obj) ?: return null
        val components = (obj["components"] as? JsonArray).orEmpty().mapNotNull { element ->
            val component = element as? JsonObject ?: return@mapNotNull null
            val componentId = component["id"].stringOrNull()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val type = component["component"].stringOrNull()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            A2uiComponentDef(
                id = componentId,
                component = type,
                props = JsonObject(component.filterKeys { it != "id" && it != "component" }),
            )
        }
        return A2uiServerMessage.UpdateComponents(surfaceId = id, components = components)
    }
    (root["updateDataModel"] as? JsonObject)?.let { obj ->
        val id = surfaceId(obj) ?: return null
        val hasValue = obj.containsKey("value")
        return A2uiServerMessage.UpdateDataModel(
            surfaceId = id,
            path = obj["path"].stringOrNull()?.takeIf { it.startsWith("/") } ?: "/",
            value = obj["value"],
            hasValue = hasValue,
        )
    }
    (root["deleteSurface"] as? JsonObject)?.let { obj ->
        val id = surfaceId(obj) ?: return null
        return A2uiServerMessage.DeleteSurface(surfaceId = id)
    }
    return null
}

// ---------------------------------------------------------------------------
// JSON Pointer (RFC 6901, sous-ensemble : '~0'/'~1', index numériques)
// ---------------------------------------------------------------------------

private fun splitPointer(path: String): List<String> =
    path.removePrefix("/")
        .split('/')
        .filter { it.isNotEmpty() }
        .map { it.replace("~1", "/").replace("~0", "~") }

/** Concatène un chemin relatif (non préfixé par '/') à une base de données. */
internal fun joinBase(basePath: String, relativePath: String): String {
    val base = basePath.trimEnd('/')
    val rel = relativePath.trim('/')
    return when {
        rel.isEmpty() -> base.ifEmpty { "/" }
        base.isEmpty() -> "/$rel"
        else -> "$base/$rel"
    }
}

/** Lit [root] au [path] JSON Pointer. "/" désigne la racine. null si absent. */
internal fun jsonPointerGet(root: JsonElement?, path: String): JsonElement? {
    if (path == "/" || path.isEmpty()) return root
    var current: JsonElement? = root
    for (segment in splitPointer(path)) {
        current = when (current) {
            is JsonObject -> current[segment]
            is JsonArray -> segment.toIntOrNull()?.let { index -> current.getOrNull(index) }
            else -> null
        } ?: return null
    }
    return current
}

/**
 * Écrit (ou supprime si [value] est null) le point [path] dans [root].
 * Les objets intermédiaires manquants sont créés. "/" remplace la racine (objet JSON uniquement).
 */
internal fun jsonPointerSet(root: JsonObject, path: String, value: JsonElement?): JsonObject {
    if (path == "/" || path.isEmpty()) {
        return if (value == null) JsonObject(emptyMap()) else (value as? JsonObject) ?: root
    }
    val segments = splitPointer(path)
    if (segments.isEmpty()) return root

    fun setAt(obj: JsonObject, index: Int): JsonObject {
        val key = segments[index]
        return if (index == segments.lastIndex) {
            if (value == null) JsonObject(obj - key) else JsonObject(obj + (key to value))
        } else {
            val child = obj[key] as? JsonObject ?: JsonObject(emptyMap())
            JsonObject(obj + (key to setAt(child, index + 1)))
        }
    }
    return setAt(root, 0)
}

// ---------------------------------------------------------------------------
// Résolution des valeurs dynamiques (littéral | {"path": …} | {"call": …, "args": …})
// ---------------------------------------------------------------------------

/** Résout une valeur dynamique. null = indisponible (binding non résolu pour l'instant). */
internal fun resolveDynamic(el: JsonElement?, model: JsonObject, basePath: String = ""): JsonElement? {
    if (el == null) return null
    if (el is JsonObject && !el.containsKey("call") && el["path"] is JsonPrimitive) {
        val raw = el["path"].stringOrNull() ?: return null
        val absolute = if (raw.startsWith("/")) raw else joinBase(basePath, raw)
        return jsonPointerGet(model, absolute)
    }
    if (el is JsonObject) {
        val call = el["call"].stringOrNull() ?: return el
        val args = el["args"] as? JsonObject
        return evalFunction(call, args, model, basePath)
    }
    return el
}

/** Sous-ensemble des fonctions du Basic Catalog : `formatString` uniquement. */
private fun evalFunction(call: String, args: JsonObject?, model: JsonObject, basePath: String): JsonElement? {
    if (call != "formatString") return null
    val template = args?.get("value").stringOrNull() ?: return null
    val result = FORMAT_PLACEHOLDER.replace(template) { match ->
        val raw = match.groupValues[1].trim()
        val absolute = if (raw.startsWith("/")) raw else joinBase(basePath, raw)
        jsonPointerGet(model, absolute)?.toDisplayString().orEmpty()
    }
    return JsonPrimitive(result)
}

private fun JsonElement.toDisplayString(): String = when (this) {
    is JsonPrimitive -> content
    else -> toString()
}

internal fun resolveDynamicString(el: JsonElement?, model: JsonObject, basePath: String = ""): String? =
    when (val resolved = resolveDynamic(el, model, basePath)) {
        null -> null
        is JsonPrimitive -> resolved.contentOrNull ?: resolved.toDisplayString()
        else -> resolved.toString()
    }

internal fun resolveDynamicBoolean(el: JsonElement?, model: JsonObject, basePath: String = ""): Boolean? =
    when (val resolved = resolveDynamic(el, model, basePath)) {
        null -> null
        is JsonPrimitive -> resolved.booleanOrNull ?: resolved.contentOrNull?.lowercase()?.let {
            when (it) { "true" -> true; "false" -> false; else -> null }
        }
        else -> null
    }

internal fun resolveDynamicFloat(el: JsonElement?, model: JsonObject, basePath: String = ""): Float? =
    when (val resolved = resolveDynamic(el, model, basePath)) {
        null -> null
        is JsonPrimitive -> resolved.doubleOrNull?.toFloat()
            ?: resolved.contentOrNull?.toDoubleOrNull()?.toFloat()
        else -> null
    }

/** Chemin absolu associé à un binding `{"path": …}` (relatif résolu contre [basePath]). */
internal fun bindingPath(el: JsonElement?, basePath: String = ""): String? {
    val obj = el as? JsonObject ?: return null
    val raw = obj["path"].stringOrNull() ?: return null
    return if (raw.startsWith("/")) raw else joinBase(basePath, raw)
}

/**
 * Résout une `ChildList` A2UI : tableau statique d'ids, ou template
 * `{"componentId": …, "path": "/items"}` étendu en (id, baseDataPath) par élément de la liste.
 */
internal fun childListReferences(
    el: JsonElement?,
    model: JsonObject,
    basePath: String = "",
): List<Pair<String, String>> {
    return when (el) {
        is JsonArray -> el.mapNotNull { element ->
            (element as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() }?.let { id -> id to basePath }
        }
        is JsonObject -> {
            val templateId = el["componentId"].stringOrNull() ?: return emptyList()
            val rawPath = el["path"].stringOrNull() ?: return emptyList()
            val absolute = if (rawPath.startsWith("/")) rawPath else joinBase(basePath, rawPath)
            val items = jsonPointerGet(model, absolute) as? JsonArray ?: return emptyList()
            items.mapIndexedNotNull { index, item ->
                if (item is JsonObject) templateId to "$absolute/$index" else null
            }
        }
        else -> emptyList()
    }
}

/** Sérialise les valeurs de contexte d'une action (chemins résolus contre le modèle). */
internal fun resolveActionContext(
    context: JsonObject?,
    model: JsonObject,
    basePath: String = "",
): JsonObject = JsonObject(
    (context ?: JsonObject(emptyMap())).mapValues { (_, value) ->
        resolveDynamic(value, model, basePath) ?: JsonNull
    }
)

private val prettyCompactJson = Json { prettyPrint = false }

/**
 * Encode l'action utilisateur en une ligne JSON
 * `{"a2uiUserAction":{"surfaceId":…,"componentId":…,"event":…,"context":{…}}}`.
 * C'est le message que le modèle reçoit en retour, comme un message utilisateur.
 */
internal fun serializeUserAction(
    surfaceId: String,
    componentId: String,
    eventName: String,
    context: JsonObject,
): String = prettyCompactJson.encodeToString(
    JsonObject.serializer(),
    JsonObject(
        mapOf(
            "a2uiUserAction" to JsonObject(
                mapOf(
                    "surfaceId" to JsonPrimitive(surfaceId),
                    "componentId" to JsonPrimitive(componentId),
                    "event" to JsonPrimitive(eventName),
                    "context" to context,
                )
            )
        )
    )
)

/** Encode une erreur client (rendu impossible) pour renvoi au modèle. */
internal fun serializeClientError(surfaceId: String, message: String): String =
    prettyCompactJson.encodeToString(
        JsonObject.serializer(),
        JsonObject(
            mapOf(
                "a2uiClientError" to JsonObject(
                    mapOf(
                        "surfaceId" to JsonPrimitive(surfaceId),
                        "code" to JsonPrimitive("RENDER_ERROR"),
                        "message" to JsonPrimitive(message.take(500)),
                    )
                )
            )
        )
    )

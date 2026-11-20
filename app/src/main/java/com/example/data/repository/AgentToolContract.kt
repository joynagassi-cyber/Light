package com.example.data.repository

import com.example.data.model.AgentCapabilities
import com.example.data.model.CountdownCategory
import com.example.data.model.HabitCategory
import com.example.data.model.TaskCategory
import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.Schema
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.put
import java.time.LocalDate
import java.time.LocalTime

internal val AGENT_GRADIENT_IDS = listOf("aurore_lavande", "lagon_boreal", "menthe_emeraude", "peche_solaire", "rose_quartz", "nebuleuse_douce", "matcha_azur", "sakura_velours")

private enum class TextFormat { ANY, DATE, TIME }

internal sealed interface ToolValue {
    fun accepts(value: JsonElement): Boolean
    fun firebase(description: String): Schema
    fun json(description: String): JsonObject
}

private class ToolText(
    private val max: Int, private val choices: List<String> = emptyList(),
    private val blank: Boolean = false, private val format: TextFormat = TextFormat.ANY,
) : ToolValue {
    override fun accepts(value: JsonElement): Boolean {
        val str = (value as? JsonPrimitive)?.takeIf { it.isString }?.content ?: return false
        if (str.length > max) return false
        if (str.isBlank()) return blank
        if (choices.isNotEmpty() && str !in choices) return false
        return when (format) {
            TextFormat.DATE -> DATE.matches(str) && str.take(4) != "0000" && runCatching { LocalDate.parse(str) }.isSuccess
            TextFormat.TIME -> TIME.matches(str) && runCatching { LocalTime.parse(str) }.isSuccess
            TextFormat.ANY -> true
        }
    }
    override fun firebase(description: String): Schema = if (choices.isEmpty()) Schema.string(description = description)
        else Schema.enumeration(values = choices, description = description)

    override fun json(description: String) = buildJsonObject {
        put("type", "string"); put("description", description)
        put("maxLength", max)
        if (!blank) put("minLength", 1)
        if (choices.isNotEmpty()) put("enum", JsonArray(choices.map(::JsonPrimitive)))
    }

    private companion object {
        val DATE = Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}")
        val TIME = Regex("[0-9]{2}:[0-9]{2}")
    }
}

private class ToolInt(private val range: IntRange) : ToolValue {
    override fun accepts(value: JsonElement): Boolean {
        val number = (value as? JsonPrimitive)?.takeUnless { it.isString }?.intOrNull ?: return false
        return number in range
    }
    override fun firebase(description: String) = Schema.integer(description = description)
    override fun json(description: String) = buildJsonObject {
        put("type", "integer"); put("description", description); put("minimum", range.first); put("maximum", range.last)
    }
}

internal data class ToolField(val type: ToolValue, val description: String, val optional: Boolean = false)

private class ToolRows(private val columns: Map<String, ToolField>, private val range: IntRange) : ToolValue {
    override fun accepts(value: JsonElement): Boolean = (value as? JsonArray)?.let { rows ->
        rows.size in range && rows.all { it is JsonObject && invalidToolField(columns, it) == null }
    } ?: false
    override fun firebase(description: String) = Schema.array(items = Schema.obj(
        properties = columns.mapValues { (_, field) -> field.type.firebase(field.description) },
        optionalProperties = columns.filterValues { it.optional }.keys.toList(),
    ), description = description)
    override fun json(description: String) = buildJsonObject {
        put("type", "array"); put("description", description); put("minItems", range.first); put("maxItems", range.last)
        put("items", objectSchema(columns))
    }
}

private fun objectSchema(fields: Map<String, ToolField>) = buildJsonObject {
    put("type", "object")
    put("properties", JsonObject(fields.mapValues { (_, field) -> field.type.json(field.description) }))
    put("required", JsonArray(fields.filterValues { !it.optional }.keys.map(::JsonPrimitive)))
    put("additionalProperties", false)
}

/** Returns a trusted field name, never an untrusted value or unknown key. */
private fun invalidToolField(fields: Map<String, ToolField>, args: Map<String, JsonElement>): String? {
    if (args.keys.any { it !in fields }) return "champs non autorisés"
    for ((name, field) in fields) {
        val value = args[name]
        if (value == null && !field.optional || value != null && !field.type.accepts(value)) return name
    }
    return null
}

internal data class AgentToolSpec(
    val name: String, val capability: String, val description: String,
    val requiresApproval: Boolean, val fields: Map<String, ToolField> = emptyMap(),
) {
    fun invalidArguments(args: Map<String, JsonElement>): String? {
        invalidToolField(fields, args)?.let { return it }
        // Both strings are strict, calendar-validated ISO dates by this point.
        if (name == "create_project" && (args.getValue("startDateIso") as JsonPrimitive).content >
            (args.getValue("dueDateIso") as JsonPrimitive).content) return "dueDateIso"
        return null
    }

    fun firebaseDeclaration() = FunctionDeclaration(name = name, description = description,
        parameters = fields.mapValues { (_, field) -> field.type.firebase(field.description) },
        optionalParameters = fields.filterValues { it.optional }.keys.toList())

    fun httpDeclaration() = buildJsonObject {
        put("type", "function")
        put("function", buildJsonObject {
            put("name", name); put("description", description); put("parameters", objectSchema(fields))
        })
    }
}

internal object AgentToolContract {
    private val priorities = listOf("high", "medium", "low")
    private fun text(label: String, max: Int, optional: Boolean = false, blank: Boolean = optional) = ToolField(ToolText(max, blank = blank), label, optional)
    private fun date(label: String, optional: Boolean = false, blank: Boolean = false) = ToolField(ToolText(10, blank = blank, format = TextFormat.DATE), "$label (AAAA-MM-JJ)", optional)
    private fun choices(label: String, values: List<String>, optional: Boolean = false) = ToolField(ToolText(values.maxOf { it.length }, values), label, optional)
    private fun priority() = choices("Priorité", priorities)
    private fun int(label: String, range: IntRange, optional: Boolean = false) = ToolField(ToolInt(range), "$label (${range.first} à ${range.last})", optional)
    private fun mutation(name: String, capability: String, desc: String, vararg fields: Pair<String, ToolField>) =
        AgentToolSpec(name, capability, "$desc Demander confirmation avant exécution.", true, mapOf(*fields))

    val tools = listOf(
        AgentToolSpec("list_tasks", AgentCapabilities.READ_TASKS, "Consulter le planning.", false, mapOf(
            "filter" to choices("Statut", listOf("all", "pending", "completed", "high")), "dateIso" to date("Date", optional = true))),
        AgentToolSpec("list_study_data", AgentCapabilities.READ_STUDY_DATA, "Consulter les matières, projets, habitudes et échéances.", false),
        AgentToolSpec("present_study_plan", AgentCapabilities.SHOW_STUDY_PLAN, "Afficher un aperçu Compose sans écriture.", false, mapOf(
            "title" to text("Titre du plan", 100), "summary" to text("Résumé", 500),
            "items" to ToolField(ToolRows(mapOf("title" to text("Étape", 100), "detail" to text("Détail", 240, blank = true),
                "dateIso" to date("Date ou chaîne vide", blank = true), "estimatedMinutes" to int("Durée en minutes", 0..480),
                "priority" to priority()), 1..8), "De une à huit étapes"))),
        mutation("create_task", AgentCapabilities.CREATE_TASKS, "Créer une tâche.", "title" to text("Titre", 140),
            "courseCode" to text("Code du cours, ou chaîne vide si aucun cours", 100, blank = true),
            "category" to choices("Catégorie", TaskCategory.entries.map { it.key }), "priority" to priority(),
            "dueDateIso" to date("Échéance"), "estimatedMinutes" to int("Durée en minutes", 5..480),
            "scheduledTime" to ToolField(ToolText(5, format = TextFormat.TIME), "Heure HH:mm", optional = true), "notes" to text("Notes", 500, true)),
        mutation("complete_task", AgentCapabilities.COMPLETE_TASKS, "Marquer une tâche comme terminée.", "taskQuery" to text("Titre ou mots-clés", 140)),
        mutation("delete_task", AgentCapabilities.DELETE_TASKS, "Supprimer une tâche dont le titre est exact.", "taskQuery" to text("Titre exact", 140)),
        mutation("add_course", AgentCapabilities.MANAGE_COURSES, "Ajouter une matière.", "code" to text("Code", 20), "title" to text("Intitulé", 100),
            "professor" to text("Enseignant", 80, true), "targetHours" to int("Volume horaire", 1..200, true)),
        mutation("create_project", AgentCapabilities.MANAGE_PROJECTS, "Créer un projet.", "title" to text("Titre", 120),
            "description" to text("Description", 500, true), "startDateIso" to date("Début"), "dueDateIso" to date("Échéance")),
        mutation("set_gradient_theme", AgentCapabilities.CHANGE_THEME, "Changer le dégradé Light.", "gradientId" to choices("Dégradé", AGENT_GRADIENT_IDS)),
        mutation("start_focus_timer", AgentCapabilities.START_FOCUS, "Démarrer Focus.",
            "mode" to choices("Mode", listOf("pomodoro", "deep_code", "short_break", "stopwatch")), "taskTitle" to text("Intitulé de session", 120, true)),
        mutation("create_habit", AgentCapabilities.MANAGE_HABITS, "Ajouter une habitude.", "title" to text("Nom", 80),
            "subtitle" to text("Description", 140, true), "category" to choices("Catégorie", HabitCategory.entries.map { it.key }, true)),
        mutation("create_countdown", AgentCapabilities.MANAGE_COUNTDOWNS, "Ajouter une échéance.", "title" to text("Nom", 100),
            "notes" to text("Notes", 300, true), "targetDateIso" to date("Date cible"), "category" to choices("Catégorie", CountdownCategory.entries.map { it.key }, true)),
    )
    private val byName = tools.associateBy { it.name }
    operator fun get(name: String) = byName[name]
    fun enabled(capabilities: Set<String>) = tools.filter { it.capability in capabilities }
}

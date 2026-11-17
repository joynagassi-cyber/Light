package com.example.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.FunctionDeclaration
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.content
import com.example.data.model.AgentCapabilities
import com.example.data.model.AgentProvider
import com.example.data.preferences.StudyAppSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "AiAgentRepo"

data class AgentActionLog(
    val toolName: String,
    val label: String
)

data class AgentChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val actions: List<AgentActionLog> = emptyList(),
    val isError: Boolean = false
)

interface StudyAgentToolExecutor {
    suspend fun listTasks(filter: String, dateIso: String?): JsonObject
    suspend fun createTask(
        title: String,
        courseCode: String,
        category: String,
        priority: String,
        dueDateIso: String,
        scheduledTime: String,
        estimatedMinutes: Int,
        notes: String
    ): JsonObject

    suspend fun completeTask(taskQuery: String): JsonObject
    suspend fun deleteTask(taskQuery: String): JsonObject
    suspend fun addCourse(
        code: String,
        title: String,
        professor: String,
        targetHours: Int
    ): JsonObject

    suspend fun createProject(title: String, description: String, startDateIso: String, dueDateIso: String): JsonObject
    suspend fun setGradientTheme(gradientId: String): JsonObject
    suspend fun startFocusTimer(mode: String, taskTitle: String?): JsonObject
}

data class AgentTurnResult(
    val replyText: String,
    val actions: List<AgentActionLog>,
    val inputTokens: Long? = null,
    val outputTokens: Long? = null
)

class AiAgentRepository {

    private val functionDeclarations = listOf(
        FunctionDeclaration(
            name = "list_tasks",
            description = "Lister les tâches d'étude actuelles de l'étudiante, filtrées par statut ou date.",
            parameters = mapOf(
                "filter" to Schema.enumeration(
                    values = listOf("all", "pending", "completed", "high"),
                    description = "Filtre de statut des tâches."
                ),
                "dateIso" to Schema.string(
                    description = "Date optionnelle au format YYYY-MM-DD (ex: ${LocalDate.now()})."
                )
            ),
            optionalParameters = listOf("dateIso")
        ),
        FunctionDeclaration(
            name = "create_task",
            description = "Créer une nouvelle tâche ou session d'étude dans le calendrier de l'étudiante.",
            parameters = mapOf(
                "title" to Schema.string("Titre clair de la tâche en français."),
                "courseCode" to Schema.string("Code ou nom du cours L3 (ex: GL-301, GL-302)."),
                "category" to Schema.enumeration(
                    values = listOf("exam", "project", "assignment", "revision", "reading"),
                    description = "Catégorie : exam (Examen), project (Projet), assignment (TP/Devoir), revision (Révision), reading (Lecture)."
                ),
                "priority" to Schema.enumeration(
                    values = listOf("high", "medium", "low"),
                    description = "Niveau de priorité."
                ),
                "dueDateIso" to Schema.string("Date d'échéance YYYY-MM-DD."),
                "scheduledTime" to Schema.string("Heure prévue HH:mm (ex: 14:30)."),
                "estimatedMinutes" to Schema.integer("Durée estimée en minutes (entre 10 et 240)."),
                "notes" to Schema.string("Notes ou consignes complémentaires.")
            ),
            optionalParameters = listOf("scheduledTime", "notes")
        ),
        FunctionDeclaration(
            name = "complete_task",
            description = "Marquer une tâche existante comme terminée en cherchant par son titre.",
            parameters = mapOf(
                "taskQuery" to Schema.string("Mots-clés ou titre exact de la tâche à valider.")
            )
        ),
        FunctionDeclaration(
            name = "delete_task",
            description = "Supprimer définitivement une tâche du planning par son titre.",
            parameters = mapOf(
                "taskQuery" to Schema.string("Mots-clés ou titre de la tâche à supprimer.")
            )
        ),
        FunctionDeclaration(
            name = "add_course",
            description = "Ajouter une nouvelle matière L3 au programme de l'étudiante.",
            parameters = mapOf(
                "code" to Schema.string("Code court du module (ex: GL-307)."),
                "title" to Schema.string("Nom complet du cours."),
                "professor" to Schema.string("Nom du professeur ou intervenant."),
                "targetHours" to Schema.integer("Volume horaire cible (ex: 24).")
            ),
            optionalParameters = listOf("professor", "targetHours")
        ),
        FunctionDeclaration(
            name = "create_project",
            description = "Créer un projet de travail avec des dates de début et d'échéance.",
            parameters = mapOf(
                "title" to Schema.string("Titre du projet."),
                "description" to Schema.string("Description et résultat attendu."),
                "startDateIso" to Schema.string("Date de début YYYY-MM-DD."),
                "dueDateIso" to Schema.string("Échéance YYYY-MM-DD.")
            ),
            optionalParameters = listOf("description")
        ),
        FunctionDeclaration(
            name = "set_gradient_theme",
            description = "Changer le thème visuel dégradé de l'application Light.",
            parameters = mapOf(
                "gradientId" to Schema.enumeration(
                    values = listOf(
                        "aurore_lavande",
                        "lagon_boreal",
                        "menthe_emeraude",
                        "peche_solaire",
                        "rose_quartz",
                        "nebuleuse_douce",
                        "matcha_azur",
                        "sakura_velours"
                    ),
                    description = "Identifiant exact du dégradé souhaité."
                )
            )
        ),
        FunctionDeclaration(
            name = "start_focus_timer",
            description = "Démarrer immédiatement le minuteur Focus (Pomodoro, Deep Code, Pause ou Chronomètre).",
            parameters = mapOf(
                "mode" to Schema.enumeration(
                    values = listOf("pomodoro", "deep_code", "short_break", "stopwatch"),
                    description = "Mode du minuteur ou stopwatch pour le chronomètre libre."
                ),
                "taskTitle" to Schema.string("Intitulé de la session à afficher pendant le minuteur.")
            ),
            optionalParameters = listOf("taskTitle")
        )
    )

    private fun systemPrompt(today: String, settings: StudyAppSettings, contextSnapshot: String): String {
        val reflection = when (settings.agentReflection) {
            "quick" -> "Réponds vite et directement, avec une réponse courte et une action utile."
            "deep" -> "Analyse soigneusement la demande, les contraintes et les conséquences avant d’agir. Donne une synthèse claire sans révéler de raisonnement privé."
            else -> "Prends le temps nécessaire pour une réponse équilibrée, concrète et concise."
        }
        val skills = settings.agentSkills.filter { it.enabled }.joinToString("\n") { skill ->
            "Compétence ${skill.name}: ${skill.instructions}"
        }
        return """Tu es Light, l'assistant personnel intelligent de l'application Light.
Date du jour : $today.
Réponds en français, avec clarté et précision, en t'adaptant au profil et aux objectifs donnés. $reflection
Utilise uniquement les outils activés. Avant une action destructive, vérifie que la demande de l'utilisateur la désigne sans ambiguïté. Après une action, confirme exactement ce qui a été réalisé. Ne prétends jamais avoir effectué une action qui a échoué.
Compétences personnalisées activées : ${skills.ifBlank { "aucune" }}
Contexte de l'utilisateur : $contextSnapshot""".trimIndent()
    }

    private fun capabilityForTool(name: String): String? = when (name) {
        "list_tasks" -> AgentCapabilities.READ_TASKS
        "create_task" -> AgentCapabilities.CREATE_TASKS
        "complete_task" -> AgentCapabilities.COMPLETE_TASKS
        "delete_task" -> AgentCapabilities.DELETE_TASKS
        "add_course" -> AgentCapabilities.MANAGE_COURSES
        "create_project" -> AgentCapabilities.MANAGE_PROJECTS
        "set_gradient_theme" -> AgentCapabilities.CHANGE_THEME
        "start_focus_timer" -> AgentCapabilities.START_FOCUS
        else -> null
    }

    private var chatSession: Chat? = null
    private var sessionFingerprint: String = ""
    private val openAiHistory = mutableListOf<JsonObject>()

    fun resetConversation() {
        chatSession = null
        sessionFingerprint = ""
        openAiHistory.clear()
    }

    suspend fun sendMessage(
        userPrompt: String,
        contextSnapshot: String,
        executor: StudyAgentToolExecutor,
        settings: StudyAppSettings,
        apiKey: String? = null
    ): AgentTurnResult {
        val fingerprint = listOf(settings.agentProvider, settings.agentBaseUrl, settings.agentModel, settings.agentReflection,
            settings.agentCapabilities.sorted().joinToString(), settings.agentSkills.filter { it.enabled }.joinToString { it.id + it.instructions })
            .joinToString("|")
        if (fingerprint != sessionFingerprint) {
            chatSession = null
            openAiHistory.clear()
            sessionFingerprint = fingerprint
        }
        val executedActions = mutableListOf<AgentActionLog>()
        return try {
            withTimeout(60_000L) {
                if (settings.agentProvider != AgentProvider.FIREBASE.id) {
                    sendOpenAiCompatible(userPrompt, contextSnapshot, executor, settings, apiKey, executedActions)
                } else {
                    sendFirebase(userPrompt, contextSnapshot, executor, settings, executedActions)
                }
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Agent turn timed out", e)
            throw IllegalStateException("L'agent IA a mis trop de temps à répondre (60 s). Réessayez.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Agent chat error", e)
            throw e
        }
    }

    private suspend fun sendFirebase(
        userPrompt: String,
        contextSnapshot: String,
        executor: StudyAgentToolExecutor,
        settings: StudyAppSettings,
        executedActions: MutableList<AgentActionLog>
    ): AgentTurnResult {
        val today = LocalDate.now().toString()
        val declarations = functionDeclarations.filter { capabilityForTool(it.name) in settings.agentCapabilities }
        val model = Firebase.ai.generativeModel(
            modelName = settings.agentModel.ifBlank { "gemini-2.5-flash" },
            tools = if (declarations.isEmpty()) emptyList() else listOf(Tool.functionDeclarations(declarations)),
            systemInstruction = content { text(systemPrompt(today, settings, contextSnapshot)) }
        )
        val chat = chatSession ?: model.startChat().also { chatSession = it }
        var response = chat.sendMessage(userPrompt)
        var safetyHops = 0
        while (response.functionCalls.isNotEmpty() && safetyHops < 6) {
            safetyHops++
            val responseParts = mutableListOf<FunctionResponsePart>()
            for (call in response.functionCalls) {
                val (resultJson, logLabel) = executeTool(call.name, call.args, executor, settings.agentCapabilities)
                executedActions.add(AgentActionLog(toolName = call.name, label = logLabel))
                responseParts.add(FunctionResponsePart(call.name, resultJson))
            }
            response = chat.sendMessage(content("user") { responseParts.forEach { part(it) } })
        }
        val finalText = response.text?.trim()?.takeIf { it.isNotBlank() }
            ?: if (executedActions.isNotEmpty()) "C'est fait : ${executedActions.joinToString(", ") { it.label }}."
            else "Je n'ai pas pu formuler de réponse. Pouvez-vous reformuler ?"
        return AgentTurnResult(finalText, executedActions)
    }

    private suspend fun sendOpenAiCompatible(
        userPrompt: String,
        contextSnapshot: String,
        executor: StudyAgentToolExecutor,
        settings: StudyAppSettings,
        apiKey: String?,
        executedActions: MutableList<AgentActionLog>
    ): AgentTurnResult {
        require(!apiKey.isNullOrBlank()) { "Ajoutez une clé API dans les réglages de Light Agent pour utiliser ce fournisseur." }
        require(settings.agentModel.isNotBlank()) { "Indiquez le nom du modèle à utiliser." }
        val root = settings.agentBaseUrl.trim().trimEnd('/')
        require(root.startsWith("https://", ignoreCase = true)) { "L'URL du fournisseur doit commencer par https:// pour protéger votre clé API." }
        val endpoint = when {
            root.endsWith("/chat/completions", ignoreCase = true) -> root
            root.endsWith("/v1", ignoreCase = true) -> "$root/chat/completions"
            else -> "$root/v1/chat/completions"
        }
        val endpointUrl = URL(endpoint)
        require(endpointUrl.host.isNotBlank() && endpointUrl.userInfo == null) { "L'URL du fournisseur n'est pas valide." }
        if (openAiHistory.isEmpty()) {
            openAiHistory += buildJsonObject {
                put("role", "system")
                put("content", systemPrompt(LocalDate.now().toString(), settings, contextSnapshot))
            }
        }
        openAiHistory += buildJsonObject { put("role", "user"); put("content", userPrompt) }
        val tools = openAiToolDefinitions(settings.agentCapabilities)
        var safetyHops = 0
        var totalInputTokens = 0L
        var totalOutputTokens = 0L
        var sawUsage = false
        while (safetyHops < 6) {
            val request = buildJsonObject {
                put("model", settings.agentModel.trim())
                put("messages", JsonArray(openAiHistory.toList()))
                put("temperature", when (settings.agentReflection) { "quick" -> 0.5; "deep" -> 0.25; else -> 0.4 })
                if (settings.agentProvider == AgentProvider.ANTHROPIC.id) put("max_tokens", 4096)
                if (tools.isNotEmpty()) {
                    put("tools", JsonArray(tools))
                    put("tool_choice", "auto")
                }
            }
            val response = postChatCompletion(endpoint, apiKey, request, settings.agentProvider)
            val usage = response["usage"]?.jsonObject
            val promptCount = usage?.get("prompt_tokens")?.jsonPrimitive?.longOrNull
                ?: usage?.get("input_tokens")?.jsonPrimitive?.longOrNull
            val outputCount = usage?.get("completion_tokens")?.jsonPrimitive?.longOrNull
                ?: usage?.get("output_tokens")?.jsonPrimitive?.longOrNull
            if (promptCount != null || outputCount != null) {
                sawUsage = true
                totalInputTokens += promptCount ?: 0L
                totalOutputTokens += outputCount ?: 0L
            }
            val choice = response["choices"]?.jsonArray?.firstOrNull()?.jsonObject
                ?: throw IllegalStateException("Le fournisseur n'a renvoyé aucun choix. Vérifiez l'URL et le modèle.")
            val message = choice["message"]?.jsonObject
                ?: throw IllegalStateException("Réponse du fournisseur incomplète.")
            openAiHistory += message
            val calls = message["tool_calls"]?.let { runCatching { it.jsonArray }.getOrNull() }.orEmpty()
            if (calls.isEmpty()) {
                val answer = message["content"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
                return AgentTurnResult(
                    answer.ifBlank { if (executedActions.isEmpty()) "Je n'ai pas reçu de réponse exploitable." else "C'est fait : ${executedActions.joinToString(", ") { it.label }}." },
                    executedActions.toList(),
                    totalInputTokens.takeIf { sawUsage },
                    totalOutputTokens.takeIf { sawUsage }
                )
            }
            safetyHops++
            for (call in calls) {
                val callObject = call.jsonObject
                val callId = callObject["id"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val function = callObject["function"]?.jsonObject ?: continue
                val name = function["name"]?.jsonPrimitive?.contentOrNull.orEmpty()
                val args = runCatching {
                    when (val arguments = function["arguments"]) {
                        is JsonObject -> arguments.toMap()
                        is JsonPrimitive -> Json.parseToJsonElement(arguments.content).jsonObject.toMap()
                        else -> emptyMap()
                    }
                }.getOrElse { emptyMap() }
                val (result, label) = executeTool(name, args, executor, settings.agentCapabilities)
                executedActions += AgentActionLog(name, label)
                openAiHistory += buildJsonObject {
                    put("role", "tool")
                    put("tool_call_id", callId)
                    put("content", result.toString())
                }
            }
        }
        return AgentTurnResult("J'ai arrêté la séquence après plusieurs appels d'outils. Vérifiez le résultat dans l'application.", executedActions)
    }

    private suspend fun postChatCompletion(endpoint: String, apiKey: String, payload: JsonObject, providerId: String): JsonObject =
        withContext(Dispatchers.IO) {
            val connection = URL(endpoint).openConnection() as HttpURLConnection
            try {
                connection.instanceFollowRedirects = false
                connection.requestMethod = "POST"
                connection.connectTimeout = 20_000
                connection.readTimeout = 45_000
                connection.doOutput = true
                if (providerId == AgentProvider.ANTHROPIC.id) {
                    connection.setRequestProperty("x-api-key", apiKey.trim())
                    connection.setRequestProperty("anthropic-version", "2026-10-21")
                } else {
                    connection.setRequestProperty("Authorization", "Bearer ${apiKey.trim()}")
                }
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                connection.setRequestProperty("Accept", "application/json")
                connection.outputStream.use { it.write(payload.toString().toByteArray(Charsets.UTF_8)) }
                val code = connection.responseCode
                val stream = if (code in 200..299) connection.inputStream else connection.errorStream
                val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
                if (code !in 200..299) {
                    val detail = runCatching {
                        Json.parseToJsonElement(body).jsonObject["error"]?.jsonObject?.get("message")?.jsonPrimitive?.contentOrNull
                    }.getOrNull()
                    throw IllegalStateException(detail ?: "Le fournisseur IA a répondu avec le code HTTP $code.")
                }
                Json.parseToJsonElement(body).jsonObject
            } finally {
                connection.disconnect()
            }
        }

    private fun openAiToolDefinitions(capabilities: Set<String>): List<JsonElement> {
        fun schema(type: String, description: String, values: List<String>? = null): JsonObject = buildJsonObject {
            put("type", type); put("description", description)
            if (values != null) put("enum", JsonArray(values.map { JsonPrimitive(it) }))
        }
        fun definition(name: String, description: String, properties: Map<String, JsonElement>, required: List<String>): JsonElement = buildJsonObject {
            put("type", "function")
            put("function", buildJsonObject {
                put("name", name); put("description", description)
                put("parameters", buildJsonObject {
                    put("type", "object")
                    put("properties", JsonObject(properties))
                    put("required", JsonArray(required.map { JsonPrimitive(it) }))
                    put("additionalProperties", false)
                })
            })
        }
        val defs = listOf(
            AgentCapabilities.READ_TASKS to definition("list_tasks", "Consulter les tâches d'étude.", mapOf("filter" to schema("string", "Statut", listOf("all", "pending", "completed", "high")), "dateIso" to schema("string", "Date YYYY-MM-DD")), listOf("filter")),
            AgentCapabilities.CREATE_TASKS to definition("create_task", "Créer une tâche d'étude.", mapOf("title" to schema("string", "Titre"), "courseCode" to schema("string", "Code ou nom de matière"), "category" to schema("string", "Catégorie", listOf("exam", "project", "assignment", "revision", "reading")), "priority" to schema("string", "Priorité", listOf("high", "medium", "low")), "dueDateIso" to schema("string", "Date YYYY-MM-DD"), "scheduledTime" to schema("string", "Heure HH:mm"), "estimatedMinutes" to schema("integer", "Durée en minutes"), "notes" to schema("string", "Notes")), listOf("title", "courseCode", "category", "priority", "dueDateIso", "estimatedMinutes")),
            AgentCapabilities.COMPLETE_TASKS to definition("complete_task", "Marquer une tâche comme terminée.", mapOf("taskQuery" to schema("string", "Mots-clés du titre")), listOf("taskQuery")),
            AgentCapabilities.DELETE_TASKS to definition("delete_task", "Supprimer une tâche demandée par l'utilisateur.", mapOf("taskQuery" to schema("string", "Titre exact ou mots-clés")), listOf("taskQuery")),
            AgentCapabilities.MANAGE_COURSES to definition("add_course", "Ajouter une matière.", mapOf("code" to schema("string", "Code"), "title" to schema("string", "Nom de matière"), "professor" to schema("string", "Enseignant"), "targetHours" to schema("integer", "Heures cibles")), listOf("code", "title")),
            AgentCapabilities.MANAGE_PROJECTS to definition("create_project", "Créer un projet dans le planning.", mapOf("title" to schema("string", "Titre du projet"), "description" to schema("string", "Description"), "startDateIso" to schema("string", "Date de début YYYY-MM-DD"), "dueDateIso" to schema("string", "Échéance YYYY-MM-DD")), listOf("title", "startDateIso", "dueDateIso")),
            AgentCapabilities.CHANGE_THEME to definition("set_gradient_theme", "Choisir un thème de couleur.", mapOf("gradientId" to schema("string", "Identifiant du thème", listOf("aurore_lavande", "lagon_boreal", "menthe_emeraude", "peche_solaire", "rose_quartz", "nebuleuse_douce", "matcha_azur", "sakura_velours")),), listOf("gradientId")),
            AgentCapabilities.START_FOCUS to definition("start_focus_timer", "Démarrer un mode Focus.", mapOf("mode" to schema("string", "Mode", listOf("pomodoro", "deep_code", "short_break", "stopwatch")), "taskTitle" to schema("string", "Sujet de la session")), listOf("mode"))
        )
        return defs.filter { it.first in capabilities }.map { it.second }
    }

    private suspend fun executeTool(
        name: String,
        args: Map<String, JsonElement>,
        executor: StudyAgentToolExecutor,
        enabledCapabilities: Set<String>
    ): Pair<JsonObject, String> {
        val requiredCapability = capabilityForTool(name)
        if (requiredCapability == null || requiredCapability !in enabledCapabilities) {
            return JsonObject(mapOf("success" to JsonPrimitive(false), "error" to JsonPrimitive("Cette capacité est désactivée dans les réglages."))) to "Action bloquée : capacité désactivée"
        }
        return when (name) {
            "list_tasks" -> {
                val filter = args.str("filter", "all")
                val dateIso = args.strOrNull("dateIso")
                val res = executor.listTasks(filter, dateIso)
                res to "Planning consulté"
            }

            "create_task" -> {
                val title = args.str("title", "Session d'étude")
                val courseCode = args.str("courseCode", "GL-301")
                val category = args.str("category", "revision")
                val priority = args.str("priority", "medium")
                val dueDateIso = args.str("dueDateIso", LocalDate.now().toString())
                val scheduledTime = args.str("scheduledTime", "14:00")
                val estimatedMinutes = args.int("estimatedMinutes", 45)
                val notes = args.str("notes", "")
                val res = executor.createTask(
                    title = title,
                    courseCode = courseCode,
                    category = category,
                    priority = priority,
                    dueDateIso = dueDateIso,
                    scheduledTime = scheduledTime,
                    estimatedMinutes = estimatedMinutes,
                    notes = notes
                )
                res to "Tâche créée : $title"
            }

            "complete_task" -> {
                val query = args.str("taskQuery", "")
                val res = executor.completeTask(query)
                val ok = res["success"]?.jsonPrimitive?.contentOrNull == "true"
                res to if (ok) "Tâche validée" else "Tâche introuvable"
            }

            "delete_task" -> {
                val query = args.str("taskQuery", "")
                val res = executor.deleteTask(query)
                val ok = res["success"]?.jsonPrimitive?.contentOrNull == "true"
                res to if (ok) "Tâche supprimée" else "Tâche introuvable"
            }

            "add_course" -> {
                val code = args.str("code", "GL-307")
                val title = args.str("title", "Nouvelle matière")
                val professor = args.str("professor", "")
                val hours = args.int("targetHours", 20)
                val res = executor.addCourse(code, title, professor, hours)
                res to "Matière ajoutée : $code"
            }

            "create_project" -> {
                val title = args.str("title", "Nouveau projet")
                val description = args.str("description", "")
                val startDate = args.str("startDateIso", LocalDate.now().toString())
                val dueDate = args.str("dueDateIso", LocalDate.now().plusWeeks(1).toString())
                val result = executor.createProject(title, description, startDate, dueDate)
                result to if (result["success"]?.jsonPrimitive?.contentOrNull == "true") "Projet créé : $title" else "Projet non créé"
            }

            "set_gradient_theme" -> {
                val gradId = args.str("gradientId", "aurore_lavande")
                val res = executor.setGradientTheme(gradId)
                res to "Thème appliqué"
            }

            "start_focus_timer" -> {
                val mode = args.str("mode", "pomodoro")
                val title = args.strOrNull("taskTitle")
                val res = executor.startFocusTimer(mode, title)
                res to if (mode == "stopwatch") "Chronomètre lancé" else "Minuteur lancé"
            }

            else -> JsonObject(mapOf("error" to JsonPrimitive("Outil inconnu: $name"))) to "Action inconnue"
        }
    }

    private fun Map<String, JsonElement>.str(key: String, default: String): String =
        this[key]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() } ?: default

    private fun Map<String, JsonElement>.strOrNull(key: String): String? =
        this[key]?.jsonPrimitive?.contentOrNull?.takeIf { it.isNotBlank() }

    private fun Map<String, JsonElement>.int(key: String, default: Int): Int =
        this[key]?.jsonPrimitive?.intOrNull ?: default
}

package com.example.data.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.FunctionResponsePart
import com.google.firebase.ai.type.Tool
import com.google.firebase.ai.type.content
import com.example.data.model.AgentProvider
import com.example.data.model.TaskCategory
import com.example.data.model.HabitCategory
import com.example.data.model.CountdownCategory
import com.example.data.preferences.StudyAppSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.onEach

private const val TAG = "AiAgentRepo"

data class AgentActionLog(
    val toolName: String,
    val label: String,
    val succeeded: Boolean = true
)

internal fun agentToolResultSucceeded(result: JsonObject): Boolean {
    val successValue = result["success"] ?: return "error" !in result && "cancelled" !in result
    return runCatching { successValue.jsonPrimitive.booleanOrNull }.getOrNull() == true
}

data class AgentActionApproval(
    val id: String = UUID.randomUUID().toString(),
    val toolName: String,
    val title: String,
    val details: List<Pair<String, String>>,
    val destructive: Boolean = false
)

data class AgentPlanPreviewItem(
    val title: String,
    val detail: String,
    val dateIso: String,
    val estimatedMinutes: Int,
    val priority: String
)

data class AgentPlanPreview(
    val title: String,
    val summary: String,
    val items: List<AgentPlanPreviewItem>
)

data class AgentChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val isUser: Boolean,
    val text: String,
    val actions: List<AgentActionLog> = emptyList(),
    val isError: Boolean = false,
    val planPreview: AgentPlanPreview? = null
)

interface StudyAgentToolExecutor {
    suspend fun requestApproval(action: AgentActionApproval): Boolean
    suspend fun presentStudyPlan(plan: AgentPlanPreview): JsonObject
    suspend fun listTasks(filter: String, dateIso: String?): JsonObject
    suspend fun listStudyData(): JsonObject
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
    suspend fun createHabit(title: String, subtitle: String, category: String): JsonObject
    suspend fun createCountdown(title: String, notes: String, targetDateIso: String, category: String): JsonObject
}

data class AgentTurnResult(
    val replyText: String,
    val actions: List<AgentActionLog>,
    val inputTokens: Long? = null,
    val outputTokens: Long? = null
)

class AiAgentRepository {

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
Utilise uniquement les outils activés. Pour présenter un plan d'étude ou une proposition structurée, utilise present_study_plan, qui ne modifie aucune donnée. Toute modification exige ensuite l'accord explicite de l'utilisateur dans sa carte de validation; ne contourne jamais un refus et ne répète pas une action refusée sans nouvelle demande claire. Avant une action destructive, vérifie que la demande la désigne sans ambiguïté. Après l'accord, confirme exactement ce qui a été réalisé. Ne prétends jamais avoir effectué une action qui a échoué.
Compétences personnalisées activées : ${skills.ifBlank { "aucune" }}
Contexte de l'utilisateur : $contextSnapshot""".trimIndent()
    }

    private var chatSession: Chat? = null
    private var sessionFingerprint: String = ""
    private val openAiHistory = mutableListOf<JsonObject>()
    private val restoredHistoryPairs = mutableListOf<Pair<Boolean, String>>()

    fun resetConversation() {
        chatSession = null
        sessionFingerprint = ""
        openAiHistory.clear()
        restoredHistoryPairs.clear()
    }

    fun restoreConversation(messages: List<AgentChatMessage>) {
        resetConversation()
        val usable = messages
            .filter { !it.isError && it.text.isNotBlank() && it.text != "…" }
            .dropWhile { !it.isUser } // Skip leading assistant welcome banner for model history
            .takeLast(30)
        restoredHistoryPairs.addAll(usable.map { it.isUser to it.text })
    }

    suspend fun sendMessage(
        userPrompt: String,
        contextSnapshot: String,
        executor: StudyAgentToolExecutor,
        settings: StudyAppSettings,
        apiKey: String? = null,
        onTextUpdate: suspend (String) -> Unit = {}
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
            val result = withTimeout(60_000L) {
                if (settings.agentProvider != AgentProvider.FIREBASE.id) {
                    sendOpenAiCompatible(userPrompt, contextSnapshot, executor, settings, apiKey, executedActions)
                } else {
                    sendFirebase(userPrompt, contextSnapshot, executor, settings, executedActions, onTextUpdate)
                }
            }
            onTextUpdate(result.replyText)
            result
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
        executedActions: MutableList<AgentActionLog>,
        onTextUpdate: suspend (String) -> Unit
    ): AgentTurnResult {
        val today = LocalDate.now().toString()
        val declarations = AgentToolContract.enabled(settings.agentCapabilities).map { it.firebaseDeclaration() }
        val model = Firebase.ai.generativeModel(
            modelName = settings.agentModel.trim().also { require(it.isNotEmpty()) { "Sélectionnez le modèle actif dans Paramètres." } },
            tools = if (declarations.isEmpty()) emptyList() else listOf(Tool.functionDeclarations(declarations)),
            systemInstruction = content { text(systemPrompt(today, settings, contextSnapshot)) }
        )
        val chat = chatSession ?: model.startChat(
            history = restoredHistoryPairs.map { (isUser, msgText) ->
                content(role = if (isUser) "user" else "model") { text(msgText) }
            }
        ).also {
            chatSession = it
            restoredHistoryPairs.clear()
        }
        val initialPartial = StringBuilder()
        val initialChunks = chat.sendMessageStream(userPrompt).onEach { chunk ->
            chunk.text?.let { text ->
                initialPartial.append(text)
                onTextUpdate(initialPartial.toString())
            }
        }.toList()
        var response = initialChunks.lastOrNull() ?: throw IllegalStateException("Le modèle n'a renvoyé aucun contenu.")
        var safetyHops = 0
        while (response.functionCalls.isNotEmpty() && safetyHops < 6) {
            safetyHops++
            val responseParts = mutableListOf<FunctionResponsePart>()
            for (call in response.functionCalls) {
                val (resultJson, logLabel) = executeTool(call.name, call.args, executor, settings.agentCapabilities)
                executedActions.add(AgentActionLog(toolName = call.name, label = logLabel, succeeded = agentToolResultSucceeded(resultJson)))
                responseParts.add(FunctionResponsePart(call.name, resultJson))
            }
            val toolResponse = content("user") { responseParts.forEach { part(it) } }
            onTextUpdate("")
            val toolPartial = StringBuilder()
            val toolChunks = chat.sendMessageStream(toolResponse).onEach { chunk ->
                chunk.text?.let { text ->
                    toolPartial.append(text)
                    onTextUpdate(toolPartial.toString())
                }
            }.toList()
            response = toolChunks.lastOrNull() ?: throw IllegalStateException("Le modèle n'a renvoyé aucun contenu après l'action.")
        }
        val answer = response.text?.trim().orEmpty().ifBlank {
            if (executedActions.isNotEmpty()) summarizeActions(executedActions)
            else "Je n'ai pas pu formuler de réponse. Pouvez-vous reformuler ?"
        }
        return AgentTurnResult(includeActionOutcome(answer, executedActions), executedActions)
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
        val endpoint = compatibleEndpoint(settings.agentBaseUrl)
        if (openAiHistory.isEmpty()) {
            openAiHistory += buildJsonObject {
                put("role", "system")
                put("content", systemPrompt(LocalDate.now().toString(), settings, contextSnapshot))
            }
            if (restoredHistoryPairs.isNotEmpty()) {
                restoredHistoryPairs.forEach { (isUser, msgText) ->
                    openAiHistory += buildJsonObject {
                        put("role", if (isUser) "user" else "assistant")
                        put("content", msgText)
                    }
                }
                restoredHistoryPairs.clear()
            }
        }
        openAiHistory += buildJsonObject { put("role", "user"); put("content", userPrompt) }
        val tools = AgentToolContract.enabled(settings.agentCapabilities).map { it.httpDeclaration() }
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
            val response = postProviderJson(endpoint, apiKey, request, settings.agentProvider)
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
                val finalAnswer = answer.ifBlank {
                    if (executedActions.isEmpty()) "Je n'ai pas reçu de réponse exploitable." else summarizeActions(executedActions)
                }
                return AgentTurnResult(
                    includeActionOutcome(finalAnswer, executedActions),
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
                        is JsonPrimitive -> (arguments.takeIf { it.isString && it.content.length <= 32_768 }
                            ?.let { Json.parseToJsonElement(it.content) } as? JsonObject)?.toMap()
                        else -> null
                    }
                }.getOrNull()
                val (result, label) = executeTool(name, args, executor, settings.agentCapabilities)
                executedActions += AgentActionLog(name, label, succeeded = agentToolResultSucceeded(result))
                openAiHistory += buildJsonObject {
                    put("role", "tool")
                    put("tool_call_id", callId)
                    put("content", result.toString())
                }
            }
        }
        return AgentTurnResult("J'ai arrêté la séquence après plusieurs appels d'outils. Vérifiez le résultat dans l'application.", executedActions)
    }

    private fun compatibleEndpoint(baseUrl: String): String {
        val root = baseUrl.trim().trimEnd('/')
        require(root.startsWith("https://", ignoreCase = true)) { "L'URL du fournisseur doit commencer par https:// pour protéger votre clé API." }
        val endpoint = when {
            root.endsWith("/chat/completions", ignoreCase = true) -> root
            root.endsWith("/v1", ignoreCase = true) -> "$root/chat/completions"
            else -> "$root/v1/chat/completions"
        }
        val endpointUrl = URL(endpoint)
        require(endpointUrl.host.isNotBlank() && endpointUrl.userInfo == null) { "L'URL du fournisseur n'est pas valide." }
        return endpoint
    }

    /** A stateless completion: planning never inherits chat history, skills or tools. */
    internal suspend fun completePlannerPrompt(prompt: String, settings: StudyAppSettings, apiKey: String?): String {
        require(!apiKey.isNullOrBlank()) { "Ajoutez la clé API du fournisseur actif dans les réglages de Light Agent." }
        val request = buildPlannerProviderRequest(prompt, settings)
        val response = postProviderJson(request.endpoint, apiKey, request.payload, request.providerId)
        return plannerProviderText(response, request.providerId)
    }

    private suspend fun postProviderJson(endpoint: String, apiKey: String, payload: JsonObject, providerId: String): JsonObject =
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
                if (code !in 200..299) {
                    val message = when (code) {
                        401 -> "Le fournisseur IA a rejeté l’authentification (HTTP 401). Vérifiez la clé de ce fournisseur."
                        403 -> "Le fournisseur IA refuse l’accès (HTTP 403). Vérifiez les droits de la clé et l’accès au modèle."
                        else -> "Le fournisseur IA a répondu avec le code HTTP $code."
                    }
                    // Un fournisseur peut inclure des identifiants ou une partie de la clé dans son erreur.
                    connection.errorStream?.close()
                    throw IllegalStateException(message)
                }
                val body = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                Json.parseToJsonElement(body).jsonObject
            } finally {
                connection.disconnect()
            }
        }

    private fun approvalForTool(name: String, args: Map<String, JsonElement>, today: LocalDate): AgentActionApproval? {
        fun details(vararg values: Pair<String, String>) = values.filter { it.second.isNotBlank() }
        return when (name) {
            "create_task" -> AgentActionApproval(toolName = name, title = "Créer une tâche", details = details(
                "Titre" to args.str("title", "Session d'étude").trim().take(140),
                "Matière" to args.str("courseCode", "").trim(),
                "Échéance" to args.str("dueDateIso", today.toString()).trim(),
                "Heure" to args.str("scheduledTime", "14:00").trim().take(20),
                "Durée" to "${args.int("estimatedMinutes", 45).coerceIn(5, 480)} minutes",
                "Priorité" to (args.str("priority", "medium").takeIf { it in setOf("high", "medium", "low") } ?: "medium"),
                "Catégorie" to TaskCategory.sanitizeKey(args.str("category", "revision")),
                "Notes" to args.str("notes", "").trim().take(500)
            ))
            "complete_task" -> AgentActionApproval(toolName = name, title = "Valider une tâche", details = details("Titre demandé" to args.str("taskQuery", "").trim()))
            "delete_task" -> AgentActionApproval(toolName = name, title = "Supprimer définitivement une tâche", details = details("Titre exact" to args.str("taskQuery", "").trim()), destructive = true)
            "add_course" -> AgentActionApproval(toolName = name, title = "Ajouter une matière", details = details(
                "Code" to args.str("code", "GL-307").trim().take(20), "Nom" to args.str("title", "Nouvelle matière").trim().take(100),
                "Enseignant" to args.str("professor", "").trim().take(80), "Volume cible" to "${args.int("targetHours", 20).coerceIn(1, 200)} heures"
            ))
            "create_project" -> AgentActionApproval(toolName = name, title = "Créer un projet", details = details(
                "Titre" to args.str("title", "Nouveau projet").trim().take(120), "Description" to args.str("description", "").take(500),
                "Début" to args.str("startDateIso", today.toString()), "Échéance" to args.str("dueDateIso", today.plusWeeks(1).toString())
            ))
            "set_gradient_theme" -> AgentActionApproval(toolName = name, title = "Changer le thème", details = details("Thème demandé" to args.str("gradientId", "aurore_lavande")))
            "start_focus_timer" -> AgentActionApproval(toolName = name, title = "Démarrer Focus", details = details(
                "Mode" to (args.str("mode", "pomodoro").takeIf { it in setOf("pomodoro", "deep_code", "short_break", "stopwatch") } ?: "pomodoro"),
                "Sujet" to (args.strOrNull("taskTitle")?.trim()?.take(120) ?: "Session de concentration")
            ))
            "create_habit" -> AgentActionApproval(toolName = name, title = "Créer une habitude", details = details(
                "Nom" to args.str("title", "Nouvelle habitude").trim().take(80), "Description" to args.str("subtitle", "").trim().take(140), "Catégorie" to HabitCategory.sanitizeKey(args.str("category", "study"))
            ))
            "create_countdown" -> AgentActionApproval(toolName = name, title = "Créer une échéance", details = details(
                "Nom" to args.str("title", "Nouvelle échéance").trim().take(100), "Date" to args.str("targetDateIso", "").trim(),
                "Catégorie" to CountdownCategory.sanitizeKey(args.str("category", "exam")), "Notes" to args.str("notes", "").trim().take(300)
            ))
            else -> null
        }
    }

    private fun summarizeActions(actions: List<AgentActionLog>): String = when {
        actions.all { it.succeeded } -> "C'est fait : ${actions.joinToString(", ") { it.label }}."
        actions.none { it.succeeded } -> "Aucune action n'a abouti : ${actions.joinToString(", ") { it.label }}."
        else -> "Résultat partiel : ${actions.joinToString(", ") { it.label }}."
    }

    private fun includeActionOutcome(answer: String, actions: List<AgentActionLog>): String {
        if (actions.none { !it.succeeded }) return answer
        val summary = summarizeActions(actions)
        return if (answer.endsWith(summary)) answer else "$answer\n\n$summary"
    }

    internal suspend fun executeTool(
        name: String,
        arguments: Map<String, JsonElement>?,
        executor: StudyAgentToolExecutor,
        enabledCapabilities: Set<String>
    ): Pair<JsonObject, String> {
        fun blocked(message: String, label: String) = buildJsonObject { put("success", false); put("error", message) } to label
        val spec = AgentToolContract[name]
        if (spec == null || spec.capability !in enabledCapabilities) {
            return blocked("Cette capacité est désactivée ou indisponible.", "Action bloquée : capacité indisponible")
        }
        val args = arguments ?: return blocked("Les arguments doivent former un objet JSON.", "Action bloquée : arguments invalides")
        val invalid = spec.invalidArguments(args)
        if (invalid != null) {
            return blocked("Arguments invalides : $invalid.", "Action bloquée : arguments invalides")
        }
        val today = LocalDate.now()
        val approval = if (spec.requiresApproval) approvalForTool(name, args, today) else null
        if (spec.requiresApproval && approval == null) {
            return blocked("Cette action n’a pas de procédure de confirmation.", "Action bloquée : validation indisponible")
        }
        if (approval != null) {
            if (!executor.requestApproval(approval)) {
                return JsonObject(mapOf("success" to JsonPrimitive(false), "cancelled" to JsonPrimitive(true), "error" to JsonPrimitive("L'utilisateur a refusé cette action."))) to "Action refusée : ${approval.title}"
            }
            currentCoroutineContext().ensureActive()
        }
        return when (name) {
            "list_tasks" -> {
                val filter = args.str("filter", "all")
                val dateIso = args.strOrNull("dateIso")
                val res = executor.listTasks(filter, dateIso)
                res to if (agentToolResultSucceeded(res)) "Planning consulté" else "Planning non consulté"
            }

            "list_study_data" -> {
                val res = executor.listStudyData()
                res to if (agentToolResultSucceeded(res)) "Données d'étude consultées" else "Données d'étude non consultées"
            }

            "present_study_plan" -> {
                val rawItems = args["items"]?.let { runCatching { it.jsonArray }.getOrNull() } ?: JsonArray(emptyList())
                if (rawItems.isEmpty() || rawItems.size > 8) {
                    JsonObject(mapOf("success" to JsonPrimitive(false), "error" to JsonPrimitive("Un plan doit contenir entre une et huit étapes."))) to "Aperçu de plan invalide"
                } else {
                    val items = rawItems.mapNotNull { element ->
                        val item = runCatching { element.jsonObject }.getOrNull() ?: return@mapNotNull null
                        val title = item["title"]?.jsonPrimitive?.contentOrNull?.trim()?.take(100).orEmpty()
                        if (title.isBlank()) return@mapNotNull null
                        val dateCandidate = item["dateIso"]?.jsonPrimitive?.contentOrNull?.trim().orEmpty()
                        val date = if (dateCandidate.isBlank() || runCatching { LocalDate.parse(dateCandidate) }.isSuccess) dateCandidate else ""
                        val minutes = item["estimatedMinutes"]?.jsonPrimitive?.intOrNull?.coerceIn(0, 480) ?: 0
                        val priority = item["priority"]?.jsonPrimitive?.contentOrNull?.takeIf { it in setOf("high", "medium", "low") } ?: "medium"
                        AgentPlanPreviewItem(
                            title = title,
                            detail = item["detail"]?.jsonPrimitive?.contentOrNull.orEmpty().take(240),
                            dateIso = date,
                            estimatedMinutes = minutes,
                            priority = priority
                        )
                    }
                    if (items.isEmpty()) {
                        JsonObject(mapOf("success" to JsonPrimitive(false), "error" to JsonPrimitive("Aucune étape valide à afficher."))) to "Aperçu de plan invalide"
                    } else {
                        val plan = AgentPlanPreview(
                            title = args.str("title", "Plan d'étude").take(100),
                            summary = args.str("summary", "").take(500),
                            items = items
                        )
                        val result = executor.presentStudyPlan(plan)
                        result to if (agentToolResultSucceeded(result)) "Plan affiché : ${plan.title}" else "Plan non affiché"
                    }
                }
            }

            "create_task" -> {
                val title = args.str("title", "Session d'étude").trim().take(140)
                val courseCode = args.str("courseCode", "").trim()
                val category = TaskCategory.sanitizeKey(args.str("category", "revision"))
                val priority = args.str("priority", "medium").takeIf { it in setOf("high", "medium", "low") } ?: "medium"
                val dueDateIso = args.str("dueDateIso", today.toString()).trim()
                val scheduledTime = args.str("scheduledTime", "14:00").trim().take(20)
                val estimatedMinutes = args.int("estimatedMinutes", 45).coerceIn(5, 480)
                val notes = args.str("notes", "").trim().take(500)
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
                res to if (agentToolResultSucceeded(res)) "Tâche créée : $title" else "Tâche non créée"
            }

            "complete_task" -> {
                val query = args.str("taskQuery", "")
                val res = executor.completeTask(query)
                res to if (agentToolResultSucceeded(res)) "Tâche validée" else "Tâche introuvable ou non validée"
            }

            "delete_task" -> {
                val query = args.str("taskQuery", "")
                val res = executor.deleteTask(query)
                res to if (agentToolResultSucceeded(res)) "Tâche supprimée" else "Tâche introuvable ou non supprimée"
            }

            "add_course" -> {
                val code = args.str("code", "GL-307").trim().take(20)
                val title = args.str("title", "Nouvelle matière").trim().take(100)
                val professor = args.str("professor", "").trim().take(80)
                val hours = args.int("targetHours", 20).coerceIn(1, 200)
                val res = executor.addCourse(code, title, professor, hours)
                res to if (agentToolResultSucceeded(res)) "Matière ajoutée : $code" else "Matière non ajoutée"
            }

            "create_project" -> {
                val title = args.str("title", "Nouveau projet").trim().take(120)
                val description = args.str("description", "").take(500)
                val startDate = args.str("startDateIso", today.toString())
                val dueDate = args.str("dueDateIso", today.plusWeeks(1).toString())
                val result = executor.createProject(title, description, startDate, dueDate)
                result to if (agentToolResultSucceeded(result)) "Projet créé : $title" else "Projet non créé"
            }

            "set_gradient_theme" -> {
                val gradId = args.str("gradientId", "aurore_lavande")
                val res = executor.setGradientTheme(gradId)
                res to if (agentToolResultSucceeded(res)) "Thème appliqué" else "Thème non appliqué"
            }

            "start_focus_timer" -> {
                val requestedMode = args.str("mode", "pomodoro")
                val mode = requestedMode.takeIf { it in setOf("pomodoro", "deep_code", "short_break", "stopwatch") } ?: "pomodoro"
                val title = args.strOrNull("taskTitle")?.trim()?.take(120)
                val res = executor.startFocusTimer(mode, title)
                res to when {
                    !agentToolResultSucceeded(res) -> "Minuteur non démarré"
                    mode == "stopwatch" -> "Chronomètre lancé"
                    else -> "Minuteur lancé"
                }
            }

            "create_habit" -> {
                val title = args.str("title", "Nouvelle habitude").trim().take(80)
                val subtitle = args.str("subtitle", "").trim().take(140)
                val category = HabitCategory.sanitizeKey(args.str("category", "study"))
                val res = executor.createHabit(title, subtitle, category)
                res to if (agentToolResultSucceeded(res)) "Habitude créée : $title" else "Habitude non créée"
            }

            "create_countdown" -> {
                val title = args.str("title", "Nouvelle échéance").trim().take(100)
                val notes = args.str("notes", "").trim().take(300)
                val targetDate = args.str("targetDateIso", "").trim()
                val category = CountdownCategory.sanitizeKey(args.str("category", "exam"))
                val res = executor.createCountdown(title, notes, targetDate, category)
                res to if (agentToolResultSucceeded(res)) "Échéance créée : $title" else "Échéance non créée"
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

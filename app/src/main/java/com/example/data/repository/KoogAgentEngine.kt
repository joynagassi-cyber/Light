package com.example.data.repository

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeExecuteTools
import ai.koog.agents.core.dsl.extension.nodeLLMRequestStreaming
import ai.koog.agents.core.dsl.extension.nodeLLMSendToolResultsStreaming
import ai.koog.agents.core.dsl.extension.onTextMessage
import ai.koog.agents.core.dsl.extension.onToolCalls
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import ai.koog.agents.core.tools.reflect.asTools
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.http.client.KoogHttpClient
import ai.koog.http.client.ktor.KtorKoogHttpClient
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.openai.OpenAIClientSettings
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.streaming.StreamFrame
import ai.koog.prompt.streaming.toMessageResponse
import com.example.data.preferences.StudyAppSettings
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * One shared Ktor-backed HTTP client per (baseUrl, headers) pair.
 *
 * Koog creates a `KoogHttpClient` through a [KoogHttpClient.Factory] every time an
 * `OpenAILLMClient` is built. Building a fresh Ktor client per chat turn would lose
 * connection pooling and pay a TLS handshake every message, so we cache by the exact
 * configuration that is baked into the client (base URL + default headers, i.e. the API key).
 */
private object SharedKoogHttpClientFactory : KoogHttpClient.Factory {
    private val delegate = KtorKoogHttpClient.Factory()
    private val clients = LinkedHashMap<String, KoogHttpClient>()

    override fun create(
        clientName: String,
        baseUrl: String,
        headers: Map<String, String>,
        queryParameters: Map<String, String>,
        requestTimeoutMillis: Long,
        connectTimeoutMillis: Long,
        socketTimeoutMillis: Long,
        json: Json,
    ): KoogHttpClient {
        val key = buildString {
            append(baseUrl).append('|')
            append(headers.toSortedMap()).append('|')
            append(queryParameters.toSortedMap())
        }
        synchronized(clients) {
            clients[key]?.let { return it }
            val created = delegate.create(
                clientName = clientName,
                baseUrl = baseUrl,
                headers = headers,
                queryParameters = queryParameters,
                requestTimeoutMillis = requestTimeoutMillis,
                connectTimeoutMillis = connectTimeoutMillis,
                socketTimeoutMillis = socketTimeoutMillis,
                json = json,
            )
            // Bound the cache: clearing also drops the reference to any evicted client.
            // In practice the app only ever uses one provider + key pair at a time.
            if (clients.size >= 4) clients.clear()
            clients[key] = created
            return created
        }
    }
}

/**
 * Tools exposed to the Light agent, implemented on top of the existing [AgentToolContract]
 * validation and approval pipeline ([AiAgentRepository.runObservedTool]).
 *
 * Every Light capability — argument validation, confirmation cards for destructive or
 * state-changing actions, capability switches — is enforced exactly as before; Koog only
 * handles the transport, the tool-call loop and the streaming.
 */
internal class LightKoogTools(
    private val repository: AiAgentRepository,
    private val executor: StudyAgentToolExecutor,
    private val capabilities: Set<String>,
    private val onActivity: (AgentActivity) -> Unit,
    private val executedActions: MutableList<AgentActionLog>,
) : ToolSet {

    private fun args(vararg entries: Pair<String, JsonElement?>): JsonObject =
        JsonObject(LinkedHashMap<String, JsonElement>().apply {
            for ((key, value) in entries) value?.let { put(key, it) }
        })

    private fun text(value: String): JsonElement? =
        value.takeIf { it.isNotBlank() }?.let(::JsonPrimitive)

    private suspend fun delegate(name: String, args: JsonObject): String = try {
        val (result, log) = repository.runObservedTool(name, args, executor, capabilities, onActivity)
        executedActions += log
        result.toString()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        val message = e.message ?: "Erreur de l'outil $name."
        executedActions += AgentActionLog(name, "Action échouée : $name", succeeded = false)
        JsonObject(
            mapOf(
                "success" to JsonPrimitive(false),
                "error" to JsonPrimitive(message),
            )
        ).toString()
    }

    @Tool
    @LLMDescription("Consulter le planning (les tâches de l'application Light).")
    suspend fun list_tasks(
        @LLMDescription("Statut demandé : 'all', 'pending', 'completed' ou 'high'. Utiliser 'all' par défaut.")
        filter: String = "all",
        @LLMDescription("Date au format AAAA-MM-JJ, ou chaîne vide pour toutes les dates")
        dateIso: String = "",
    ): String = delegate(
        "list_tasks",
        args("filter" to JsonPrimitive(filter.takeIf { it.isNotBlank() } ?: "all"), "dateIso" to text(dateIso)),
    )

    @Tool
    @LLMDescription("Consulter les matières, projets, habitudes et échéances connus de l'application.")
    suspend fun list_study_data(): String = delegate("list_study_data", args())

    @Tool
    @LLMDescription("Afficher une proposition de plan d'étude à l'utilisateur (aucun effet sur les données). Demander ensuite une confirmation avant de créer quoi que ce soit.")
    suspend fun present_study_plan(
        @LLMDescription("Titre du plan (100 caractères maximum)")
        title: String,
        @LLMDescription("Résumé du plan (500 caractères maximum)")
        summary: String,
        @LLMDescription("De une à huit étapes sous forme de tableau JSON encodé en chaîne. Chaque étape est un objet {\"title\", \"detail\", \"dateIso\", \"estimatedMinutes\", \"priority\"} ; title est obligatoire, priority vaut 'high', 'medium' ou 'low'. Exemple : [{\"title\":\"Réviser le chapitre 3\",\"detail\":\"\",\"dateIso\":\"\",\"estimatedMinutes\":45,\"priority\":\"medium\"}]")
        items: String,
    ): String {
        val itemsElement = runCatching { Json.parseToJsonElement(items) }.getOrElse { JsonPrimitive(items) }
        return delegate(
            "present_study_plan",
            args("title" to text(title), "summary" to text(summary), "items" to itemsElement),
        )
    }

    @Tool
    @LLMDescription("Créer une tâche dans le planning. Demande toujours une confirmation avant exécution.")
    suspend fun create_task(
        @LLMDescription("Titre de la tâche (140 caractères maximum)")
        title: String,
        @LLMDescription("Catégorie de la tâche : 'exam', 'project', 'milestone', 'assignment', 'revision' ou 'reading'")
        category: String,
        @LLMDescription("Priorité : 'high', 'medium' ou 'low'")
        priority: String,
        @LLMDescription("Échéance au format AAAA-MM-JJ")
        dueDateIso: String,
        @LLMDescription("Durée estimée en minutes (5 à 480)")
        estimatedMinutes: Int,
        @LLMDescription("Code du cours (ex. 'GL-307'), ou chaîne vide si aucun")
        courseCode: String = "",
        @LLMDescription("Heure planifiée au format HH:mm, ou chaîne vide")
        scheduledTime: String = "",
        @LLMDescription("Notes libres, ou chaîne vide")
        notes: String = "",
    ): String = delegate(
        "create_task",
        args(
            "title" to text(title), "category" to text(category), "priority" to text(priority),
            "dueDateIso" to text(dueDateIso), "estimatedMinutes" to JsonPrimitive(estimatedMinutes),
            "courseCode" to text(courseCode), "scheduledTime" to text(scheduledTime), "notes" to text(notes),
        ),
    )

    @Tool
    @LLMDescription("Marquer une tâche existante comme terminée. Demande toujours une confirmation avant exécution.")
    suspend fun complete_task(
        @LLMDescription("Titre exact ou mots-clés identifiant la tâche")
        taskQuery: String,
    ): String = delegate("complete_task", args("taskQuery" to text(taskQuery)))

    @Tool
    @LLMDescription("Supprimer définitivement une tâche dont le titre est exact. Demande toujours une confirmation avant exécution.")
    suspend fun delete_task(
        @LLMDescription("Titre exact de la tâche à supprimer")
        taskQuery: String,
    ): String = delegate("delete_task", args("taskQuery" to text(taskQuery)))

    @Tool
    @LLMDescription("Ajouter une matière (cours) au parcours. Demande toujours une confirmation avant exécution.")
    suspend fun add_course(
        @LLMDescription("Code du cours (20 caractères maximum)")
        code: String,
        @LLMDescription("Intitulé de la matière (100 caractères maximum)")
        title: String,
        @LLMDescription("Nom de l'enseignant, ou chaîne vide")
        professor: String = "",
        @LLMDescription("Volume horaire cible (1 à 200 heures), ou -1 si inconnu")
        targetHours: Int = -1,
    ): String = delegate(
        "add_course",
        args(
            "code" to text(code), "title" to text(title), "professor" to text(professor),
            "targetHours" to targetHours.takeIf { it in 1..200 }?.let(::JsonPrimitive),
        ),
    )

    @Tool
    @LLMDescription("Créer un projet dans l'application. Demande toujours une confirmation avant exécution.")
    suspend fun create_project(
        @LLMDescription("Titre du projet (120 caractères maximum)")
        title: String,
        @LLMDescription("Date de début au format AAAA-MM-JJ")
        startDateIso: String,
        @LLMDescription("Date d'échéance au format AAAA-MM-JJ (doit être postérieure au début)")
        dueDateIso: String,
        @LLMDescription("Description du projet, ou chaîne vide")
        description: String = "",
    ): String = delegate(
        "create_project",
        args(
            "title" to text(title), "description" to text(description),
            "startDateIso" to text(startDateIso), "dueDateIso" to text(dueDateIso),
        ),
    )

    @Tool
    @LLMDescription("Changer le dégradé d'arrière-plan de l'application Light. Demande toujours une confirmation avant exécution.")
    suspend fun set_gradient_theme(
        @LLMDescription("Identifiant du dégradé : 'aurore_lavande', 'lagon_boreal', 'menthe_emeraude', 'peche_solaire', 'rose_quartz', 'nebuleuse_douce', 'matcha_azur' ou 'sakura_velours'")
        gradientId: String,
    ): String = delegate("set_gradient_theme", args("gradientId" to text(gradientId)))

    @Tool
    @LLMDescription("Démarrer une session Focus (minuteur de concentration). Demande toujours une confirmation avant exécution.")
    suspend fun start_focus_timer(
        @LLMDescription("Mode : 'pomodoro', 'deep_code', 'short_break' ou 'stopwatch'")
        mode: String,
        @LLMDescription("Intitulé de la session, ou chaîne vide")
        taskTitle: String = "",
    ): String = delegate(
        "start_focus_timer",
        args("mode" to text(mode), "taskTitle" to text(taskTitle)),
    )

    @Tool
    @LLMDescription("Ajouter une habitude à suivre. Demande toujours une confirmation avant exécution.")
    suspend fun create_habit(
        @LLMDescription("Nom de l'habitude (80 caractères maximum)")
        title: String,
        @LLMDescription("Description courte, ou chaîne vide")
        subtitle: String = "",
        @LLMDescription("Catégorie : 'study', 'life', 'health', 'sports' ou 'mindset'")
        category: String = "",
    ): String = delegate(
        "create_habit",
        args("title" to text(title), "subtitle" to text(subtitle), "category" to text(category)),
    )

    @Tool
    @LLMDescription("Ajouter une échéance à la liste. Demande toujours une confirmation avant exécution.")
    suspend fun create_countdown(
        @LLMDescription("Nom de l'échéance (100 caractères maximum)")
        title: String,
        @LLMDescription("Date cible au format AAAA-MM-JJ")
        targetDateIso: String,
        @LLMDescription("Notes libres, ou chaîne vide")
        notes: String = "",
        @LLMDescription("Catégorie : 'exam', 'project', 'milestone' ou 'personal'")
        category: String = "",
    ): String = delegate(
        "create_countdown",
        args("title" to text(title), "notes" to text(notes), "targetDateIso" to text(targetDateIso), "category" to text(category)),
    )
}

/** Streaming tool-calling loop: each LLM call streams frames to the UI; tool calls are executed sequentially. */
private fun lightStreamingWithToolsStrategy() = strategy<String, String>("light_streaming_loop") {
    val nodeCallLLM by nodeLLMRequestStreaming().transform { it.toList().toMessageResponse() }
    val nodeSendToolResults by nodeLLMSendToolResultsStreaming().transform { it.toList().toMessageResponse() }
    val executeTools by nodeExecuteTools(parallel = false)

    edge(nodeStart forwardTo nodeCallLLM)
    edge(nodeCallLLM forwardTo executeTools onToolCalls { true })
    edge(executeTools forwardTo nodeSendToolResults)
    edge(nodeSendToolResults forwardTo executeTools onToolCalls { true })
    edge(nodeSendToolResults forwardTo nodeFinish onTextMessage { true })
    edge(nodeCallLLM forwardTo nodeFinish onTextMessage { true })
}

internal data class KoogTurnUsage(val inputTokens: Long?, val outputTokens: Long?)

/**
 * Runs one OpenAI-compatible turn (agnes and any compatible provider) through Koog.
 * The caller keeps ownership of [AiAgentRepository.openAiHistory]; this function only
 * normalizes it into a plain user/assistant seed for the Koog prompt.
 */
internal suspend fun AiAgentRepository.runKoogOpenAiTurn(
    userPrompt: String,
    instructions: String,
    executor: StudyAgentToolExecutor,
    settings: StudyAppSettings,
    apiKey: String,
    executedActions: MutableList<AgentActionLog>,
    onActivity: (AgentActivity) -> Unit,
    onTextUpdate: suspend (String) -> Unit,
): Pair<String, KoogTurnUsage> {
    val root = settings.agentBaseUrl.trim().trimEnd('/')
    require(root.startsWith("https://", ignoreCase = true)) {
        "L'URL du fournisseur doit commencer par https:// pour protéger votre clé API."
    }
    val lowerRoot = root.lowercase()
    val (koogBaseUrl, chatCompletionsPath) = when {
        lowerRoot.endsWith("/chat/completions") ->
            root.dropLast("/chat/completions".length).trimEnd('/') to "chat/completions"
        lowerRoot.endsWith("/v1") -> root to "chat/completions"
        else -> root to "v1/chat/completions"
    }
    val endpointUrl = java.net.URL("$koogBaseUrl/$chatCompletionsPath")
    require(endpointUrl.host.isNotBlank() && endpointUrl.userInfo == null) {
        "L'URL du fournisseur n'est pas valide."
    }

    // History: plain user/assistant text only. Provider-specific artefacts (tool_calls, tool
    // messages) are deliberately dropped instead of replayed with stale call ids.
    val seed = compactPlainTextMessages(openAiHistory).let { compacted ->
        if (compacted.isNotEmpty()) compacted
        else restoredHistoryPairs
            .takeLast(MAX_HISTORY_MESSAGES)
            .map { (isUser, text) ->
                JsonObject(mapOf("role" to JsonPrimitive(if (isUser) "user" else "assistant"), "content" to JsonPrimitive(text)))
            }
    }.takeLast(MAX_HISTORY_MESSAGES)

    val seededPrompt = prompt("light-agent") {
        system(instructions)
        seed.forEach { message ->
            val role = (message["role"] as? JsonPrimitive)?.contentOrNull
            val content = (message["content"] as? JsonPrimitive)?.contentOrNull
                ?.takeIf { it.isNotBlank() }
                ?.take(MAX_MESSAGE_CHARS)
                ?: return@forEach
            if (role == "user") user(content) else assistant(content)
        }
    }

    val model = LLModel(
        provider = LLMProvider.OpenAI,
        id = settings.agentModel.trim(),
        capabilities = listOf(
            LLMCapability.Completion,
            LLMCapability.Tools,
            LLMCapability.ToolChoice,
            LLMCapability.Temperature,
            LLMCapability.Schema.JSON.Basic,
            LLMCapability.Thinking,
        ),
        contextLength = 128_000,
    )

    val client = OpenAILLMClient(
        apiKey = apiKey.trim(),
        settings = OpenAIClientSettings(baseUrl = koogBaseUrl, chatCompletionsPath = chatCompletionsPath),
        httpClientFactory = SharedKoogHttpClientFactory,
    )
    val promptExecutor = MultiLLMPromptExecutor(client)

    val toolset = LightKoogTools(this, executor, settings.agentCapabilities, onActivity, executedActions)
    val enabledToolNames = AgentToolContract.enabled(settings.agentCapabilities).map { it.name }.toSet()
    val toolRegistry = if (enabledToolNames.isEmpty()) {
        ToolRegistry.EMPTY
    } else {
        ToolRegistry { tools(toolset.asTools().filter { it.name in enabledToolNames }) }
    }

    val streamingBuffer = StringBuilder()
    var inputTokens = 0L
    var outputTokens = 0L
    var sawUsage = false

    val agent = AIAgent(
        promptExecutor = promptExecutor,
        agentConfig = AIAgentConfig(
            prompt = seededPrompt,
            model = model,
            maxAgentIterations = MAX_TOOL_SEQUENCES,
        ),
        strategy = lightStreamingWithToolsStrategy(),
        toolRegistry = toolRegistry,
    ) {
        handleEvents {
            onLLMStreamingStarting {
                streamingBuffer.setLength(0)
            }
            onLLMStreamingFrameReceived { ctx ->
                when (val frame = ctx.streamFrame) {
                    is StreamFrame.TextDelta -> {
                        streamingBuffer.append(frame.text)
                        onTextUpdate(streamingBuffer.toString())
                    }
                    is StreamFrame.End -> {
                        frame.metaInfo.inputTokensCount?.toLong()?.let { inputTokens += it; sawUsage = true }
                        frame.metaInfo.outputTokensCount?.toLong()?.let { outputTokens += it; sawUsage = true }
                    }
                    else -> Unit
                }
            }
            onAgentExecutionFailed { ctx ->
                android.util.Log.w(TAG, "Koog agent execution failed", ctx.error)
            }
        }
    }

    val reply = try {
        agent.run(userPrompt, null).trim()
    } finally {
        // GraphAIAgent.close() is currently a no-op; the shared HTTP client outlives the turn on purpose.
        try {
            agent.close()
        } catch (_: Exception) {
            // Never mask the turn result because of cleanup.
        }
    }

    // Plain user/assistant pair only — keeps the next turn's seed compact and provider-agnostic.
    val finalAnswer = reply.ifBlank {
        if (executedActions.isEmpty()) "Je n'ai pas reçu de réponse exploitable." else summarizeActions(executedActions)
    }
    openAiHistory.clear()
    openAiHistory.addAll(seed)
    openAiHistory += JsonObject(mapOf("role" to JsonPrimitive("user"), "content" to JsonPrimitive(userPrompt.take(MAX_MESSAGE_CHARS))))
    openAiHistory += JsonObject(mapOf("role" to JsonPrimitive("assistant"), "content" to JsonPrimitive(finalAnswer.take(MAX_MESSAGE_CHARS))))
    if (openAiHistory.size > MAX_HISTORY_MESSAGES) {
        val trimmed = openAiHistory.takeLast(MAX_HISTORY_MESSAGES)
        openAiHistory.clear()
        openAiHistory.addAll(trimmed)
    }
    restoredHistoryPairs.clear()

    return finalAnswer to KoogTurnUsage(inputTokens.takeIf { sawUsage }, outputTokens.takeIf { sawUsage })
}

/** Keeps only plain-text user/assistant messages from provider history. */
private fun compactPlainTextMessages(messages: List<JsonObject>): List<JsonObject> =
    messages.mapNotNull { message ->
        if (message["tool_calls"] != null) return@mapNotNull null
        val role = (message["role"] as? JsonPrimitive)?.contentOrNull
        if (role != "user" && role != "assistant") return@mapNotNull null
        val content = (message["content"] as? JsonPrimitive)?.contentOrNull
            ?.takeIf { it.isNotBlank() }
            ?.take(MAX_MESSAGE_CHARS)
            ?: return@mapNotNull null
        JsonObject(mapOf("role" to JsonPrimitive(role), "content" to JsonPrimitive(content)))
    }

private const val MAX_HISTORY_MESSAGES = 40
private const val MAX_MESSAGE_CHARS = 8_000
private const val MAX_TOOL_SEQUENCES = 20
private const val TAG = "KoogAgentEngine"

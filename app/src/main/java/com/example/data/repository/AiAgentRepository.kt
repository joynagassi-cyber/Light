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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate
import java.util.UUID

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

    suspend fun setGradientTheme(gradientId: String): JsonObject
    suspend fun startFocusTimer(mode: String, taskTitle: String?): JsonObject
}

data class AgentTurnResult(
    val replyText: String,
    val actions: List<AgentActionLog>
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

    private val agentModel by lazy {
        val today = LocalDate.now().toString()
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            tools = listOf(Tool.functionDeclarations(functionDeclarations)),
            systemInstruction = content {
                text(
                    """
                    Tu es Light Agent, l'assistant personnel intelligent et bienveillant de l'application Light.
                    Date du jour : $today.
                    Tu réponds toujours en français avec clarté, élégance et concision (style Apple, précis et motivant).
                    Adapte systématiquement ton genre grammatical (féminin ou masculin), ton vocabulaire professionnel (étudiant, entrepreneur, autodidacte, particulier ou profession personnalisée) et tes recommandations au profil détaillé fourni dans le contexte.
                    Dès que l'utilisateur te demande de consulter, créer, valider ou supprimer une tâche, d'ajouter un module/projet, de changer de thème dégradé ou de lancer un Pomodoro/chronomètre, appelle directement les outils disponibles sans demander de confirmation inutile (sauf ambiguïté réelle).
                    Après avoir exécuté un outil, résume concrètement l'action effectuée et propose la suite logique.
                    """.trimIndent()
                )
            }
        )
    }

    private var chatSession: Chat? = null

    fun resetConversation() {
        chatSession = null
    }

    suspend fun sendMessage(
        userPrompt: String,
        contextSnapshot: String,
        executor: StudyAgentToolExecutor
    ): AgentTurnResult {
        val chat = chatSession ?: agentModel.startChat().also { chatSession = it }
        val executedActions = mutableListOf<AgentActionLog>()

        val enrichedPrompt = if (chat.history.isEmpty()) {
            "[Profil & Contexte actuel : $contextSnapshot]\n\nDemande : $userPrompt"
        } else {
            userPrompt
        }

        return try {
            withTimeout(60_000L) {
                var response = chat.sendMessage(enrichedPrompt)
                var safetyHops = 0

                while (response.functionCalls.isNotEmpty() && safetyHops < 6) {
                    safetyHops++
                    val responseParts = mutableListOf<FunctionResponsePart>()

                    for (call in response.functionCalls) {
                        val args = call.args
                        val (resultJson, logLabel) = executeTool(call.name, args, executor)
                        executedActions.add(AgentActionLog(toolName = call.name, label = logLabel))
                        responseParts.add(FunctionResponsePart(call.name, resultJson))
                    }

                    response = chat.sendMessage(
                        content("user") {
                            responseParts.forEach { part(it) }
                        }
                    )
                }

                val finalText = response.text?.trim()?.takeIf { it.isNotBlank() }
                    ?: if (executedActions.isNotEmpty()) {
                        "C'est fait : ${executedActions.joinToString(", ") { it.label }}."
                    } else {
                        "Je n'ai pas pu formuler de réponse. Pouvez-vous reformuler ?"
                    }

                AgentTurnResult(replyText = finalText, actions = executedActions)
            }
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Agent turn timed out", e)
            throw IllegalStateException("L'agent IA a mis trop de temps à répondre (60s). Réessayez.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Agent chat error", e)
            throw e
        }
    }

    private suspend fun executeTool(
        name: String,
        args: Map<String, JsonElement>,
        executor: StudyAgentToolExecutor
    ): Pair<JsonObject, String> {
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

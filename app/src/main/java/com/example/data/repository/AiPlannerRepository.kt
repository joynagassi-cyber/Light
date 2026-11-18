package com.example.data.repository

import android.util.Log
import com.example.data.model.AiPlannedTask
import com.example.data.model.AiStudyPlanResult
import com.example.data.model.Course
import com.example.data.model.TaskCategory
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.time.LocalDate

private const val TAG = "AiPlannerRepo"

class AiPlannerRepository {

    private val studyPlanSchema = Schema.obj(
        properties = mapOf(
            "summary" to Schema.string(
                description = "Résumé motivant et concis du plan d'étude en français (1 à 2 phrases)."
            ),
            "strategyTip" to Schema.string(
                description = "Conseil méthodologique adapté au génie logiciel (Feynman, Pomodoro, kata, tests)."
            ),
            "tasks" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "title" to Schema.string(description = "Titre actionnable et clair en français (max 80 caractères)."),
                        "notes" to Schema.string(description = "Sous-objectifs concrets ou notions clés."),
                        "courseCode" to Schema.string(description = "Code du cours le plus pertinent parmi ceux fournis."),
                        "dueDateIso" to Schema.string(description = "Date YYYY-MM-DD entre aujourd'hui et l'échéance."),
                        "scheduledTime" to Schema.string(description = "Créneau horaire HH:mm (ex: 09:00, 14:30, 18:00)."),
                        "priority" to Schema.enumeration(
                            values = listOf("high", "medium", "low"),
                            description = "Priorité de la tâche."
                        ),
                        "category" to Schema.enumeration(
                            values = listOf("exam", "project", "assignment", "revision", "reading"),
                            description = "Catégorie : exam (partiel/contrôle), project (projet dev), assignment (TP/TD/rendu), revision (synthèse/fiches), reading (lecture/doc)."
                        ),
                        "estimatedMinutes" to Schema.integer(description = "Durée estimée entre 20 et 90 minutes.")
                    )
                ),
                description = "Liste de 3 à 6 sessions d'étude structurées et progressives."
            )
        )
    )

    private val plannerModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-2.5-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = studyPlanSchema
            }
        )
    }

    suspend fun generateSmartStudyPlan(
        goalPrompt: String,
        targetDateIso: String,
        dailyMinutesAvailable: Int,
        courses: List<Course>,
        personaInstruction: String = ""
    ): AiStudyPlanResult {
        val today = LocalDate.now().toString()
        val courseListText = if (courses.isEmpty()) {
            "GL-301 Architecture Logicielle, GL-302 Systèmes Distribués, GL-303 Bases de Données, GL-304 Algorithmique"
        } else {
            courses.joinToString(", ") { "${it.code} (${it.title})" }
        }
        val profileLine = if (personaInstruction.isNotBlank()) {
            "Profil de l'utilisateur : $personaInstruction"
        } else {
            "Profil de l'utilisateur : Étudiante en 3ᵉ année d'ingénierie Génie Logiciel."
        }

        val fullPrompt = """
            Tu es un coach de planification d'élite dans l'application Light.
            $profileLine
            Date d'aujourd'hui : $today
            Date cible / échéance : $targetDateIso
            Temps disponible par jour : $dailyMinutesAvailable minutes
            Modules / Projets actuels : $courseListText
            Objectif exprimé : "$goalPrompt"

            Génère un plan intelligent, équilibré, progressif et réaliste en français (accordé au genre de l'utilisateur et adapté à son âge et sa profession), découpé en 3 à 6 blocs actionnables.
            Assure-toi que chaque dueDateIso soit au format YYYY-MM-DD compris entre $today et $targetDateIso.
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                plannerModel.generateContent(fullPrompt)
            }
            val jsonText = response.text
                ?: throw IllegalStateException("Le modèle IA a renvoyé une réponse vide.")

            val root = JSONObject(jsonText)
            val summary = root.optString("summary", "Plan d'étude intelligent généré.")
            val strategyTip = root.optString("strategyTip", "Alternez blocs de 50 minutes de code et 10 minutes de pause.")
            val tasksArray = root.getJSONArray("tasks")
            val parsedTasks = (0 until tasksArray.length()).map { idx ->
                val obj = tasksArray.getJSONObject(idx)
                val rawDate = obj.optString("dueDateIso", today)
                val safeDate = if (Regex("""^\d{4}-\d{2}-\d{2}$""").matches(rawDate)) rawDate else today
                val rawPriority = obj.optString("priority", "medium")
                val safePriority = if (rawPriority in listOf("high", "medium", "low")) rawPriority else "medium"
                val safeCategory = TaskCategory.sanitizeKey(obj.optString("category", TaskCategory.REVISION.key))
                AiPlannedTask(
                    title = obj.optString("title", "Session d'étude").take(140),
                    notes = obj.optString("notes", "").take(500),
                    courseCode = obj.optString("courseCode", courses.firstOrNull()?.code ?: "GL-301"),
                    dueDateIso = safeDate,
                    scheduledTime = obj.optString("scheduledTime", "10:00").take(10),
                    priority = safePriority,
                    category = safeCategory,
                    estimatedMinutes = obj.optInt("estimatedMinutes", 45).coerceIn(15, 120)
                )
            }
            AiStudyPlanResult(
                summary = summary,
                strategyTip = strategyTip,
                tasks = parsedTasks
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "AI study plan generation timed out", e)
            throw IllegalStateException("La génération IA a expiré après 60s. Vérifiez votre connexion et réessayez.")
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Failed to generate smart study plan", e)
            val msg = e.localizedMessage.orEmpty()
            if (msg.contains("Unable to resolve host", ignoreCase = true) ||
                msg.contains("network", ignoreCase = true) ||
                msg.contains("offline", ignoreCase = true) ||
                e is java.net.UnknownHostException ||
                e is java.io.IOException
            ) {
                throw IllegalStateException("Connexion Internet requise pour générer un plan IA. Votre planning existant reste disponible hors ligne.")
            }
            throw e
        }
    }
}

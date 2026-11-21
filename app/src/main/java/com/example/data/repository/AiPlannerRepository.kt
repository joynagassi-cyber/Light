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
import org.json.JSONException
import org.json.JSONObject
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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

    suspend fun generateSmartStudyPlan(
        goalPrompt: String,
        targetDateIso: String,
        dailyMinutesAvailable: Int,
        courses: List<Course>,
        personaInstruction: String = "",
        modelName: String,
        externalCompletion: (suspend (String) -> String)? = null
    ): AiStudyPlanResult {
        require(modelName.isNotBlank()) { "Sélectionnez d’abord un modèle dans les réglages de Light Agent." }
        val today = LocalDate.now()
        val deadline = runCatching { LocalDate.parse(targetDateIso) }.getOrNull()
        require(deadline != null && !deadline.isBefore(today)) { "Choisissez une échéance à partir d’aujourd’hui." }
        val courseListText = if (courses.isEmpty()) {
            "Aucun cours enregistré"
        } else {
            courses.joinToString(", ") { "${it.code} (${it.title})" }
        }
        val profileLine = if (personaInstruction.isNotBlank()) {
            "Profil de l'utilisateur : $personaInstruction"
        } else {
            ""
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
            Réponds uniquement par un objet JSON avec summary, strategyTip et tasks. Chaque tâche contient title,
            notes, courseCode, dueDateIso, scheduledTime (HH:mm), priority (high, medium, low), category et estimatedMinutes.
            Assure-toi que chaque dueDateIso soit au format YYYY-MM-DD compris entre $today et $targetDateIso.
        """.trimIndent()

        return try {
            val response = withTimeout(60_000L) {
                if (externalCompletion != null) externalCompletion(fullPrompt)
                else Firebase.ai.generativeModel(
                    modelName = modelName.trim(),
                    generationConfig = generationConfig {
                        responseMimeType = "application/json"
                        responseSchema = studyPlanSchema
                    }
                ).generateContent(fullPrompt).text ?: error("Le modèle IA a renvoyé une réponse vide.")
            }.trim()
            decodeStudyPlan(response, today, deadline, courses)
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

private val planTimeFormat = DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT)

/** Validate the response before it becomes a preview or can be persisted. */
internal fun decodeStudyPlan(
    response: String, today: LocalDate, deadline: LocalDate, courses: List<Course>,
): AiStudyPlanResult {
    val text = response.trim()
    val json = if (text.startsWith("```") && text.endsWith("```") && '\n' in text) {
        text.substringAfter('\n').dropLast(3).trim()
    } else text
    try {
        val root = JSONObject(json)
        val tasks = root.getJSONArray("tasks")
        require(tasks.length() in 1..6) { "Le modèle actif doit renvoyer entre une et six tâches." }
        val parsed = (0 until tasks.length()).map { index ->
            val item = tasks.getJSONObject(index)
            val date = runCatching { LocalDate.parse(item.optString("dueDateIso")) }.getOrDefault(today)
            val time = runCatching { LocalTime.parse(item.optString("scheduledTime")) }.getOrNull()
            val course = item.optString("courseCode", "").trim()
            val title = (item.opt("title") as? String).orEmpty().trim()
            require(title.isNotEmpty()) {
                "Le modèle actif a renvoyé une tâche sans titre. Réessayez."
            }
            AiPlannedTask(
                title = title.take(140),
                notes = (item.opt("notes") as? String).orEmpty().trim().take(500),
                courseCode = courses.firstOrNull { it.code.equals(course, ignoreCase = true) }?.code.orEmpty(),
                dueDateIso = date.coerceIn(today, deadline).toString(),
                scheduledTime = time?.format(planTimeFormat) ?: "10:00",
                priority = item.optString("priority").takeIf { it in listOf("high", "medium", "low") } ?: "medium",
                category = TaskCategory.sanitizeKey(item.optString("category")),
                estimatedMinutes = item.optInt("estimatedMinutes", 45).coerceIn(15, 120),
            )
        }
        return AiStudyPlanResult(
            summary = (root.opt("summary") as? String)?.trim()?.takeIf(String::isNotEmpty)?.take(500) ?: "Plan d'étude intelligent généré.",
            strategyTip = (root.opt("strategyTip") as? String)?.trim()?.takeIf(String::isNotEmpty)?.take(500) ?: "Alternez des séances et de courtes pauses.",
            tasks = parsed,
        )
    } catch (_: JSONException) {
        // The provider payload may contain private user data; do not expose it in the error or logs.
        throw IllegalArgumentException("Le modèle actif a renvoyé un plan illisible. Réessayez ou choisissez un autre modèle.")
    }
}

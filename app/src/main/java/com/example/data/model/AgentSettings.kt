package com.example.data.model

data class AgentSkill(
    val id: String,
    val name: String,
    val instructions: String,
    val enabled: Boolean = true
)

object AgentCapabilities {
    const val READ_TASKS = "read_tasks"
    const val CREATE_TASKS = "create_tasks"
    const val COMPLETE_TASKS = "complete_tasks"
    const val DELETE_TASKS = "delete_tasks"
    const val MANAGE_COURSES = "manage_courses"
    const val MANAGE_PROJECTS = "manage_projects"
    const val CHANGE_THEME = "change_theme"
    const val START_FOCUS = "start_focus"

    val all = listOf(
        READ_TASKS to "Consulter le planning",
        CREATE_TASKS to "Créer des tâches",
        COMPLETE_TASKS to "Valider des tâches",
        DELETE_TASKS to "Supprimer des tâches",
        MANAGE_COURSES to "Gérer les matières",
        MANAGE_PROJECTS to "Créer des projets",
        CHANGE_THEME to "Changer le thème",
        START_FOCUS to "Lancer une session Focus"
    )

    val defaults = all.map { it.first }.filterNot { it == DELETE_TASKS }.toSet()
}

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
    const val MANAGE_HABITS = "manage_habits"
    const val MANAGE_COUNTDOWNS = "manage_countdowns"
    const val READ_STUDY_DATA = "read_study_data"
    const val SHOW_STUDY_PLAN = "show_study_plan"
    const val WEB_SEARCH = "web_search"
    const val ACADEMIC_SEARCH = "academic_search"
    const val USE_COMPOSIO = "use_composio"

    val all = listOf(
        READ_TASKS to "Consulter le planning",
        CREATE_TASKS to "Créer des tâches",
        COMPLETE_TASKS to "Valider des tâches",
        DELETE_TASKS to "Supprimer des tâches",
        MANAGE_COURSES to "Ajouter des matières",
        MANAGE_PROJECTS to "Créer des projets",
        CHANGE_THEME to "Changer le thème",
        START_FOCUS to "Lancer une session Focus",
        MANAGE_HABITS to "Créer des habitudes",
        MANAGE_COUNTDOWNS to "Créer des échéances",
        READ_STUDY_DATA to "Consulter matières, projets, habitudes et échéances",
        SHOW_STUDY_PLAN to "Afficher des plans structurés",
        WEB_SEARCH to "Rechercher sur le web (nécessite la recherche activée et une clé)",
        ACADEMIC_SEARCH to "Rechercher des publications arXiv (sans clé)",
        USE_COMPOSIO to "Utiliser les connecteurs Composio (Google Super, Notion — clé locale)"
    )

    val descriptions = mapOf(
        READ_TASKS to "Voir les tâches et leur état dans votre planning.",
        CREATE_TASKS to "Ajouter des tâches d'étude au calendrier.",
        COMPLETE_TASKS to "Marquer une tâche existante comme terminée.",
        DELETE_TASKS to "Supprimer une tâche. À désactiver si vous ne souhaitez aucune suppression par l'agent.",
        MANAGE_COURSES to "Ajouter une matière à votre espace.",
        MANAGE_PROJECTS to "Créer un projet de travail.",
        CHANGE_THEME to "Modifier les couleurs de l'application.",
        START_FOCUS to "Démarrer immédiatement un minuteur Focus.",
        MANAGE_HABITS to "Ajouter une habitude quotidienne. L'agent ne peut pas valider ni supprimer une habitude.",
        MANAGE_COUNTDOWNS to "Ajouter une échéance ou un jalon. L'agent ne peut pas supprimer vos événements.",
        READ_STUDY_DATA to "Consulter les données affichées dans Light, sans pouvoir les modifier.",
        SHOW_STUDY_PLAN to "Afficher un aperçu en cartes Compose à partir d'un schéma fixe. Aucun élément de ce plan n'est exécuté automatiquement.",
        WEB_SEARCH to "Recherche bornée sur le web via Tavily ou Exa : activez « Recherche & Images » et ajoutez une clé locale. L'assistant cite toujours l'URL des sources. fetch_page lit le texte d'une page https publique.",
        ACADEMIC_SEARCH to "Recherche de publications sur arXiv (API ouverte, sans compte). Les liens pointent vers la page de l'article.",
        USE_COMPOSIO to "Découvrir (composio_search_tools) puis exécuter (composio_execute) des outils des connecteurs autorisés — Google Super et Notion. Toute exécution exige votre confirmation. Les comptes se connectent via le tableau de bord Composio (app.composio.dev)."
    )

    // Les capacités de recherche (web / arXiv) sont opt-in : désactivées par défaut.
    val defaults = all.map { it.first }.filterNot { it == DELETE_TASKS || it == WEB_SEARCH || it == ACADEMIC_SEARCH || it == USE_COMPOSIO }.toSet()
}

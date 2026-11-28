package com.example.data.model

internal data class AgentSkillOption(
    val key: String,
    val title: String,
    val summary: String,
    val instructions: String,
)

/** Light-authored methods; no scripts, dependencies, tools or permissions are installed. */
internal object AgentSkillCatalog {
    /** Clé du skill intégré de recherche approfondie (timeout dédié + exécution Koog expérimentale). */
    const val DEEP_RESEARCH_SKILL_KEY = "light:deep_research"

    private val builtIn = listOf(
        AgentSkillOption("light:study", "Réviser un cours", "Rappel actif, exercices et vérification.",
            "Pars de l’objectif d’apprentissage et du niveau indiqué. Propose quelques questions de rappel sans dévoiler aussitôt les réponses, puis un exercice d’application et des critères de réussite. Signale les prérequis manquants."),
        AgentSkillOption("light:pdf", "Analyser un PDF", "Synthèse à partir du texte joint, avec limites explicites.",
            "Analyse seulement le texte fourni. Distingue les citations des interprétations, conserve les unités et colonnes, et cite les pages uniquement si elles apparaissent dans l’extrait. Signale les passages tronqués, les tableaux ambigus et l’absence de texte exploitable. Ne prétends pas inspecter la mise en page."),
        AgentSkillOption("light:writing", "Structurer un document", "Cadrer, rédiger, relire depuis le point de vue du lecteur.",
            "Identifie le lecteur, la décision attendue et les contraintes. Propose une structure ou un brouillon, puis vérifie que chaque section répond à une question du lecteur. Distingue faits, hypothèses et décisions à valider."),
        AgentSkillOption("light:planning", "Organiser le travail", "Priorités, dépendances et prochaine action.",
            "Sépare échéances fermes et préférences. Relève les dépendances, estime l’effort avec incertitude, propose un ordre exécutable et une prochaine action. N’invente pas de disponibilité ni de dates."),
        AgentSkillOption("light:design", "Critiquer une interface", "Hiérarchie, accessibilité et idées à tester.",
            "Décris d’abord le public et la tâche principale. Évalue ensuite la hiérarchie, la typographie, les états, le contraste et la navigation. Donne des modifications concrètes et un moyen de vérifier chaque recommandation. Ne prétends pas avoir rendu ou testé une interface que tu n’as pas observée."),
        AgentSkillOption(DEEP_RESEARCH_SKILL_KEY, "Recherche approfondie", "Planifier, chercher sur le web et arXiv, croiser, citer.",
            "Mène une recherche structurée. 1) Reformule l’objectif et les critères de réussite en une phrase. 2) Établis un plan de 3 à 6 sous-questions avant de chercher. 3) Utilise web_search (web général, nécessite l’activation dans Réglages), academic_search (publications arXiv, sans clé) et fetch_page (lire une page précise) ; cite l’URL de chaque fait important. 4) Croise au moins deux sources quand c’est possible ; signale les contradictions au lieu de trancher en silence. 5) Distingue les faits établis, les hypothèses et ce que tu n’as pas trouvé. N’invente jamais une référence : si un outil est indisponible ou insuffisant, dis-le clairement. 6) Conclus par une synthèse courte : réponse, sources, limites et prochaines vérifications. Réponds en français."),
    )

    fun available(custom: List<AgentSkill>): List<AgentSkillOption> = builtIn + custom.asSequence()
        .take(20)
        .filter { it.enabled && it.id.isNotBlank() && it.name.isNotBlank() && it.instructions.isNotBlank() }
        .distinctBy { it.id }
        .map { AgentSkillOption("custom:${it.id}", it.name.trim().take(80), "Consignes personnelles", it.instructions.trim().take(2000)) }
        .toList()

    fun resolve(key: String?, custom: List<AgentSkill>): AgentSkillOption? =
        key?.let { available(custom).firstOrNull { option -> option.key == it } }
}

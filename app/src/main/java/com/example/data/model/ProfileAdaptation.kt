package com.example.data.model

data class AdaptiveHabitTemplate(
    val title: String,
    val subtitle: String,
    val category: HabitCategory,
    val iconKey: String,
    val colorIndex: Long
)

data class AdaptiveModuleTemplate(
    val id: String,
    val code: String,
    val title: String,
    val lead: String,
    val colorIndex: Long,
    val targetMinutes: Long
)

data class ProfileAdaptation(
    val gender: UserGender,
    val profession: UserProfession,
    val roleLabel: String,
    val customProfession: String,
    val age: Int,
    val birthYear: Int,
    val headerSubtitle: String,
    val greetingLead: String,
    val readyAdjective: String,
    val ageBadge: String,
    val workspaceSectionTitle: String,
    val scheduleSectionTitle: String,
    val taskModuleLabel: String,
    val metricScoreLabel: String,
    val defaultGoalPlaceholder: String,
    val heroQuote: String,
    val recommendedPomodoroMinutes: Int,
    val aiPersonaInstruction: String,
    val notificationSummary: String
)

fun UserProfile?.adaptation(): ProfileAdaptation {
    val gender = UserGender.fromKey(this?.gender)
    val profession = UserProfession.fromKey(this?.profession)
    val custom = this?.customProfession?.trim().orEmpty()
    val roleLabel = profession.labelFor(gender, custom)
    val currentYear = java.time.LocalDate.now().year
    val birthYear = (this?.birthYear?.toInt() ?: (currentYear - 21)).coerceIn(1930, currentYear - 6)
    val age = (this?.age?.toInt() ?: (currentYear - birthYear)).coerceIn(6, 100)
    val major = this?.major?.trim().takeIf { !it.isNullOrBlank() } ?: profession.defaultFocusDomain()

    val freq = NotificationFrequency.fromKey(this?.notificationFrequency)
    val notifEnabled = this?.notificationsEnabled ?: true
    val notifSummary = if (!notifEnabled || freq == NotificationFrequency.OFF) {
        "Notifications en pause"
    } else {
        "Rappels : ${freq.label}"
    }

    val isFem = gender == UserGender.FEMININE
    val readyAdj = if (isFem) "Prête" else "Prêt"

    val headerSubtitle = when {
        profession == UserProfession.OTHER && custom.isNotBlank() && major.equals(profession.defaultFocusDomain(), ignoreCase = true) ->
            custom
        else -> "$roleLabel · $major"
    }

    val workspaceTitle = when (profession) {
        UserProfession.STUDENT -> "Mes Matières"
        UserProfession.ENTREPRENEUR -> "Mes Pôles & Projets"
        UserProfession.SELF_TAUGHT -> "Mes Parcours d'Étude"
        UserProfession.INDIVIDUAL -> "Mes Domaines Perso"
        UserProfession.OTHER -> "Mes Projets (${roleLabel})"
    }

    val scheduleTitle = when (profession) {
        UserProfession.STUDENT -> "Programme du jour"
        UserProfession.ENTREPRENEUR -> "Feuille de route du jour"
        UserProfession.SELF_TAUGHT -> "Plan d'apprentissage du jour"
        UserProfession.INDIVIDUAL -> "Mon programme du jour"
        UserProfession.OTHER -> "Priorités du jour"
    }

    val taskModuleLabel = when (profession) {
        UserProfession.STUDENT -> "Matière associée"
        UserProfession.ENTREPRENEUR -> "Projet ou pôle associé"
        UserProfession.SELF_TAUGHT -> "Parcours ou thème associé"
        UserProfession.INDIVIDUAL, UserProfession.OTHER -> "Domaine associé"
    }

    val metricScoreLabel = when (profession) {
        UserProfession.STUDENT -> "Moyenne estimée"
        UserProfession.ENTREPRENEUR -> "Indice d'exécution"
        UserProfession.SELF_TAUGHT -> "Maîtrise acquise"
        UserProfession.INDIVIDUAL, UserProfession.OTHER -> "Indice d'avancement"
    }

    val defaultGoalPlaceholder = when (profession) {
        UserProfession.STUDENT -> "Ex : Maîtriser les chapitres Systèmes Distribués et UML avant le partiel"
        UserProfession.ENTREPRENEUR -> "Ex : Finaliser le MVP, préparer le pitch et valider 3 entretiens clients"
        UserProfession.SELF_TAUGHT -> "Ex : Terminer le module Kotlin Compose et livrer mon portfolio"
        UserProfession.INDIVIDUAL -> "Ex : Organiser ma semaine, lire 2 chapitres et avancer sur mon projet"
        UserProfession.OTHER -> "Ex : Structurer mes priorités de la semaine en $roleLabel"
    }

    val heroQuote = when (profession) {
        UserProfession.STUDENT -> if (age <= 24) {
            "\"Chaque session claire te rapproche de la maîtrise.\""
        } else {
            "\"La régularité transforme l'effort en expertise durable.\""
        }
        UserProfession.ENTREPRENEUR ->
            "\"Exécutez l'essentiel avec calme : la clarté crée l'élan.\""
        UserProfession.SELF_TAUGHT ->
            "\"Apprendre un peu chaque jour bâtit une liberté immense.\""
        UserProfession.INDIVIDUAL ->
            "\"De petits pas bien rythmés dessinent de grandes journées.\""
        UserProfession.OTHER ->
            "\"Votre méthode sur-mesure en $roleLabel commence ici.\""
    }

    val greetingLead = if (isFem) "Ravie de vous retrouver" else "Ravi de vous retrouver"
    val recommendedPomodoro = when (profession) {
        UserProfession.ENTREPRENEUR -> 45
        UserProfession.SELF_TAUGHT -> 35
        UserProfession.INDIVIDUAL -> 25
        else -> 25
    }

    val aiInstruction = buildString {
        append("L'utilisateur s'appelle ${this@adaptation?.displayName?.ifBlank { roleLabel } ?: roleLabel}, ")
        append("genre=${gender.label}, âge=$age ans (né(e) en $birthYear), ")
        append("profession=$roleLabel")
        if (custom.isNotBlank()) append(" (précision: $custom)")
        append(", domaine/filière='$major', fréquence notifications=${if (notifEnabled) freq.label else "désactivée"}. ")
        append("Accorde toujours tes phrases en français au genre ${gender.label.lowercase()} et adapte tes conseils, ")
        append("créneaux Focus, exemples et plans d'action à son profil de $roleLabel ($age ans).")
    }

    return ProfileAdaptation(
        gender = gender,
        profession = profession,
        roleLabel = roleLabel,
        customProfession = custom,
        age = age,
        birthYear = birthYear,
        headerSubtitle = headerSubtitle,
        greetingLead = greetingLead,
        readyAdjective = readyAdj,
        ageBadge = "$age ans · $birthYear",
        workspaceSectionTitle = workspaceTitle,
        scheduleSectionTitle = scheduleTitle,
        taskModuleLabel = taskModuleLabel,
        metricScoreLabel = metricScoreLabel,
        defaultGoalPlaceholder = defaultGoalPlaceholder,
        heroQuote = heroQuote,
        recommendedPomodoroMinutes = recommendedPomodoro,
        aiPersonaInstruction = aiInstruction,
        notificationSummary = notifSummary
    )
}

fun adaptiveHabitSuggestionsFor(profession: UserProfession): List<AdaptiveHabitTemplate> = when (profession) {
    UserProfession.STUDENT -> listOf(
        AdaptiveHabitTemplate("Fiches de synthèse", "Consolider les notions vues en cours", HabitCategory.STUDY, "book", 0L),
        AdaptiveHabitTemplate("Revue de code 20 min", "Relire ses TPs, commits et tests", HabitCategory.STUDY, "sparkles", 4L),
        AdaptiveHabitTemplate("Levé tôt", "Démarrer la journée avec sérénité", HabitCategory.HEALTH, "sun", 2L),
        AdaptiveHabitTemplate("Pause yeux 20-20-20", "Protéger sa vue loin des écrans", HabitCategory.HEALTH, "eye", 3L),
        AdaptiveHabitTemplate("Hydratation 1,5L", "Garder son énergie pendant les révisions", HabitCategory.HEALTH, "water", 4L)
    )
    UserProfession.ENTREPRENEUR -> listOf(
        AdaptiveHabitTemplate("Deep Work matin 90 min", "Avancer sur le produit avant les messages", HabitCategory.STUDY, "sparkles", 0L),
        AdaptiveHabitTemplate("Revue KPI & Priorités", "Aligner les 3 actions à fort impact", HabitCategory.LIFE, "book", 2L),
        AdaptiveHabitTemplate("Zéro distraction mobile", "Mode concentration activé en session", HabitCategory.MINDSET, "phone_off", 5L),
        AdaptiveHabitTemplate("Sport & énergie", "30 min d'activité pour tenir le rythme", HabitCategory.SPORTS, "fitness", 1L),
        AdaptiveHabitTemplate("Déconnexion le soir", "Préserver son sommeil avant 23h", HabitCategory.HEALTH, "moon", 3L)
    )
    UserProfession.SELF_TAUGHT -> listOf(
        AdaptiveHabitTemplate("Pratique guidée 45 min", "Coder ou pratiquer sans distraction", HabitCategory.STUDY, "sparkles", 0L),
        AdaptiveHabitTemplate("Journal d'apprentissage", "Noter ce qui a été compris aujourd'hui", HabitCategory.LIFE, "smile", 3L),
        AdaptiveHabitTemplate("Apprendre une nouveauté", "1 article technique ou doc officielle", HabitCategory.STUDY, "book", 4L),
        AdaptiveHabitTemplate("Langue & vocabulaire", "15 min de pratique quotidienne", HabitCategory.LIFE, "language", 1L),
        AdaptiveHabitTemplate("Pause yeux & posture", "S'étirer entre deux modules", HabitCategory.HEALTH, "eye", 2L)
    )
    UserProfession.INDIVIDUAL -> listOf(
        AdaptiveHabitTemplate("Planifier sa journée", "Choisir ses 3 priorités du matin", HabitCategory.LIFE, "smile", 0L),
        AdaptiveHabitTemplate("Lecture 20 min", "Un moment calme loin des écrans", HabitCategory.LIFE, "book", 4L),
        AdaptiveHabitTemplate("Hydratation 1,5L", "Prendre soin de son corps au quotidien", HabitCategory.HEALTH, "water", 3L),
        AdaptiveHabitTemplate("Mouvement quotidien", "Marche rapide ou étirements", HabitCategory.SPORTS, "fitness", 1L),
        AdaptiveHabitTemplate("Respiration & calme", "5 minutes de recentrage", HabitCategory.MINDSET, "mind", 5L)
    )
    UserProfession.OTHER -> listOf(
        AdaptiveHabitTemplate("Bloc Focus quotidien", "45 min de travail profond sans interruption", HabitCategory.STUDY, "sparkles", 0L),
        AdaptiveHabitTemplate("Bilan rapide du soir", "Célébrer ses avancées du jour", HabitCategory.MINDSET, "smile", 3L),
        AdaptiveHabitTemplate("Levé tôt & régulier", "Commencer la journée avec élan", HabitCategory.HEALTH, "sun", 2L),
        AdaptiveHabitTemplate("Hydratation & vitalité", "Boire régulièrement tout au long du jour", HabitCategory.HEALTH, "water", 4L),
        AdaptiveHabitTemplate("Activité physique", "Bouger au moins 20 min par jour", HabitCategory.SPORTS, "fitness", 1L)
    )
}

fun adaptiveStarterModulesFor(
    profession: UserProfession,
    customProfession: String
): List<AdaptiveModuleTemplate> = when (profession) {
    UserProfession.STUDENT -> listOf(
        AdaptiveModuleTemplate("gl301", "GL-301", "Architecture Logicielle & Design Patterns", "Prof. Laurent", 0L, 1440L),
        AdaptiveModuleTemplate("gl302", "GL-302", "Systèmes Distribués & Cloud", "Prof. Moreau", 1L, 1200L),
        AdaptiveModuleTemplate("gl303", "GL-303", "Bases de Données Avancées & NoSQL", "Prof. Benali", 2L, 1080L),
        AdaptiveModuleTemplate("gl304", "GL-304", "Algorithmique & Graphes", "Prof. Chen", 3L, 1320L),
        AdaptiveModuleTemplate("gl305", "GL-305", "Génie des Exigences & DevOps CI/CD", "Prof. Dupuis", 4L, 960L),
        AdaptiveModuleTemplate("gl306", "GL-306", "Systèmes d'Exploitation & Concurrence", "Prof. Girard", 5L, 1200L)
    )
    UserProfession.ENTREPRENEUR -> listOf(
        AdaptiveModuleTemplate("ent01", "PROD", "Produit, Roadmap & Design MVP", "Pôle Produit", 0L, 1800L),
        AdaptiveModuleTemplate("ent02", "GROW", "Acquisition, Clients & Marketing", "Pôle Croissance", 1L, 1200L),
        AdaptiveModuleTemplate("ent03", "OPS", "Finance, Juridique & Opérations", "Pôle Ops", 2L, 900L),
        AdaptiveModuleTemplate("ent04", "TECH", "Architecture & Automatisation", "Pôle Tech", 3L, 1440L),
        AdaptiveModuleTemplate("ent05", "LEAD", "Partenariats, Pitch & Stratégie", "Direction", 4L, 960L)
    )
    UserProfession.SELF_TAUGHT -> listOf(
        AdaptiveModuleTemplate("auto01", "CORE", "Fondamentaux & Théorie Appliquée", "Auto-formation", 0L, 1200L),
        AdaptiveModuleTemplate("auto02", "BUILD", "Projets Pratiques & Portfolio", "Ateliers", 1L, 1600L),
        AdaptiveModuleTemplate("auto03", "WATCH", "Veille, Lectures & Documentation", "Ressources", 2L, 720L),
        AdaptiveModuleTemplate("auto04", "SOFT", "Méthode, Langues & Prise de Notes", "Transversal", 3L, 840L)
    )
    UserProfession.INDIVIDUAL -> listOf(
        AdaptiveModuleTemplate("per01", "PERSO", "Organisation & Maison", "Quotidien", 0L, 600L),
        AdaptiveModuleTemplate("per02", "PROJ", "Projets Personnels & Créativité", "Initiatives", 1L, 960L),
        AdaptiveModuleTemplate("per03", "BIEN", "Santé, Énergie & Équilibre", "Bien-être", 2L, 720L),
        AdaptiveModuleTemplate("per04", "CULT", "Lectures & Découvertes", "Culture", 3L, 600L)
    )
    UserProfession.OTHER -> {
        val label = customProfession.trim().ifBlank { "Métier" }
        listOf(
            AdaptiveModuleTemplate("oth01", "COEUR", "Cœur d'activité · $label", "Prioritaire", 0L, 1440L),
            AdaptiveModuleTemplate("oth02", "DEV", "Perfectionnement & Outils", "Montée en compétence", 1L, 960L),
            AdaptiveModuleTemplate("oth03", "PLAN", "Coordination & Suivi", "Organisation", 2L, 720L),
            AdaptiveModuleTemplate("oth04", "EQUIL", "Énergie & Équilibre Personnel", "Bien-être", 3L, 600L)
        )
    }
}

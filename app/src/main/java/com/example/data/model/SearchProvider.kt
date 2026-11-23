package com.example.data.model

enum class SearchProvider(
    val id: String,
    val displayName: String,
    val docsUrl: String
) {
    TAVILY("tavily", "Tavily", "https://docs.tavily.com"),
    EXA("exa", "Exa", "https://docs.exa.ai"),
    NONE("none", "Désactivée", "");

    companion object {
        fun fromId(id: String?): SearchProvider =
            entries.firstOrNull { it.id == id } ?: NONE
    }
}

enum class ImageProvider(
    val id: String,
    val displayName: String,
    val docsUrl: String,
    val apiDocsUrl: String
) {
    OPENAI("openai", "OpenAI Images", "https://platform.openai.com/docs/guides/images", "https://api.openai.com/v1/images/generations"),
    OPENROUTER("openrouter", "OpenRouter (images)", "https://openrouter.ai/docs", "https://openrouter.ai/api/v1/images/generations"),
    NONE("none", "Désactivée", "", "");

    companion object {
        fun fromId(id: String?): ImageProvider =
            entries.firstOrNull { it.id == id } ?: NONE
    }
}

/**
 * Faits vs hypothèses (à date):
 * - OpenAI images: endpoint documenté et vérifiable ci-dessus.
 * - OpenRouter: propose un accès unifié à plusieurs modèles; endpoint images à confirmer par sa doc au moment d'usage.
 * - Agnes (apihub.agnes-ai.com): identité non vérifiée par source primaire indépendante; intégration suspendue jusqu'à identification.
 */
object ProviderVerificationNotes {
    const val AGNES_STATUS = "En attente d'identification — aucune intégration active tant que l'éditeur et la doc primaire ne sont pas établis."
    const val UNSPLASH_PROXY_NOTE = "Couvertures Unsplash: requête via proxy Cloud Function avec attribution obligatoire et tracking téléchargement (à déployer en prod)."
}

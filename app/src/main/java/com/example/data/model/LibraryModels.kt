package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class LibraryItemSource(val key: String, val label: String) {
    MANUAL("manual", "Manuel"),
    AGENT("agent", "Agent");

    companion object {
        fun fromKey(key: String?): LibraryItemSource =
            entries.firstOrNull { it.key == key } ?: MANUAL
        fun sanitize(key: String?): String = fromKey(key).key
    }
}

enum class LibraryItemCategory(val key: String, val label: String) {
    COURSE("course", "Cours"),
    READING("reading", "Lecture"),
    RESEARCH("research", "Recherche"),
    PROJECT("project", "Projet"),
    OTHER("other", "Autre");

    companion object {
        fun fromKey(key: String?): LibraryItemCategory =
            entries.firstOrNull { it.key == key } ?: OTHER
        fun sanitize(key: String?): String = fromKey(key).key
    }
}

enum class LibraryFolderKind(val key: String, val label: String) {
    MANUAL("manual", "Mes dossiers"),
    AGENT("agent", "Générés par l’agent");

    companion object {
        fun fromKey(key: String?): LibraryFolderKind =
            entries.firstOrNull { it.key == key } ?: MANUAL
        fun sanitize(key: String?): String = fromKey(key).key
    }
}

@IgnoreExtraProperties
data class LibraryFolder(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val kind: String = LibraryFolderKind.MANUAL.key,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class LibraryItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val folderId: String = "",
    val title: String = "",
    val category: String = LibraryItemCategory.OTHER.key,
    val source: String = LibraryItemSource.MANUAL.key,
    val mimeType: String = "markdown",
    /** Bounded extracted text content, stored as UTF-8 string (Firestore limit 1 MiB). */
    val textContent: String = "",
    /** Optional original file name for attribution. */
    val fileName: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val categoryEnum: LibraryItemCategory get() = LibraryItemCategory.fromKey(category)
    val sourceEnum: LibraryItemSource get() = LibraryItemSource.fromKey(source)
}

const val MAX_LIBRARY_TEXT_CHARS = 800_000
const val MAX_LIBRARY_TITLE_CHARS = 120
const val MAX_LIBRARY_FOLDER_NAME = 60

fun sanitizeLibraryTitle(raw: String): String =
    raw.trim().take(MAX_LIBRARY_TITLE_CHARS).ifBlank { "Document sans titre" }

fun sanitizeLibraryFolderName(raw: String): String =
    raw.trim().take(MAX_LIBRARY_FOLDER_NAME).ifBlank { "Dossier" }

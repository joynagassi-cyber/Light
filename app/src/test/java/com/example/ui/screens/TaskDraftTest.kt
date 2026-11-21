package com.example.ui.screens

import com.example.data.model.MAX_TASK_SUBTASKS
import com.example.data.model.normalizeSubtasks
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskDraftTest {
    @Test fun keepsReorderedNonBlankNamesInStorageOrder() {
        val draft = listOf("  Lire ", "", "Résumer", "Relire")
        assertEquals(listOf("Résumer", "Relire", "Lire"), normalizeSubtasks(moveDraftItem(draft, 0, 3)))
        assertEquals(draft, moveDraftItem(draft, 0, 4))
        assertEquals(MAX_TASK_SUBTASKS, normalizeSubtasks(List(10) { "Étape $it" }).size)
        assertEquals(120, normalizeSubtasks(listOf("x".repeat(121))).single().length)
    }
}

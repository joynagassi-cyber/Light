package com.example.data.firebase

import org.junit.Assert.assertEquals
import org.junit.Test

class FirestoreDiagnosticPathTest {
    @Test fun preservesCollectionAndMasksDocumentIds() {
        assertEquals("users/{id}/tasks/{id}", firestoreDiagnosticPath("users/uid123/tasks/privateTask"))
        assertEquals("users/{id}/focusSessions", firestoreDiagnosticPath("users/uid123/focusSessions"))
        assertEquals("{collection}/{id}", firestoreDiagnosticPath("unknown/email@example.test"))
        assertEquals(null, firestoreDiagnosticPath(null))
    }
}

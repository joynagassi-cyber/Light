package com.example.data.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import org.json.JSONObject

enum class OperationType(val value: String) {
    CREATE("create"),
    UPDATE("update"),
    DELETE("delete"),
    LIST("list"),
    GET("get"),
    WRITE("write"),
}

private val knownCollections = setOf("users", "courses", "tasks", "projects", "focusProfiles", "focusSessions", "habits", "countdowns")

internal fun firestoreDiagnosticPath(path: String?): String? = path?.substringBefore('?')?.trim('/')
    ?.split('/')?.mapIndexed { index, part ->
        if (index % 2 != 0) "{id}" else part.takeIf { it in knownCollections } ?: "{collection}"
    }?.joinToString("/")

fun handleFirestoreError(exception: Throwable, operationType: OperationType, path: String?): String {
    val code = generateSequence(exception) { it.cause }
        .filterIsInstance<FirebaseFirestoreException>().firstOrNull()?.code?.name ?: "UNKNOWN"
    // Identifies the request and status without logging account IDs, emails, payloads or raw errors.
    val diagnostic = JSONObject().apply {
        put("code", code)
        put("operationType", operationType.value)
        put("path", firestoreDiagnosticPath(path))
    }.toString()
    Log.e("FirestoreError", diagnostic)
    return diagnostic
}

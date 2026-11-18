package com.example.data.repository

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentToolResultTest {
    @Test
    fun `explicit unsuccessful result is not logged as success`() {
        assertFalse(agentToolResultSucceeded(JsonObject(mapOf("success" to JsonPrimitive(false)))))
    }

    @Test
    fun `explicit successful result is logged as success`() {
        assertTrue(agentToolResultSucceeded(JsonObject(mapOf("success" to JsonPrimitive(true)))))
    }

    @Test
    fun `read result without success field remains successful`() {
        assertTrue(agentToolResultSucceeded(JsonObject(mapOf("count" to JsonPrimitive(0)))))
    }

    @Test
    fun `error or cancellation without success field is not logged as success`() {
        assertFalse(agentToolResultSucceeded(JsonObject(mapOf("error" to JsonPrimitive("blocked")))))
        assertFalse(agentToolResultSucceeded(JsonObject(mapOf("cancelled" to JsonPrimitive(true)))))
    }

    @Test
    fun `malformed success field is conservatively treated as failure`() {
        assertFalse(agentToolResultSucceeded(JsonObject(mapOf("success" to JsonObject(emptyMap())))))
    }
}

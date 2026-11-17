package com.example.ui

import com.example.data.model.AgentCapabilities
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AgentCapabilitiesTest {
    @Test
    fun `default agent permissions cover only declared capabilities`() {
        val declared = AgentCapabilities.all.map { it.first }
        assertEquals(declared.size, declared.toSet().size)
        assertTrue(AgentCapabilities.defaults.all { it in declared })
        assertTrue(AgentCapabilities.DELETE_TASKS !in AgentCapabilities.defaults)
    }

    @Test
    fun `project creation is a separately declared capability`() {
        assertTrue(AgentCapabilities.MANAGE_PROJECTS in AgentCapabilities.defaults)
        assertTrue(AgentCapabilities.all.any { it.first == AgentCapabilities.MANAGE_PROJECTS })
    }
}

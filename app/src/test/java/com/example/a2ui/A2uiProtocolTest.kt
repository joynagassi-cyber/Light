package com.example.a2ui

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class A2uiProtocolTest {

    private val json = Json { ignoreUnknownKeys = true }

    private fun obj(raw: String): JsonObject = json.parseToJsonElement(raw).jsonObject

    @Test
    fun extractionReturnsCleanTextAndMessages() {
        val text = """
            Voici votre récapitulatif.
            ```a2ui
            {"version":"v0.9","createSurface":{"surfaceId":"s1","catalogId":"catalog","sendDataModel":true}}
            {"version":"v0.9","updateComponents":{"surfaceId":"s1","components":[{"id":"root","component":"Column","children":["t"]},{"id":"t","component":"Text","text":"Salut"}]}}
            ```
        """.trimIndent()
        val extraction = extractA2ui(text)
        assertEquals("Voici votre récapitulatif.", extraction.cleanText)
        assertEquals(1, extraction.blocks.size)
        val block = extraction.blocks.single()
        assertEquals(2, block.messages.size)
        assertTrue(block.messages[0] is A2uiServerMessage.CreateSurface)
        assertEquals(
            listOf("s1"),
            block.surfaceIds,
        )
        assertEquals(0, block.errors.size)
        val update = block.messages[1] as A2uiServerMessage.UpdateComponents
        assertEquals(2, update.components.size)
        assertEquals("Column", update.components[0].component)
        assertEquals("Salut", update.components[1].props["text"]?.jsonPrimitive?.contentOrNull)
    }

    @Test
    fun unterminatedFenceHidesPartialProtocolFromMarkdown() {
        val text = "Voici le début\n```a2ui\n{\"version\":\"v0.9\",\"createSurface\":{\"surfaceId\":\"s1\",\"catalogId\":\"c\"}}\n{\"version\":\"v0.9\",\"updateComp"
        val extraction = extractA2ui(text)
        assertEquals("Voici le début", extraction.cleanText)
        assertEquals(1, extraction.blocks.size)
        assertEquals(1, extraction.blocks.single().messages.size)
    }

    @Test
    fun garbageLineIsReportedButValidMessagesAreKept() {
        val text = """
            ```a2ui
            ceci n est pas du json
            {"version":"v0.9","deleteSurface":{"surfaceId":"s1"}}
            ```
        """.trimIndent()
        val extraction = extractA2ui(text)
        val block = extraction.blocks.single()
        assertEquals(1, block.messages.size)
        assertEquals(1, block.errors.size)
        assertEquals("", extraction.cleanText)
    }

    @Test
    fun plainTextPassesThroughWithoutBlocks() {
        val extraction = extractA2ui("Réponds-moi simplement.")
        assertEquals("Réponds-moi simplement.", extraction.cleanText)
        assertTrue(extraction.blocks.isEmpty())
    }

    @Test
    fun updateDataModelParsesPathValueAndDeletion() {
        val text = """
            ```a2ui
            {"version":"v0.9","updateDataModel":{"surfaceId":"s1","path":"/compteur","value":42}}
            {"version":"v0.9","updateDataModel":{"surfaceId":"s1","path":"/compteur"}}
            ```
        """.trimIndent()
        val messages = extractA2ui(text).blocks.single().messages
        val set = messages[0] as A2uiServerMessage.UpdateDataModel
        assertEquals("/compteur", set.path)
        assertTrue(set.hasValue)
        assertEquals(42.0, set.value?.jsonPrimitive?.doubleOrNullSafe() ?: -1.0, 0.0)
        val remove = messages[1] as A2uiServerMessage.UpdateDataModel
        assertFalse(remove.hasValue)
    }

    private fun kotlinx.serialization.json.JsonPrimitive.doubleOrNullSafe(): Double? =
        content.toDoubleOrNull()

    @Test
    fun jsonPointerSetAndGetRoundTrip() {
        val root = JsonObject(emptyMap())
        val withCount = jsonPointerSet(root, "/user/profile/name", JsonPrimitive("Ada"))
        assertEquals("Ada", jsonPointerGet(withCount, "/user/profile/name")?.jsonPrimitive?.contentOrNull)
        // Suppression
        val removed = jsonPointerSet(withCount, "/user/profile/name", null)
        assertNull(jsonPointerGet(removed, "/user/profile/name"))
        assertEquals("Ada", jsonPointerGet(withCount, "/user/profile/name")?.jsonPrimitive?.contentOrNull)
        // Racine
        val cleared = jsonPointerSet(withCount, "/", null)
        assertTrue(cleared.isEmpty())
        // Index de tableau
        val withList = jsonPointerSet(JsonObject(emptyMap()), "/items", JsonArray(listOf(JsonPrimitive("a"), JsonPrimitive("b"))))
        assertEquals("b", jsonPointerGet(withList, "/items/1")?.jsonPrimitive?.contentOrNull)
    }

    @Test
    fun resolveDynamicHandlesLiteralsPathsAndFormatString() {
        val model = obj("""{"user":{"name":"Ada"},"items":[{"label":"Un"}]}""")
        // Littéral
        assertEquals("bonjour", resolveDynamicString(JsonPrimitive("bonjour"), model))
        // Chemin absolu
        assertEquals("Ada", resolveDynamicString(obj("""{"path":"/user/name"}"""), model))
        // Chemin relatif contre une base de template
        assertEquals("Un", resolveDynamicString(obj("""{"path":"label"}"""), model, "/items/0"))
        // formatString
        assertEquals(
            "Bonjour Ada !",
            resolveDynamicString(
                obj("""{"call":"formatString","args":{"value":"Bonjour ${'$'}{/user/name} !"},"returnType":"string"}"""),
                model,
            ),
        )
        // Fonction inconnue
        assertNull(resolveDynamicString(obj("""{"call":"hack","args":{}}"""), model))
    }

    @Test
    fun childListReferencesExpandStaticAndTemplateLists() {
        val model = obj("""{"items":[{"name":"a"},{"name":"b"}]}""")
        // Statique
        val static = childListReferences(JsonArray(listOf(JsonPrimitive("c1"), JsonPrimitive("c2"))), model)
        assertEquals(listOf("c1" to "", "c2" to ""), static)
        // Template
        val template = childListReferences(obj("""{"componentId":"item_row","path":"/items"}"""), model)
        assertEquals(listOf("item_row" to "/items/0", "item_row" to "/items/1"), template)
        // Chemin manquant
        assertEquals(emptyList<Pair<String, String>>(), childListReferences(obj("""{"componentId":"x","path":"/nope"}"""), model))
    }

    @Test
    fun serializeUserActionProducesNavigableJson() {
        val model = obj("""{"count":3}""")
        val context = resolveActionContext(obj("""{"reponse":{"path":"/count"},"label":"ok"}"""), model)
        val line = serializeUserAction("surface", "composant", "plan_confirme", context)
        val parsed = json.parseToJsonElement(line).jsonObject
        val action = parsed["a2uiUserAction"]!!.jsonObject
        assertEquals("surface", action["surfaceId"]?.jsonPrimitive?.contentOrNull)
        assertEquals("composant", action["componentId"]?.jsonPrimitive?.contentOrNull)
        assertEquals("plan_confirme", action["event"]?.jsonPrimitive?.contentOrNull)
        assertEquals(3.0, action["context"]!!.jsonObject["reponse"]?.jsonPrimitive?.doubleOrNullSafe() ?: -1.0, 0.0)
        assertEquals("ok", action["context"]!!.jsonObject["label"]?.jsonPrimitive?.contentOrNull)
    }

    @Test
    fun bindingPathResolvesRelativeAndAbsolute() {
        assertEquals("/items/2/name", bindingPath(obj("""{"path":"name"}"""), "/items/2"))
        assertEquals("/absolu", bindingPath(obj("""{"path":"/absolu"}"""), "/items/2"))
        assertNull(bindingPath(JsonPrimitive("texte")))
    }
}

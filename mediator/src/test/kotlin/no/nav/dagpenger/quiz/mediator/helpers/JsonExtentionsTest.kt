package no.nav.dagpenger.quiz.mediator.helpers

import no.nav.dagpenger.quiz.mediator.helpers.JsonExtentions.toPrettyJson
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class JsonExtentionsTest {
    private val uformatertJson = """{"id": "faktum.faktumFraGrunnNivå","type": "tekst"}"""
    private val forventetJson = """
            {
              "id" : "faktum.faktumFraGrunnNivå",
              "type" : "tekst"
            }
    """.trimIndent()

    @Test
    fun `Skal kunne pretty formatere en string`() {
        val formatertJson = uformatertJson.toPrettyJson()
        assertEquals(forventetJson, formatertJson)
    }
}

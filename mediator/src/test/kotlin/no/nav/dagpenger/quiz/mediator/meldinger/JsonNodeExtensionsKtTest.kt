package no.nav.dagpenger.quiz.mediator.meldinger

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.dagpenger.model.faktum.Valg
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class JsonNodeExtensionsKtTest {

    private val jsonMedEnListe = """
{
  "liste": [
      "valg1",
      "valg2"
  ]
}
    """.trimIndent()

    private val jsonMedTomListe = """
{
  "liste": []
}
    """.trimIndent()

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `Skal kunne konvertere en JsonNode til et Valg`() {
        val expectedValg = Valg("valg1", "valg2")
        val jsonNodeMedListe = objectMapper.readValue<JsonNode>(jsonMedEnListe)

        val jsonListe = jsonNodeMedListe["liste"]
        val valg = jsonListe.asValg()

        assertEquals(expectedValg, valg)
    }

    @Test
    fun `Skal ikke kunne opprette Valg uten svaralternativer`() {
        val jsonNodeUtenInnhold = objectMapper.readValue<JsonNode>(jsonMedTomListe)

        val tomJsonListe = jsonNodeUtenInnhold["liste"]

        assertThrows<IllegalArgumentException> { tomJsonListe.asValg() }
    }
}

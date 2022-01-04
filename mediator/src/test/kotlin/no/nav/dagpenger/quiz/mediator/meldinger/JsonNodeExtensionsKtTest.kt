package no.nav.dagpenger.quiz.mediator.meldinger

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.dagpenger.model.faktum.Valg
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class JsonNodeExtensionsKtTest {

    private val jsonMedSvarliste = """
{
  "svar": [
      "valg1",
      "valg2"
  ]
}
    """.trimIndent()

    private val jsonMedTomSvarliste = """
{
  "svar": []
}
    """.trimIndent()

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `Skal kunne konvertere en JsonNode til et Valg`() {
        val expectedValg = Valg("valg1", "valg2")
        val faktumNodeMedSvarliste = objectMapper.readValue<JsonNode>(jsonMedSvarliste)

        val svarNode = faktumNodeMedSvarliste["svar"]
        val valg = svarNode.asValg()

        assertEquals(expectedValg, valg)
    }

    @Test
    fun `Skal ikke kunne opprette Valg uten svaralternativer`() {
        val faktumNodeMedTomSvarliste = objectMapper.readValue<JsonNode>(jsonMedTomSvarliste)

        val svarMedTomListe = faktumNodeMedTomSvarliste["svar"]

        assertThrows<IllegalArgumentException> { svarMedTomListe.asValg() }
    }
}

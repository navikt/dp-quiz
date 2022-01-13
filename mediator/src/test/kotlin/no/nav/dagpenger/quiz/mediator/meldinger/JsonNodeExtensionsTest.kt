package no.nav.dagpenger.quiz.mediator.meldinger

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Flervalg
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class JsonNodeExtensionsTest {

    private val jsonMedSvarlisteMedEtValg = """
{
  "svar": [
      "valg2"
  ]
}
    """.trimIndent()

    private val jsonMedSvarlisteMedFlereValg = """
{
  "svar": [
      "valg1",
      "valg2"
  ]
}
    """.trimIndent()

    private val objectMapper = jacksonObjectMapper()

    @Test
    fun `Skal kunne konvertere en JsonNode til et Envalg`() {
        val forventedeValg = Envalg("valg2")
        val faktumNodeMedSvarliste = objectMapper.readValue<JsonNode>(jsonMedSvarlisteMedEtValg)

        val svarNodeMedEtSvar = faktumNodeMedSvarliste["svar"]
        val valg = svarNodeMedEtSvar.asEnvalg()

        assertEquals(forventedeValg, valg)
    }

    @Test
    fun `Skal kunne konvertere en JsonNode til et Flervalg`() {
        val forventedeValg = Flervalg("valg1", "valg2")
        val faktumNodeMedSvarliste = objectMapper.readValue<JsonNode>(jsonMedSvarlisteMedFlereValg)

        val svarNode = faktumNodeMedSvarliste["svar"]
        val valg = svarNode.asFlervalg()

        assertEquals(forventedeValg, valg)
    }
}

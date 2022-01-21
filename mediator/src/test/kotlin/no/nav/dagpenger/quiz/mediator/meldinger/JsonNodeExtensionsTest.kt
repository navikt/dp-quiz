package no.nav.dagpenger.quiz.mediator.meldinger

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.quiz.mediator.helpers.februar
import no.nav.dagpenger.quiz.mediator.helpers.januar
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

    private val jsonMedSvartekst = """
{
  "svar": "Dette er et tekstsvar"
}
    """.trimIndent()

    private val jsonMedPeriodeISvaret = """
{
  "svar": {
      "fom": "2018-01-01",
      "tom": "2018-01-20"
  }
}
    """.trimIndent()

    private val jsonMedPågåendePeriodeISvaret = """
{
  "svar": {
      "fom": "2018-02-01",
      "tom": null
  }
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

    @Test
    fun `Skal konvertere et svar til en Tekst`() {
        val faktumNode = objectMapper.readValue<JsonNode>(jsonMedSvartekst)
        val svarnode = faktumNode["svar"]

        val tekstsvaret = svarnode.asTekst()

        assertEquals("Dette er et tekstsvar", tekstsvaret.verdi)
    }

    @Test
    fun `Skal konvertere et svar til en Periode`() {
        val faktumNode = objectMapper.readValue<JsonNode>(jsonMedPeriodeISvaret)
        val svarnode = faktumNode["svar"]

        val avsluttetPeriode = svarnode.asPeriode()

        val forventetPeriode = Periode(1.januar(), 20.januar())
        assertEquals(forventetPeriode, avsluttetPeriode)
    }

    @Test
    fun `Skal konvertere et svar til en pågående Periode`() {
        val faktumNode = objectMapper.readValue<JsonNode>(jsonMedPågåendePeriodeISvaret)
        val svarnode = faktumNode["svar"]

        val pågåendePeriode = svarnode.asPeriode()

        val forventetPeriode = Periode(1.februar())
        assertEquals(forventetPeriode, pågåendePeriode)
    }
}

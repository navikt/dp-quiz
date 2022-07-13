package no.nav.dagpenger.model.helpers

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon.navn
import no.nav.dagpenger.model.unit.marshalling.finnSeksjon
import kotlin.test.assertEquals
@DslMarker
annotation class JsonAssertMarker

@JsonAssertMarker
class MedSeksjon(private val seksjon: JsonNode) {
    fun fakta(block: MedFakta.() -> Unit) = fakta(true, block)
    fun fakta(sjekkAlle: Boolean, block: MedFakta.() -> Unit) {
        val fakta = seksjon["fakta"]
        val medFakta = MedFakta(fakta)
        block(medFakta)

        if (sjekkAlle) assertEquals(
            emptySet(),
            medFakta.faktaSomIkkeErSjekket,
            "Det er faktum som ikke er verifisert. Sjekk alle eller sett sjekkAlle til false"
        )
    }
}

@JsonAssertMarker
class MedFakta(private val fakta: JsonNode) {
    var faktaSomIkkeErSjekket = fakta.map { it["beskrivendeId"].asText() }.toMutableSet()

    fun antall(antall: Int) = antall(antall, null)
    fun antallReadOnly(antall: Int) = antall(antall, true)

    private fun antall(antall: Int, readOnly: Boolean?) =
        when (readOnly) {
            true -> assertEquals(antall, fakta.count { it["readOnly"].asBoolean() })
            false -> assertEquals(antall, fakta.count { !it["readOnly"].asBoolean() })
            null -> assertEquals(antall, fakta.size())
        }

    fun faktum(navn: String, block: MedFaktum.() -> Unit) {
        val faktum = fakta.finnFaktum(navn)

        block(MedFaktum(faktum))
        faktaSomIkkeErSjekket.remove(navn)
    }

    fun generatorFaktum(navn: String, block: MedGeneratorFaktum.() -> Unit) {
        val medGeneratorFaktum = MedGeneratorFaktum(fakta.finnFaktum(navn))
        medGeneratorFaktum.assertGenerator()

        block(medGeneratorFaktum)
        faktaSomIkkeErSjekket.remove(navn)
    }
}

@JsonAssertMarker
open class MedFaktum(val faktum: JsonNode) {
    fun readOnly(readOnly: Boolean = true) {
        assertEquals(readOnly, faktum["readOnly"].asBoolean(), "Faktum var ikke readOnly")
    }

    fun erType(type: String) {
        assertEquals(type, faktum["type"].asText())
    }

    fun besvartMed(boolsk: Boolean) = assertEquals(boolsk, faktum["svar"].asBoolean())
    fun besvartMed(int: Int) = assertEquals(int, faktum["svar"].asInt())
}

@JsonAssertMarker
class MedGeneratorFaktum(faktum: JsonNode) : MedFaktum(faktum) {
    internal fun assertGenerator() =
        assertEquals("generator", faktum["type"].asText(), "Faktum $navn er ikke en generator")

    fun antall(antallSvar: Int) = assertEquals(antallSvar, faktum["svar"].size())
    fun svar(i: Int, block: MedFakta.() -> Unit) {
        val medFakta = MedFakta(faktum["svar"][i - 1])

        block(medFakta)
    }
}

internal fun ObjectNode.medSeksjon(seksjonNavn: String, block: MedSeksjon.() -> Unit) {
    val seksjon = finnSeksjon(seksjonNavn)
    block(MedSeksjon(seksjon))
}

private fun JsonNode.finnFaktum(navn: String): JsonNode {
    val jsonNode = this.find { it["beskrivendeId"].asText() == navn }
    return checkNotNull(jsonNode) { "Fant ikke faktum med navn '$navn' i JSON \n ${this.toPrettyString()}" }
}

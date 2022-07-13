package no.nav.dagpenger.model.helpers

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon.navn
import no.nav.dagpenger.model.unit.marshalling.finnSeksjon
import kotlin.test.assertEquals

@DslMarker
internal annotation class JsonAssertMarker

@JsonAssertMarker
internal class MedSøknad(private val søknad: JsonNode, block: MedSøknad.() -> Unit) {
    var antallSeksjoner: Int
        get() = throw IllegalStateException()
        set(value) = assertEquals(value, søknad["seksjoner"].size())

    init {
        block(this)
    }

    fun seksjon(seksjonNavn: String, block: MedSeksjon.() -> Unit) {
        block(MedSeksjon(søknad.finnSeksjon(seksjonNavn)))
    }
}

@JsonAssertMarker
internal class MedSeksjon(private val seksjon: JsonNode) {
    var ferdig: Boolean
        get() = throw IllegalStateException()
        set(value) = assertEquals(value, seksjon["ferdig"].asBoolean())

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
internal class MedFakta(private val fakta: JsonNode) {
    internal var faktaSomIkkeErSjekket = fakta.map { it["beskrivendeId"].asText() }.toMutableSet()
    var antall: Int
        get() = throw IllegalStateException()
        set(value) = antall(value)
    var antallReadOnly: Int
        get() = throw IllegalStateException()
        set(value) = antall(value, true)
    var antallBesvarte: Int
        get() = throw IllegalStateException()
        set(value) = assertEquals(value, fakta.count { it.has("svar") }, "Feil antall faktum")

    private fun antall(antall: Int) = antall(antall, null)

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
internal open class MedFaktum(val faktum: JsonNode) {
    var besvart: Boolean
        get() = TODO()
        set(value) = assertEquals(value, faktum.has("svar"), "Faktumet er besvart")

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
internal class MedGeneratorFaktum(faktum: JsonNode) : MedFaktum(faktum) {
    var antall: Int
        get() = throw IllegalStateException()
        set(value) = antall(value)

    internal fun assertGenerator() =
        assertEquals("generator", faktum["type"].asText(), "Faktum $navn er ikke en generator")

    fun svar(i: Int, block: MedFakta.() -> Unit) {
        val medFakta = MedFakta(faktum["svar"][i - 1])

        block(medFakta)
    }
    private fun antall(antallSvar: Int) = assertEquals(antallSvar, faktum["svar"].size())
}

private fun JsonNode.finnFaktum(navn: String): JsonNode {
    val jsonNode = this.find { it["beskrivendeId"].asText() == navn }
    return checkNotNull(jsonNode) { "Fant ikke faktum med navn '$navn' i JSON \n ${this.toPrettyString()}" }
}

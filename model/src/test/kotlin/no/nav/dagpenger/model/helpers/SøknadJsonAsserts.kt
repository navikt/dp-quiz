package no.nav.dagpenger.model.helpers

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.model.marshalling.SøkerJsonBuilder
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon.navn
import no.nav.dagpenger.model.unit.marshalling.finnSeksjon
import kotlin.test.assertEquals

@DslMarker
internal annotation class JsonAssertMarker

@JsonAssertMarker
internal class MedSøknad private constructor(private val søknad: JsonNode, block: MedSøknad.() -> Unit) {
    constructor(
        søknadprosess: Søknadprosess,
        block: MedSøknad.() -> Unit
    ) : this(SøkerJsonBuilder(søknadprosess).resultat(), block)

    init {
        block(this)
    }

    fun harAntallSeksjoner(value: Int) = assertEquals(value, søknad["seksjoner"].size())

    fun erFerdig() = assertEquals(true, søknad["ferdig"].asBoolean())

    fun seksjon(seksjonNavn: String, block: MedSeksjon.() -> Unit) {
        block(MedSeksjon(søknad.finnSeksjon(seksjonNavn)))
    }
}

@JsonAssertMarker
internal class MedSeksjon(private val seksjon: JsonNode) {
    fun erFerdig() = assertEquals(true, seksjon["ferdig"].asBoolean())

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

    fun harAntallFakta(value: Int) = antall(value)
    fun harAntallReadOnly(value: Int) = antall(value, true)

    fun harAntallBesvarte(value: Int) =
        assertEquals(value, fakta.count { it.has("svar") }, "Feil antall faktum i seksjon")

    private fun antall(antall: Int) = antall(antall, null)

    private fun antall(antall: Int, readOnly: Boolean?) =
        when (readOnly) {
            true -> assertEquals(antall, fakta.count { it["readOnly"].asBoolean() })
            false -> assertEquals(antall, fakta.count { !it["readOnly"].asBoolean() })
            null -> assertEquals(antall, fakta.size())
        }

    fun dokument(navn: String, block: MedFaktum.() -> Unit) = faktum(navn, block).also {
        it.erType("dokument")
    }

    fun boolsk(navn: String, block: MedFaktum.() -> Unit) = faktum(navn, block).also {
        it.erType("boolean")
    }

    fun dato(navn: String, block: MedFaktum.() -> Unit) = faktum(navn, block).also {
        it.erType("localdate")
    }

    fun heltall(navn: String, block: MedFaktum.() -> Unit) = faktum(navn, block).also {
        it.erType("int")
    }

    fun land(navn: String, block: MedLandFaktum.() -> Unit) {
        val faktum = MedLandFaktum(fakta.finnFaktum(navn))
        faktum.erType("land")

        block(faktum)
        faktaSomIkkeErSjekket.remove(navn)
    }

    fun envalg(navn: String, block: MedEnvalgFaktum.() -> Unit) {
        val faktum = MedEnvalgFaktum(fakta.finnFaktum(navn))
        faktum.erType("envalg")

        block(faktum)
        faktaSomIkkeErSjekket.remove(navn)
    }

    fun flervalg(navn: String, block: MedFlervalgFaktum.() -> Unit) {
        val faktum = MedFlervalgFaktum(fakta.finnFaktum(navn))
        faktum.erType("flervalg")

        block(faktum)
        faktaSomIkkeErSjekket.remove(navn)
    }

    fun generator(navn: String, block: MedGeneratorFaktum.() -> Unit) {
        val faktum = MedGeneratorFaktum(fakta.finnFaktum(navn))
        faktum.erType("generator")

        block(faktum)
        faktaSomIkkeErSjekket.remove(navn)
    }

    private fun faktum(navn: String, block: MedFaktum.() -> Unit): MedFaktum {
        val medFaktum = MedFaktum(fakta.finnFaktum(navn))
        block(medFaktum)
        faktaSomIkkeErSjekket.remove(navn)
        return medFaktum
    }

    fun alle(block: MedFaktum.() -> Unit) = fakta.forEach { block(MedFaktum(it)) }
}

@JsonAssertMarker
internal open class MedFaktum(val faktum: JsonNode) {
    protected val navn = faktum["beskrivendeId"].asText()

    fun erBesvart() = erBesvart(true)
    fun erIkkeBesvart() = erBesvart(false)
    private fun erBesvart(boolsk: Boolean = true) = assertEquals(boolsk, faktum.has("svar"), "Faktum $navn er besvart")

    fun erReadOnly(readOnly: Boolean = true) =
        assertEquals(readOnly, faktum["readOnly"].asBoolean(), "Faktum $navn var ikke readOnly")

    internal fun erType(type: String) = assertEquals(type, faktum["type"].asText())

    fun harRoller(vararg rolle: String) = harRoller(rolle.toList())

    private fun harRoller(roller: List<String>) = faktum.get("roller").toSet().map { it.asText() }.also {
        assertEquals(roller, it)
    }

    fun erBesvartMed(boolsk: Boolean) = assertEquals(boolsk, faktum["svar"].asBoolean())

    open fun erBesvartMed(int: Int) = assertEquals(int, faktum["svar"].asInt(), "Faktum $navn er besvart med")
}

@JsonAssertMarker
internal class MedGeneratorFaktum(faktum: JsonNode) : MedFaktum(faktum) {
    private val svar = faktum["svar"]
    private val templates = faktum["templates"]
    fun svar(i: Int, block: MedFakta.() -> Unit) = MedFakta(svar[i - 1]).also { block(it) }

    fun templates(block: MedFakta.() -> Unit) = MedFakta(templates).also { block(it) }

    override fun erBesvartMed(antallSvar: Int) = assertEquals(antallSvar, svar.size())
    fun alle(block: MedFaktum.() -> Unit) = svar.flatten().forEach { block(MedFaktum(it)) }
}

@JsonAssertMarker
internal open class MedEnvalgFaktum(faktum: JsonNode) : MedFaktum(faktum) {
    fun harGyldigeValg(vararg valg: String) = harGyldigeValg(valg.toList())
    private fun harGyldigeValg(valg: List<String>) =
        faktum.get("gyldigeValg").toSet().map { it.asText() }
            .also { assertEquals(valg, it, "Valgfaktum ${this.navn} har ikke riktige valg") }
}

@JsonAssertMarker
internal class MedFlervalgFaktum(faktum: JsonNode) : MedEnvalgFaktum(faktum)

@JsonAssertMarker
internal open class MedLandFaktum(faktum: JsonNode) : MedFaktum(faktum) {
    fun harGrupper(vararg valg: String) = harGrupper(valg.toList())
    private fun harGrupper(grupper: List<String>) =
        faktum.get("grupper").toSet().map { it["gruppeId"].asText() }
            .also { assertEquals(grupper, it, "Landfaktum ${this.navn} har ikke riktige grupper") }
}

private fun JsonNode.finnFaktum(navn: String): JsonNode {
    val jsonNode = this.find { it["beskrivendeId"].asText() == navn }
    return checkNotNull(jsonNode) { "Fant ikke faktum med navn '$navn' i JSON \n ${this.toPrettyString()}" }
}

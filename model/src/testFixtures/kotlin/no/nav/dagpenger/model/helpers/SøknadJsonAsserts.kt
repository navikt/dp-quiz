package no.nav.dagpenger.model.helpers

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.model.marshalling.SøkerJsonBuilder
import no.nav.dagpenger.model.seksjon.Prosess
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import kotlin.test.assertContains

@DslMarker
internal annotation class JsonAssertMarker

@JsonAssertMarker
class MedSøknad private constructor(private val søknad: JsonNode, block: MedSøknad.() -> Unit) {
    constructor(
        prosess: Prosess,
        block: MedSøknad.() -> Unit
    ) : this(SøkerJsonBuilder(prosess).resultat(), block)

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
class MedSeksjon(private val seksjon: JsonNode) {
    fun erFerdig() = assertEquals(true, seksjon["ferdig"].asBoolean())

    fun fakta(block: MedFakta.() -> Unit) = fakta(sjekkAlle = true, sjekkRekkefølge = true, block = block)

    fun fakta(sjekkAlle: Boolean, sjekkRekkefølge: Boolean, block: MedFakta.() -> Unit) {
        val medFakta = MedFakta(seksjon["fakta"])
        block(medFakta)

        if (sjekkAlle) medFakta.erAlleFaktumSjekket()
        if (sjekkRekkefølge) medFakta.sjekkRekkfølge()
    }
}

@JsonAssertMarker
class MedFakta(private val fakta: JsonNode) {
    private val faktumSomIkkeErSjekket = fakta.map { it["beskrivendeId"].asText() }.toMutableSet()
    private val rekkefølge = faktumSomIkkeErSjekket.toMutableList()
    private val verifisertIRekkefølge = mutableListOf<String>()

    fun erAlleFaktumSjekket() =
        assertEquals(
            emptySet<String>(),
            faktumSomIkkeErSjekket,
            "Det er faktum som ikke er verifisert. Sjekk alle eller sett sjekkAlle til false"
        )

    fun sjekkRekkfølge() = assertEquals(
        rekkefølge,
        verifisertIRekkefølge,
        "\nRekkefølgen på fakta-verifisering er ikke i henhold til rekkefølgen som er definert av seksjon + avhengigheter"
    )

    fun harAntallFakta(value: Int) = antall(value, null)
    fun harAntallReadOnly(value: Int) = antall(value, true)

    private fun antall(antall: Int, readOnly: Boolean?) =
        when (readOnly) {
            true -> assertEquals(antall, fakta.count { it["readOnly"].asBoolean() })
            false -> assertEquals(antall, fakta.count { !it["readOnly"].asBoolean() })
            null -> assertEquals(antall, fakta.size())
        }

    fun harAntallBesvarte(value: Int) =
        assertEquals(value, fakta.count { it.has("svar") }, "Feil antall faktum i seksjon")

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

    fun tekst(navn: String, block: MedFaktum.() -> Unit) = faktum(navn, block).also {
        it.erType("tekst")
    }

    fun envalg(navn: String, block: MedFaktum.() -> Unit) = faktum(navn, block).also {
        it.erType("envalg")
    }

    fun flervalg(navn: String, block: MedFaktum.() -> Unit) = faktum(navn, block).also {
        it.erType("flervalg")
    }

    fun land(navn: String, block: MedLandFaktum.() -> Unit) {
        val faktum = MedLandFaktum(fakta.finnFaktum(navn))
        faktum.erType("land")
        block(faktum)
        sjekketFaktum(navn)
    }

    fun generator(navn: String, block: MedGeneratorFaktum.() -> Unit) {
        val faktum = MedGeneratorFaktum(fakta.finnFaktum(navn))
        faktum.erType("generator")
        block(faktum)
        sjekketFaktum(navn)
    }

    private fun faktum(navn: String, block: MedFaktum.() -> Unit): MedFaktum {
        val medFaktum = MedFaktum(fakta.finnFaktum(navn))
        block(medFaktum)
        sjekketFaktum(navn)
        return medFaktum
    }

    private fun sjekketFaktum(navn: String) {
        faktumSomIkkeErSjekket.remove(navn)
        verifisertIRekkefølge.add(navn)
    }

    fun alle(block: MedFaktum.() -> Unit) = fakta.forEach { block(MedFaktum(it)) }
}

@JsonAssertMarker
open class MedFaktum(val faktum: JsonNode) {
    protected val navn: String = faktum["beskrivendeId"].asText()

    fun erBesvart() = erBesvart(true)
    fun erIkkeBesvart() = erBesvart(false)
    private fun erBesvart(boolsk: Boolean = true) = assertEquals(boolsk, faktum.has("svar"), "Faktum $navn er besvart")

    fun erReadOnly(readOnly: Boolean = true) =
        assertEquals(readOnly, faktum["readOnly"].asBoolean(), "Faktum $navn var ikke readOnly")

    fun harRoller(vararg rolle: String) = harRoller(rolle.toList())

    private fun harRoller(roller: List<String>) = faktum.get("roller").toSet().map { it.asText() }.also {
        assertEquals(roller, it)
    }

    internal fun erType(type: String) = assertEquals(type, faktum["type"].asText())

    fun erBesvartMed(boolsk: Boolean) = assertEquals(boolsk, faktum["svar"].asBoolean())

    open fun erBesvartMed(antallSvar: Int) =
        assertEquals(antallSvar, faktum["svar"].asInt(), "Faktum $navn er besvart med")

    open fun erBesvartMed(tekst: String) =
        assertEquals(tekst, faktum["svar"].asText(), "Faktum $navn er besvart med")

    fun sannsynliggjøresAv(block: MedFakta.() -> Unit) = MedFakta(faktum["sannsynliggjoresAv"]).also { block(it) }

    fun harGyldigeValg(vararg valg: String) = harGyldigeValg(valg.toList())
    private fun harGyldigeValg(valg: List<String>) =
        faktum.get("gyldigeValg").toSet().map { it.asText() }
            .also { assertEquals(valg, it, "Valgfaktum ${this.navn} har ikke riktige valg") }
}

@JsonAssertMarker
class MedGeneratorFaktum(faktum: JsonNode) : MedFaktum(faktum) {
    private val svar = faktum["svar"]
    private val templates = faktum["templates"]
    fun svar(i: Int, block: MedFakta.() -> Unit) = MedFakta(svar[i - 1]).also { block(it) }

    fun templates(block: MedFakta.() -> Unit) = MedFakta(templates).also { block(it) }

    override fun erBesvartMed(antallSvar: Int) = assertEquals(antallSvar, svar.size())

    fun alle(block: MedFaktum.() -> Unit) = svar.flatten().forEach { block(MedFaktum(it)) }
}

@JsonAssertMarker
open class MedLandFaktum(faktum: JsonNode) : MedFaktum(faktum) {
    fun harLand(vararg land: String) {
        val alleLand = faktum.get("gyldigeLand").map { it.asText() }
        land.toList().forEach {
            assertContains(alleLand, it, "Landfaktum $navn mangler land $it")
        }
    }

    fun grupper(sjekkAlle: Boolean, block: MedGruppeLandFaktum.() -> Unit) {
        val gruppeland = MedGruppeLandFaktum(faktum)
        block(gruppeland)
        if (sjekkAlle) gruppeland.sjekkAlle()
    }

    @JsonAssertMarker
    inner class MedGruppeLandFaktum(
        faktum: JsonNode
    ) {
        private val sjekkaGrupper = mutableListOf<String>()
        private val grupper = faktum["grupper"].associate { fakta ->
            val navn = fakta["gruppeId"].asText()
            navn to fakta["land"].map { it.asText() }
        }

        fun gruppe(gruppeNavn: String, block: MedGruppeFaktum.() -> Unit) {
            assertTrue(
                grupper.containsKey(gruppeNavn),
                "Landfaktum ${this@MedLandFaktum.navn} mangler gruppe $gruppeNavn"
            )
            block(MedGruppeFaktum(grupper[gruppeNavn]!!))
            sjekkaGrupper.add(gruppeNavn)
        }

        internal fun sjekkAlle() = assertEquals(
            grupper.keys.toList(),
            sjekkaGrupper,
            "Landfaktum ${this@MedLandFaktum.navn} mangler sjekk for grupper"
        )

        inner class MedGruppeFaktum(
            private val gruppeland: List<String>
        ) {
            fun harLand(vararg land: String) {
                land.toList().forEach {
                    assertContains(gruppeland, it, "Landfaktum $land mangler land $it")
                }
            }
        }
    }
}

private fun JsonNode.finnFaktum(navn: String): JsonNode {
    val jsonNode = this.find { it["beskrivendeId"].asText() == navn }
    return checkNotNull(jsonNode) { "Fant ikke faktum med navn '$navn' i JSON \n ${this.toPrettyString()}" }
}

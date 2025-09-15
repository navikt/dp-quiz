package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.helpers.TestProsesser
import no.nav.dagpenger.model.helpers.desember
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.mellom
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.visitor.ProsessVisitor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GeneratorFaktumTest {
    private lateinit var søknadprosess: Prosess

    @BeforeEach
    fun setup() {
        val faktaPrototype =
            Fakta(
                testversjon,
                heltall faktum "periode antall" id 1 genererer 2 og 3,
                dato faktum "fom" id 2,
                dato faktum "tom" id 3,
                dato faktum "ønsket dato" id 4,
            )
        val prototypeSubsumsjon =
            faktaPrototype generator 1 har
                "periode".deltre {
                    faktaPrototype.dato(4) mellom faktaPrototype.dato(2) og faktaPrototype.dato(3)
                }
        søknadprosess =
            Prosess(
                TestProsesser.Test,
                faktaPrototype,
                Seksjon(
                    "periode antall",
                    Rolle.nav,
                    faktaPrototype generator 1,
                ),
                Seksjon(
                    "periode",
                    Rolle.nav,
                    faktaPrototype dato 2,
                    faktaPrototype dato 3,
                ),
                Seksjon(
                    "søknadsdato",
                    Rolle.søker,
                    faktaPrototype dato 4,
                ),
                rootSubsumsjon = prototypeSubsumsjon,
            )
    }

    @Test
    fun ` periode faktum `() {
        søknadprosess.generator(1).besvar(2)
        søknadprosess.dato(4).besvar(5.januar)
        søknadprosess.dato("2.1").besvar(1.januar)
        søknadprosess.dato("3.1").besvar(8.januar)
        søknadprosess.dato("2.2").besvar(1.februar)
        søknadprosess.dato("3.2").besvar(8.februar)

        assertEquals(5, søknadprosess.size)
        assertTrue(søknadprosess.rootSubsumsjon.resultat()!!)
        søknadprosess.dato(4).besvar(12.januar)
        assertFalse(søknadprosess.rootSubsumsjon.resultat()!!)

        søknadprosess.dato(4).besvar(8.januar)
        assertTrue(søknadprosess.rootSubsumsjon.resultat()!!)
    }

    @Test
    fun ` har med tom generator blir false `() {
        søknadprosess.generator(1).besvar(0)
        assertEquals(false, søknadprosess.rootSubsumsjon.resultat())
    }

    @Test
    fun ` har med en oppfylt generert subsumsjon blir true `() {
        søknadprosess.generator(1).besvar(1)
        søknadprosess.dato("2.1").besvar(1.januar)
        søknadprosess.dato("3.1").besvar(8.januar)
        søknadprosess.dato(4).besvar(5.januar)
        assertEquals(true, søknadprosess.rootSubsumsjon.resultat())
    }

    @Test
    fun ` har med en ikke oppfylt generert subsumsjon blir false `() {
        søknadprosess.generator(1).besvar(1)
        søknadprosess.dato("2.1").besvar(1.desember)
        søknadprosess.dato("3.1").besvar(8.desember)
        søknadprosess.dato(4).besvar(5.januar)
        assertEquals(false, søknadprosess.rootSubsumsjon.resultat())
    }

    @Test
    fun ` har med oppfylt og ikke oppfylt genererte subsumsjoner blir true `() {
        søknadprosess.generator(1).besvar(2)
        søknadprosess.dato("2.1").besvar(1.desember)
        søknadprosess.dato("3.1").besvar(8.desember)
        søknadprosess.dato("2.2").besvar(1.januar)
        søknadprosess.dato("3.2").besvar(8.januar)
        søknadprosess.dato(4).besvar(5.januar)
        assertEquals(true, søknadprosess.rootSubsumsjon.resultat())
    }

    @Test
    fun `vi kan gi navn av type tekst til en generator`() {
        val faktaPrototype =
            Fakta(
                testversjon,
                tekst faktum "arbeidsgivernavn" id 1,
                heltall faktum "arbeidsgiver med navn" id 2 navngittAv 1 genererer 1,
            )
        val søknadprosess =
            søknadprosess(
                faktaPrototype,
                faktaPrototype generator 2 har
                    "arbeidsgiver".deltre {
                        (faktaPrototype tekst 1).utfylt()
                    },
                Seksjon(
                    "arbeidsgiver",
                    Rolle.søker,
                    faktaPrototype heltall 2,
                    faktaPrototype tekst 1,
                ),
            )
        søknadprosess.generator(2).besvar(2)
        søknadprosess.tekst("1.1").besvar(Tekst("Arbeidsgiver 1"))
        søknadprosess.tekst("1.2").besvar(Tekst("Arbeidsgiver 2"))
        val generatorer = GeneratorVisitor(søknadprosess)

        assertEquals(
            Tekst("Arbeidsgiver 1"),
            generatorer.first().identitet(søknadprosess.tekst("1.1").faktumId)!!.svar(),
        )
        assertEquals(
            Tekst("Arbeidsgiver 2"),
            generatorer.first().identitet(søknadprosess.tekst("1.2").faktumId)!!.svar(),
        )
    }

    private fun søknadprosess(
        faktaPrototype: Fakta,
        prototypeSubsumsjon: Subsumsjon,
        vararg seksjoner: Seksjon,
    ): Prosess =
        Prosess(
            TestProsesser.Test,
            faktaPrototype,
            *seksjoner,
            rootSubsumsjon = prototypeSubsumsjon,
        )

    private class GeneratorVisitor(
        prosess: Prosess,
        private val generatorer: MutableSet<GeneratorFaktum> = mutableSetOf(),
    ) : ProsessVisitor,
        MutableSet<GeneratorFaktum> by generatorer {
        init {
            prosess.accept(this)
        }

        override fun <R : Comparable<R>> visitMedSvar(
            faktum: GeneratorFaktum,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFakta: Set<Faktum<*>>,
            templates: List<TemplateFaktum<*>>,
            roller: Set<Rolle>,
            clazz: Class<R>,
            svar: R,
            genererteFaktum: Set<Faktum<*>>,
        ) {
            generatorer.add(faktum)
        }
    }
}

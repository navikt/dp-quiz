package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.helpers.desember
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.mellom
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.seksjon.Versjon.UserInterfaceType.Web
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GeneratorFaktumTest {
    private lateinit var søknadprosessTestBygger: Versjon.Bygger

    @BeforeEach
    fun setup() {
        val søknadPrototype = Søknad(
            testversjon,
            heltall faktum "periode antall" id 1 genererer 2 og 3,
            dato faktum "fom" id 2,
            dato faktum "tom" id 3,
            dato faktum "ønsket dato" id 4
        )
        val prototypeSubsumsjon = søknadPrototype generator 1 har "periode".deltre {
            søknadPrototype.dato(4) mellom søknadPrototype.dato(2) og søknadPrototype.dato(3)
        }

        val prototypeSøknadprosess = Søknadprosess(
            Seksjon(
                "periode antall",
                Rolle.nav,
                søknadPrototype generator 1
            ),
            Seksjon(
                "periode",
                Rolle.nav,
                søknadPrototype dato 2,
                søknadPrototype dato 3,
            ),
            Seksjon(
                "søknadsdato",
                Rolle.søker,
                søknadPrototype dato 4,
            ),
        )
        søknadprosessTestBygger = Versjon.Bygger(søknadPrototype, prototypeSubsumsjon, mapOf(Web to prototypeSøknadprosess))
    }

    @Test
    fun ` periode faktum `() {
        val søknadprosess = søknadprosessTestBygger.søknadprosess(testPerson, Web)
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
        val søknadprosess = søknadprosessTestBygger.søknadprosess(testPerson, Web)
        søknadprosess.generator(1).besvar(0)
        assertEquals(false, søknadprosess.rootSubsumsjon.resultat())
    }

    @Test
    fun ` har med en oppfylt generert subsumsjon blir true `() {
        val søknadprosess = søknadprosessTestBygger.søknadprosess(testPerson, Web)
        søknadprosess.generator(1).besvar(1)
        søknadprosess.dato("2.1").besvar(1.januar)
        søknadprosess.dato("3.1").besvar(8.januar)
        søknadprosess.dato(4).besvar(5.januar)
        assertEquals(true, søknadprosess.rootSubsumsjon.resultat())
    }

    @Test
    fun ` har med en ikke oppfylt generert subsumsjon blir false `() {
        val søknadprosess = søknadprosessTestBygger.søknadprosess(testPerson, Web)
        søknadprosess.generator(1).besvar(1)
        søknadprosess.dato("2.1").besvar(1.desember)
        søknadprosess.dato("3.1").besvar(8.desember)
        søknadprosess.dato(4).besvar(5.januar)
        assertEquals(false, søknadprosess.rootSubsumsjon.resultat())
    }

    @Test
    fun ` har med oppfylt og ikke oppfylt genererte subsumsjoner blir true `() {
        val søknadprosess = søknadprosessTestBygger.søknadprosess(testPerson, Web)
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
        val søknadPrototype = Søknad(
            testversjon,
            tekst faktum "arbeidsgivernavn" id 1,
            heltall faktum "arbeidsgiver med navn" id 2 navngittAv 1 genererer 1
        )
        val søknadprosess = søknadprosess(
            søknadPrototype,
            søknadPrototype generator 2 har "arbeidsgiver".deltre {
                (søknadPrototype tekst 1).utfylt()
            },
            Seksjon(
                "arbeidsgiver",
                Rolle.søker,
                søknadPrototype heltall 2,
                søknadPrototype tekst 1
            )
        )
        søknadprosess.generator(2).besvar(2)
        søknadprosess.tekst("1.1").besvar(Tekst("Arbeidsgiver 1"))
        søknadprosess.tekst("1.2").besvar(Tekst("Arbeidsgiver 2"))
        val generatorer = GeneratorVisitor(søknadprosess)

        assertEquals(
            Tekst("Arbeidsgiver 1"),
            generatorer.first().identitet(søknadprosess.tekst("1.1").faktumId)!!.svar()
        )
        assertEquals(
            Tekst("Arbeidsgiver 2"),
            generatorer.first().identitet(søknadprosess.tekst("1.2").faktumId)!!.svar()
        )
    }

    private fun søknadprosess(
        søknadPrototype: Søknad,
        prototypeSubsumsjon: Subsumsjon,
        vararg seksjoner: Seksjon
    ): Søknadprosess {
        val prototypeSøknadprosess = Søknadprosess(
            *seksjoner
        )
        val søknadprosessTestBygger =
            Versjon.Bygger(søknadPrototype, prototypeSubsumsjon, mapOf(Web to prototypeSøknadprosess))
        return søknadprosessTestBygger.søknadprosess(testPerson, Web)
    }

    private class GeneratorVisitor(
        søknadprosess: Søknadprosess,
        private val generatorer: MutableSet<GeneratorFaktum> = mutableSetOf()
    ) :
        SøknadprosessVisitor,
        MutableSet<GeneratorFaktum> by generatorer {

        init {
            søknadprosess.accept(this)
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
            genererteFaktum: Set<Faktum<*>>
        ) {
            generatorer.add(faktum)
        }
    }
}

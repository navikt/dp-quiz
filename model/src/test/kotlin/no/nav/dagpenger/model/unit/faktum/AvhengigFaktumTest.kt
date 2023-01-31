package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.seksjon.Faktagrupper
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import no.nav.dagpenger.model.visitor.FaktumVisitor
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

internal class AvhengigFaktumTest {
    @Test
    fun `Resetter avhengige faktum`() {
        val fakta = Fakta(
            testversjon,
            boolsk faktum "f1" id 1,
            boolsk faktum "f2" id 2 avhengerAv 1,
            boolsk faktum "f3" id 3 avhengerAv 2
        )
        val ja1 = fakta.boolsk(1)
        val ja2 = fakta.boolsk(2)
        val ja3 = fakta.boolsk(3)

        ja1.besvar(true)
        ja2.besvar(true)
        ja3.besvar(true)
        assertTrue { fakta.all { it.erBesvart() } }

        ja1.besvar(false)
        assertFalse { ja2.erBesvart() }
        assertFalse { ja3.erBesvart() }
    }

    @Test
    fun `Templatefaktum innenfor generatorfaktum kan være avhengig av et annet templatefaktum`() {
        val fakta = Fakta(
            testversjon,
            heltall faktum "periode antall" id 1 genererer 2 og 3,
            dato faktum "fom" id 2,
            dato faktum "tom" id 3 avhengerAv 2,
            dato faktum "ønsket dato" id 4
        )
        val prototypeFaktagrupper = Faktagrupper(
            Seksjon("periode antall", Rolle.nav, fakta generator 1),
            Seksjon("periode", Rolle.nav, fakta dato 2, fakta dato 3),
            Seksjon("søknadsdato", Rolle.søker, fakta dato 4)
        )
        val søknadprosess =
            Versjon.Bygger(
                fakta,
                TomSubsumsjon,
                prototypeFaktagrupper
            )
                .søknadprosess(testPerson)

        søknadprosess.generator(1).besvar(1)
        søknadprosess.dato("2.1").besvar(LocalDate.now())
        søknadprosess.dato("3.1").besvar(LocalDate.now())

        assertEquals(LocalDate.now(), søknadprosess.dato("3.1").svar())

        søknadprosess.dato("2.1").besvar(LocalDate.now().plusDays(1))

        assertFalse(søknadprosess.dato("3.1").erBesvart())
        AvhengigheterVisitor(søknadprosess.dato("2.1")).also {
            assertEquals(1, it.avhengigheter.size)
        }

        søknadprosess.generator(1).besvar(1)

        AvhengigheterVisitor(søknadprosess.dato("2.1")).also {
            println(it.avhengigheter)
            assertEquals(1, it.avhengigheter.size)
        }

        søknadprosess.generator(1).besvar(2)

        søknadprosess.dato("2.1").besvar(LocalDate.now())
        søknadprosess.dato("2.2").besvar(LocalDate.now())

        søknadprosess.dato("3.1").besvar(LocalDate.now())
        søknadprosess.dato("3.2").besvar(LocalDate.now())

        søknadprosess.dato("2.2").besvar(LocalDate.now().plusDays(1))
        assertFalse(søknadprosess.dato("3.2").erBesvart())

        AvhengigheterVisitor(søknadprosess.dato("2.2")).also {
            assertEquals(1, it.avhengigheter.size)
        }

        AvhengigheterVisitor(søknadprosess.dato("2.1")).also {
            assertEquals(1, it.avhengigheter.size)
        }
    }

    @Test @Disabled
    fun `faktum som er avhengigAv et templatefaktum`() {
        val fakta = Fakta(
            testversjon,
            heltall faktum "periode antall" id 1 genererer 2 og 3,
            dato faktum "fom" id 2,
            dato faktum "tom" id 3,
            boolsk faktum "boolsk" id 4 avhengerAv 3
        )
        val søknadprosess = fakta.testSøknadprosess()

        søknadprosess.generator(1).besvar(1)
        søknadprosess.dato("2.1").besvar(LocalDate.now())
        søknadprosess.dato("3.1").besvar(LocalDate.now())

        søknadprosess.boolsk(4).besvar(true)

        assertTrue(søknadprosess.boolsk(4).erBesvart())
        søknadprosess.dato("3.1").besvar(LocalDate.now().plusDays(2))

        assertFalse(søknadprosess.boolsk(4).erBesvart())
    }
}

class AvhengigheterVisitor(faktum: Faktum<*>) : FaktumVisitor {
    lateinit var avhengigheter: Set<Faktum<*>>

    init {
        faktum.accept(this)
    }

    override fun <R : Comparable<R>> postVisitAvhengigeFakta(faktum: Faktum<R>, avhengigeFakta: MutableSet<Faktum<*>>) {
        avhengigheter = avhengigeFakta
    }
}

package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

internal class AvhengigFaktumTest {

    @Test
    fun `Resetter avhengige faktum`() {
        val søknad = Søknad(
            testversjon,
            boolsk faktum "f1" id 1,
            boolsk faktum "f2" id 2 avhengerAv 1,
            boolsk faktum "f3" id 3 avhengerAv 2
        )
        val ja1 = søknad.boolsk(1)
        val ja2 = søknad.boolsk(2)
        val ja3 = søknad.boolsk(3)

        ja1.besvar(true)
        ja2.besvar(true)
        ja3.besvar(true)
        assertTrue { søknad.all { it.erBesvart() } }

        ja1.besvar(false)
        assertFalse { ja2.erBesvart() }
        assertFalse { ja3.erBesvart() }
    }

    @Test
    fun `Templatefaktum innenfor generatorfaktum kan være avhengig av et annet templatefaktum`() {
        val søknad = Søknad(
            testversjon,
            heltall faktum "periode antall" id 1 genererer 2 og 3,
            dato faktum "fom" id 2,
            dato faktum "tom" id 3 avhengerAv 2,
            dato faktum "ønsket dato" id 4
        )

        val prototypeSøknadprosess = Søknadprosess(
            Seksjon("periode antall", Rolle.nav, søknad generator 1),
            Seksjon("periode", Rolle.nav, søknad dato 2, søknad dato 3),
            Seksjon("søknadsdato", Rolle.søker, søknad dato 4),
        )

        val søknadprosess =
            Versjon.Bygger(søknad, TomSubsumsjon, mapOf(Versjon.UserInterfaceType.Web to prototypeSøknadprosess))
                .søknadprosess(testPerson, Versjon.UserInterfaceType.Web)

        søknadprosess.generator(1).besvar(1)
        søknadprosess.dato("2.1").besvar(LocalDate.now())
        søknadprosess.dato("3.1").besvar(LocalDate.now())

        assertEquals(LocalDate.now(), søknadprosess.dato("3.1").svar())

        søknadprosess.dato("2.1").besvar(LocalDate.now().plusDays(1))

        kotlin.test.assertFalse(søknadprosess.dato("3.1").erBesvart())
    }
}

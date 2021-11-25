package no.nav.dagpenger.model.unit.seksjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.Testprosess
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.seksjon.Versjon.UserInterfaceType.Web
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class NesteSeksjonTest {
    @Test
    fun ` neste seksjon bug - rolle ble kopiert fra avehengig faktum til seksjon `() {
        val prototypesøknad = Søknad(
            testversjon,
            boolsk faktum "f1" id 1,
            boolsk faktum "f2" id 2 avhengerAv 1
        )
        val prototypeSubsumsjon = prototypesøknad.boolsk(1) er true hvisOppfylt {
            prototypesøknad.boolsk(2) er true
        }
        val prototypeSøknadprosess = Søknadprosess(
            prototypesøknad,
            Seksjon("nav", Rolle.nav, prototypesøknad.boolsk(2)),
            Seksjon("søker", Rolle.søker, prototypesøknad.boolsk(1)),
            rootSubsumsjon = prototypeSubsumsjon
        )
        val fakta = Versjon.Bygger(prototypesøknad, prototypeSubsumsjon, mapOf(Web to prototypeSøknadprosess))
            .søknadprosess(testPerson, Web)

        assertEquals(listOf(fakta[1]), fakta.nesteSeksjoner())
    }

    @Test
    fun ` bug-fiks read-only fakta i seksjonen`() {
        val prototypesøknad = Søknad(
            Prosessversjon(Testprosess.Test, 401),
            boolsk faktum "f1" id 1,
            boolsk faktum "f2" id 2 avhengerAv 1
        )
        val prototypeSubsumsjon = prototypesøknad.boolsk(1) er true hvisOppfylt {
            prototypesøknad.boolsk(2) er true
        }
        val prototypeSøknadprosess = Søknadprosess(
            prototypesøknad,
            Seksjon("søker1", Rolle.søker, prototypesøknad.boolsk(2)),
            Seksjon("søker2", Rolle.søker, prototypesøknad.boolsk(1)),
            rootSubsumsjon = prototypeSubsumsjon
        )

        Versjon.Bygger(prototypesøknad, prototypeSubsumsjon, mapOf(Web to prototypeSøknadprosess))
            .søknadprosess(testPerson, Web).also { fakta ->
                assertEquals(listOf(fakta[1]), fakta.nesteSeksjoner())
            }
    }
}

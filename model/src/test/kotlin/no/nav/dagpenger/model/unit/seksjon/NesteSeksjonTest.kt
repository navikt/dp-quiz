package no.nav.dagpenger.model.unit.seksjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class NesteSeksjonTest {
    @Test
    fun ` neste seksjon bug - rolle ble kopiert fra avehengig faktum til seksjon `() {
        val prototypesøknad = Fakta(
            testversjon,
            boolsk faktum "f1" id 1,
            boolsk faktum "f2" id 2 avhengerAv 1
        )
        val prototypeSubsumsjon = prototypesøknad.boolsk(1) er true hvisOppfylt {
            prototypesøknad.boolsk(2) er true
        }
        val prototypeUtredningsprosess = Utredningsprosess(
            prototypesøknad,
            Seksjon("nav", Rolle.nav, prototypesøknad.boolsk(2)),
            Seksjon("søker", Rolle.søker, prototypesøknad.boolsk(1)),
            rootSubsumsjon = prototypeSubsumsjon
        )
        val fakta = Versjon.Bygger(
            prototypesøknad,
            prototypeSubsumsjon,
            prototypeUtredningsprosess
        )
            .søknadprosess(testPerson)

        assertEquals(listOf(fakta[1]), fakta.nesteSeksjoner())
    }

    @Test
    fun ` bug-fiks read-only fakta i seksjonen`() {
        val prototypesøknad = Fakta(
            testversjon,
            boolsk faktum "f1" id 1,
            boolsk faktum "f2" id 2 avhengerAv 1
        )
        val prototypeSubsumsjon = prototypesøknad.boolsk(1) er true hvisOppfylt {
            prototypesøknad.boolsk(2) er true
        }
        val prototypeUtredningsprosess = Utredningsprosess(
            prototypesøknad,
            Seksjon("søker1", Rolle.søker, prototypesøknad.boolsk(2)),
            Seksjon("søker2", Rolle.søker, prototypesøknad.boolsk(1)),
            rootSubsumsjon = prototypeSubsumsjon
        )

        Versjon.Bygger(
            prototypesøknad,
            prototypeSubsumsjon,
            prototypeUtredningsprosess
        )
            .søknadprosess(testPerson).also { fakta ->
                assertEquals(listOf(fakta[1]), fakta.nesteSeksjoner())
            }
    }
}

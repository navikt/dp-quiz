package no.nav.dagpenger.model.unit.seksjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.helpers.TestProsesser
import no.nav.dagpenger.model.helpers.testProsess
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class NesteSeksjonTest {
    @Test
    fun ` neste seksjon bug - rolle ble kopiert fra avehengig faktum til seksjon `() {
        val prototypeFakta =
            Fakta(
                testversjon,
                boolsk faktum "f1" id 1,
                boolsk faktum "f2" id 2 avhengerAv 1,
            )
        val prototypeSubsumsjon =
            prototypeFakta.boolsk(1) er true hvisOppfylt {
                prototypeFakta.boolsk(2) er true
            }
        val prototypeProsess =
            Prosess(
                TestProsesser.Test,
                prototypeFakta,
                Seksjon("nav", Rolle.nav, prototypeFakta.boolsk(2)),
                Seksjon("søker", Rolle.søker, prototypeFakta.boolsk(1)),
                rootSubsumsjon = prototypeSubsumsjon,
            )
        prototypeProsess.testProsess().also {
            assertEquals(listOf(it[1]), it.nesteSeksjoner())
        }
    }

    @Test
    fun ` bug-fiks read-only fakta i seksjonen`() {
        val prototypeFakta =
            Fakta(
                testversjon,
                boolsk faktum "f1" id 1,
                boolsk faktum "f2" id 2 avhengerAv 1,
            )
        val prototypeSubsumsjon =
            prototypeFakta.boolsk(1) er true hvisOppfylt {
                prototypeFakta.boolsk(2) er true
            }
        val prototypeProsess =
            Prosess(
                TestProsesser.Test,
                prototypeFakta,
                Seksjon("søker1", Rolle.søker, prototypeFakta.boolsk(2)),
                Seksjon("søker2", Rolle.søker, prototypeFakta.boolsk(1)),
                rootSubsumsjon = prototypeSubsumsjon,
            )

        prototypeProsess.testProsess().also {
            assertEquals(listOf(it[1]), it.nesteSeksjoner())
        }
    }
}

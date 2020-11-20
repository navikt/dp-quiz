package no.nav.dagpenger.model.unit.seksjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.seksjon.Versjon.UserInterfaceType.Web
import no.nav.dagpenger.model.subsumsjon.så
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class NesteSeksjonTest {

    @Test
    fun ` neste seksjon bug - rolle ble kopiert fra avehengig faktum til seksjon `() {
        val prototypesøknad = Søknad(
            400,
            ja nei "f1" id 1,
            ja nei "f2" id 2 avhengerAv 1

        )

        val prototypeSubsumsjon = prototypesøknad.ja(1) er true så
            (prototypesøknad.ja(2) er true)

        val prototypeFaktagruppe = Søknadprosess(
            prototypesøknad,
            Seksjon("nav", Rolle.nav, prototypesøknad.ja(2)),
            Seksjon("søker", Rolle.søker, prototypesøknad.ja(1)),
            rootSubsumsjon = prototypeSubsumsjon
        )

        Versjon(prototypesøknad, prototypeSubsumsjon, mapOf(Web to prototypeFaktagruppe))
        val fakta = Versjon.id(prototypesøknad.versjonId).søknadprosess("12345678912", Web)

        assertEquals(listOf(fakta[1]), fakta.nesteSeksjoner())
    }

    @Test
    fun ` bug-fiks read-only fakta i seksjonen`() {
        val prototypesøknad = Søknad(
            401,
            ja nei "f1" id 1,
            ja nei "f2" id 2 avhengerAv 1

        )

        val prototypeSubsumsjon = prototypesøknad.ja(1) er true så
            (prototypesøknad.ja(2) er true)

        val prototypeFaktagruppe = Søknadprosess(
            prototypesøknad,
            Seksjon("søker1", Rolle.søker, prototypesøknad.ja(2)),
            Seksjon("søker2", Rolle.søker, prototypesøknad.ja(1)),
            rootSubsumsjon = prototypeSubsumsjon
        )

        val versjon = Versjon(prototypesøknad, prototypeSubsumsjon, mapOf(Web to prototypeFaktagruppe))

        val fakta = Versjon.id(prototypesøknad.versjonId).søknadprosess("12345678912", Web)

        assertEquals(listOf(fakta[1]), fakta.nesteSeksjoner())
    }
}

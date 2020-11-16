package no.nav.dagpenger.model.unit.seksjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.model.faktagrupper.Versjon.FaktagrupperType.Web
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.subsumsjon.så
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class NesteSeksjonTest {

    @Test
    fun ` neste seksjon bug - rolle ble kopiert fra avehengig faktum til seksjon `() {
        val prototypesøknad = Søknad(
            ja nei "f1" id 1,
            ja nei "f2" id 2 avhengerAv 1

        )

        val prototypeSubsumsjon = prototypesøknad.ja(1) er true så
            (prototypesøknad.ja(2) er true)

        val prototypeFaktagruppe = Faktagrupper(
            prototypesøknad,
            Seksjon("nav", Rolle.nav, prototypesøknad.ja(2)),
            Seksjon("søker", Rolle.søker, prototypesøknad.ja(1)),
            rootSubsumsjon = prototypeSubsumsjon
        )

        val versjon = Versjon(prototypesøknad, prototypeSubsumsjon, mapOf(Web to prototypeFaktagruppe))

        val fakta = Versjon.siste.faktagrupper("12345678912", Web)

        assertEquals(2, fakta[0].size)
        assertEquals(listOf(fakta[1]), fakta.nesteSeksjoner())
    }
}
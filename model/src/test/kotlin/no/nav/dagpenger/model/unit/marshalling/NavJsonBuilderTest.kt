package no.nav.dagpenger.model.unit.marshalling

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.NavJsonBuilder
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.så
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NavJsonBuilderTest {
    @Test
    fun `bygger behov event`() {
        val prototypeSøknad = Søknad(
            ja nei "f1" id 1,
            ja nei "f1" id 2 avhengerAv 1,
            ja nei "f3" id 3,
            ja nei "f3" id 4 avhengerAv 3,
        )

        val prototypeSubsumsjon =
            prototypeSøknad.ja(1) er true så
                "alle".alle(
                    prototypeSøknad.ja(2) er true,
                    prototypeSøknad.ja(3) er true,
                    prototypeSøknad.ja(4) er true
                )

        val søkerSeksjon = Seksjon("seksjon søker", Rolle.søker, prototypeSøknad.ja(1))
        val navSeksjon = Seksjon("seksjon nav", Rolle.nav, prototypeSøknad.ja(2), prototypeSøknad.ja(3), prototypeSøknad.ja(4))

        val prototypeFaktagrupper = Faktagrupper(
            prototypeSøknad,
            søkerSeksjon,
            navSeksjon,
            rootSubsumsjon = prototypeSubsumsjon
        )

        Versjon(
            1,
            prototypeSøknad,
            prototypeSubsumsjon,
            mapOf(Versjon.FaktagrupperType.Web to prototypeFaktagrupper)
        )

        val fakta = Versjon.siste.faktagrupper(fnr = "12345678910", Versjon.FaktagrupperType.Web)
        fakta.ja(1).besvar(true)
        val json = NavJsonBuilder(fakta).resultat()

        assertEquals("behov", json["@event_name"].asText())
        assertEquals("12345678910", json["fnr"].asText())
        assertEquals(2, json["fakta"].size())
    }
}

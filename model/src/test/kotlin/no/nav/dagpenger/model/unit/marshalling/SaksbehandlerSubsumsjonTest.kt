package no.nav.dagpenger.model.unit.marshalling

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.model.faktagrupper.Versjon.FaktagrupperType.Web
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.SaksbehandlerJsonBuilder
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.gyldigGodkjentAv
import no.nav.dagpenger.model.regel.ugyldigGodkjentAv
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.så
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.UUID

internal class SaksbehandlerSubsumsjonTest {
    companion object {
        private val prototypeSøknad = Søknad(
            ja nei "f1" id 1,
            ja nei "f2" id 2 avhengerAv 1,
            ja nei "f3" id 3,
            ja nei "f4" id 4 avhengerAv 3,
        )
    }

    @Test
    fun `byggger oppgave event`() {

        val fakta = faktagrupper(
            prototypeSøknad.ja(1) er true gyldigGodkjentAv prototypeSøknad.ja(2) så
                (prototypeSøknad.ja(3) er true ugyldigGodkjentAv prototypeSøknad.ja(4))
        )

        val json = SaksbehandlerJsonBuilder(fakta, "saksbehandler2").resultat()

        assertEquals("oppgave", json["@event_name"].asText())
        assertDoesNotThrow { UUID.fromString(json["soknad_uuid"].asText()) }
        assertEquals("saksbehandler2", json["seksjon_navn"].asText())
        assertEquals(2, json["fakta"].size())
        assertEquals("1", json["fakta"][0]["id"].asText())
        assertEquals(
            setOf(Rolle.saksbehandler, Rolle.søker),
            json["fakta"][0]["roller"].map { Rolle.valueOf(it.asText()) }.toSet()
        )
        assertEquals("2", json["fakta"][1]["id"].asText())
        assertEquals(
            setOf(Rolle.saksbehandler),
            json["fakta"][1]["roller"].map { Rolle.valueOf(it.asText()) }.toSet()
        )
    }

    @Test
    fun `enkel subsumsjon`() {
        val fakta = faktagrupper(prototypeSøknad.ja(1) er true)
        fakta.ja(1).besvar(true)
        SaksbehandlerJsonBuilder(fakta, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertTrue(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
        }
    }

    @Test
    fun `subsumsjon med gyldig sti`() {
        val fakta = faktagrupper(
            prototypeSøknad.ja(1) er true så (
                prototypeSøknad.ja(3) er true
                )
        )
        fakta.ja(1).besvar(true)
        fakta.ja(3).besvar(false)
        SaksbehandlerJsonBuilder(fakta, "saksbehandler2").resultat().also { json ->
            assertEquals(2, json["subsumsjoner"].size())
            assertTrue(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
            assertFalse(json["subsumsjoner"][1]["lokalt_resultat"].asBoolean())
        }

        fakta.ja(1).besvar(false)
        SaksbehandlerJsonBuilder(fakta, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertFalse(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
        }
    }

    private fun faktagrupper(prototypeSubsumsjon: Subsumsjon): Faktagrupper {
        val prototypeFaktagrupper = Faktagrupper(
            prototypeSøknad,
            Seksjon("søker", Rolle.søker, prototypeSøknad.ja(1), prototypeSøknad.ja(3)),
            Seksjon("saksbehandler2", Rolle.saksbehandler, prototypeSøknad.ja(2)),
            Seksjon("saksbehandler4", Rolle.saksbehandler, prototypeSøknad.ja(4)),
            rootSubsumsjon = prototypeSubsumsjon
        )

        Versjon(
            prototypeSøknad,
            prototypeSubsumsjon,
            mapOf(Web to prototypeFaktagrupper)
        )

        return Versjon.siste.faktagrupper("12345678910", Web)
    }
}

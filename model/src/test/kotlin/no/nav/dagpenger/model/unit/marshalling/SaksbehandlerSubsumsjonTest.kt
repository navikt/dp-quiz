package no.nav.dagpenger.model.unit.marshalling

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.SaksbehandlerJsonBuilder
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.gyldigGodkjentAv
import no.nav.dagpenger.model.regel.ugyldigGodkjentAv
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.seksjon.Versjon.UserInterfaceType.Web
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
    fun `bygger oppgave event`() {

        val søknadprosess = søknadprosess(
            prototypeSøknad.ja(1) er true gyldigGodkjentAv prototypeSøknad.ja(2) så
                (prototypeSøknad.ja(3) er true ugyldigGodkjentAv prototypeSøknad.ja(4))
        )

        val json = SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat()

        assertEquals("oppgave", json["@event_name"].asText())
        assertDoesNotThrow { UUID.fromString(json["soknad_uuid"].asText()) }
        assertEquals("saksbehandler2", json["seksjon_navn"].asText())
        assertEquals(2, json["fakta"].size())
        assertEquals("2", json["fakta"][0]["id"].asText())
        assertEquals("1", json["fakta"][1]["id"].asText())
        assertEquals(
            setOf(Rolle.saksbehandler.typeNavn),
            json["fakta"][0]["roller"].map { it.asText() }.toSet()
        )
        assertEquals(
            setOf(Rolle.søker.typeNavn),
            json["fakta"][1]["roller"].map { it.asText() }.toSet()
        )
    }

    @Test
    fun `enkel subsumsjon`() {
        val søknadprosess = søknadprosess(prototypeSøknad.ja(1) er true)
        søknadprosess.ja(1).besvar(true)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertTrue(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
        }
    }

    @Test
    fun `subsumsjon med gyldig sti`() {
        val søknadprosess = søknadprosess(
            prototypeSøknad.ja(1) er true så (
                prototypeSøknad.ja(3) er true
                )
        )
        søknadprosess.ja(1).besvar(true)
        søknadprosess.ja(3).besvar(false)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(2, json["subsumsjoner"].size())
            assertTrue(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
            assertFalse(json["subsumsjoner"][1]["lokalt_resultat"].asBoolean())
        }

        søknadprosess.ja(1).besvar(false)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertFalse(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
        }
    }

    private fun søknadprosess(prototypeSubsumsjon: Subsumsjon): Søknadprosess {
        val prototypeFaktagrupper = Søknadprosess(
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

        return Versjon.siste.søknadprosess("12345678910", Web)
    }
}

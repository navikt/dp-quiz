package no.nav.dagpenger.model.unit.marshalling

import io.mockk.clearStaticMockk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.helpers.NyttEksempel
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.marshalling.SaksbehandlerJsonBuilder
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.etter
import no.nav.dagpenger.model.regel.med
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.godkjentAv
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfylt
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.ikkeOppfyltGodkjentAv
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.oppfyltGodkjentAv
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.ResourceBundle
import java.util.UUID

internal class SaksbehandlerJsonBuilderTest {
    private lateinit var prototypeFakta: Fakta

    @BeforeEach
    fun setup() {
        prototypeFakta = Fakta(
            testversjon,
            boolsk faktum "f1" id 1,
            boolsk faktum "f2" id 2 avhengerAv 1,
            boolsk faktum "f3" id 3,
            boolsk faktum "f4" id 4 avhengerAv 3,
            boolsk faktum "f5" id 5,
            boolsk faktum "f6" id 6,
            boolsk faktum "f7" id 7,
            heltall faktum "f67" id 67 genererer 6 og 7,
            dato faktum "f8" id 8,
            dato faktum "f9" id 9,
            maks dato "f10" av 8 og 9 id 10,
            boolsk faktum "f11" id 11 avhengerAv 10,
            boolsk faktum "f12" id 12 avhengerAv 67,
            heltall faktum "f1314" id 1314 genererer 13 og 14,
            boolsk faktum "f13" id 13,
            boolsk faktum "f14" id 14,
        )
    }

    @Test
    fun `inkluderer genererte faktum som faktumet avhengerAv`() {
        val søknadsprosess = søknadprosess(
            (prototypeFakta.heltall(67) er 1).godkjentAv(prototypeFakta.boolsk(12))
        )
        søknadsprosess.heltall(67).besvar(1)
        søknadsprosess.boolsk("6.1").besvar(true)
        søknadsprosess.boolsk("7.1").besvar(true)

        SaksbehandlerJsonBuilder(søknadsprosess, "saksbehandler67").resultat().also {
            assertEquals(listOf("12", "6.1", "7.1"), it["fakta"].map { it["id"].asText() })
            assertEquals(3, it["fakta"].size())
        }

        søknadsprosess.heltall(1314).besvar(1)
        søknadsprosess.boolsk("13.1").besvar(true)
        søknadsprosess.boolsk("14.1").besvar(true)

        SaksbehandlerJsonBuilder(søknadsprosess, "saksbehandler67").resultat().also {
            assertEquals(listOf("12", "6.1", "7.1"), it["fakta"].map { it["id"].asText() })
            assertEquals(3, it["fakta"].size())
        }
    }

    @Test
    fun `bygger oppgave event`() {
        val søknadprosess = søknadprosess(
            (prototypeFakta.boolsk(1) er true).oppfyltGodkjentAv(prototypeFakta.boolsk(2)) hvisOppfylt
                { (prototypeFakta.boolsk(3) er true).ikkeOppfyltGodkjentAv(prototypeFakta.boolsk(4)) }
        )

        søknadprosess.boolsk(1).besvar(true)
        val json = SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat()

        assertEquals("oppgave", json["@event_name"].asText())
        assertDoesNotThrow { UUID.fromString(json["søknad_uuid"].asText()) }
        assertEquals("saksbehandler2", json["seksjon_navn"].asText())
        assertEquals(2, json["fakta"].size())
        assertEquals("2", json["fakta"][0]["id"].asText())
        assertEquals("f2", json["fakta"][0]["navn"].asText())
        assertEquals("boolean", json["fakta"][0]["type"].asText())
        assertEquals(
            setOf(Rolle.saksbehandler.typeNavn),
            json["fakta"][0]["roller"].map { it.asText() }.toSet()
        )
        assertEquals(
            setOf(Rolle.søker.typeNavn),
            json["fakta"][1]["roller"].map { it.asText() }.toSet()
        )
        assertEquals(listOf("1"), json["fakta"][0]["godkjenner"].map { it.asText() })
        assertTrue(json["fakta"][1]["godkjenner"].map { it.asText() }.isEmpty())
    }

    @Test
    fun `inkluderer utledede faktum`() {
        val søknadprosess =
            søknadprosess((prototypeFakta.dato(10) etter prototypeFakta.dato(8)).godkjentAv(prototypeFakta.boolsk(11)))
        søknadprosess.dato(8).besvar(1.januar)
        søknadprosess.dato(9).besvar(10.januar)

        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler5").resultat().also { json ->
            assertEquals(4, json["fakta"].size())
            // assertTrue(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
        }
    }

    @Test
    fun `enkel subsumsjon`() {
        val søknadprosess = søknadprosess(prototypeFakta.boolsk(1) er true)
        søknadprosess.boolsk(1).besvar(true)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertTrue(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
        }
    }

    @Test
    fun `subsumsjon med oppfylt sti`() {
        val søknadprosess = søknadprosess(
            prototypeFakta.boolsk(1) er true hvisOppfylt {
                prototypeFakta.boolsk(3) er true
            }
        )
        søknadprosess.boolsk(1).besvar(true)
        søknadprosess.boolsk(3).besvar(false)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(2, json["subsumsjoner"].size())
            assertTrue(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
            assertFalse(json["subsumsjoner"][1]["lokalt_resultat"].asBoolean())
        }

        søknadprosess.boolsk(1).besvar(false)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertFalse(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
        }
    }

    @Test
    fun `subsumsjon med ikke oppfylt sti`() {
        val søknadprosess = søknadprosess(
            prototypeFakta.boolsk(1) er true hvisIkkeOppfylt {
                prototypeFakta.boolsk(3) er true
            }
        )
        søknadprosess.boolsk(1).besvar(false)
        søknadprosess.boolsk(3).besvar(false)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(2, json["subsumsjoner"].size())
            assertFalse(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
            assertFalse(json["subsumsjoner"][1]["lokalt_resultat"].asBoolean())
        }

        søknadprosess.boolsk(1).besvar(true)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertTrue(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
        }
    }

    @Test
    fun `allesubsumsjon`() {
        val søknadprosess = søknadprosess(
            "alle".alle(
                prototypeFakta.boolsk(1) er true,
                prototypeFakta.boolsk(3) er true
            )
        )
        søknadprosess.boolsk(1).besvar(true)
        søknadprosess.boolsk(3).besvar(false)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertEquals(2, json["subsumsjoner"][0]["subsumsjoner"].size())
            assertFalse(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
            assertTrue(json["subsumsjoner"][0]["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
            assertFalse(json["subsumsjoner"][0]["subsumsjoner"][1]["lokalt_resultat"].asBoolean())
        }
    }

    @Test
    fun `minstEnAv subsumsjon`() {
        val søknadprosess = søknadprosess(
            "minstEnAv".minstEnAv(
                prototypeFakta.boolsk(1) er true,
                prototypeFakta.boolsk(3) er true
            )
        )
        søknadprosess.boolsk(1).besvar(true)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertEquals(2, json["subsumsjoner"][0]["subsumsjoner"].size())
            assertTrue(json["subsumsjoner"][0]["lokalt_resultat"].isNull)
            assertTrue(json["subsumsjoner"][0]["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
            assertTrue(json["subsumsjoner"][0]["subsumsjoner"][1]["lokalt_resultat"].isNull)
        }

        søknadprosess.boolsk(3).besvar(false)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertEquals(2, json["subsumsjoner"][0]["subsumsjoner"].size())
            assertTrue(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
            assertTrue(json["subsumsjoner"][0]["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
            assertFalse(json["subsumsjoner"][0]["subsumsjoner"][1]["lokalt_resultat"].asBoolean())
        }
    }

    @Test
    fun `deltre subsumsjon`() {
        val søknadprosess = søknadprosess(
            "deltre" deltre {
                prototypeFakta.boolsk(1) er true hvisIkkeOppfylt {
                    prototypeFakta.boolsk(3) er true
                }
            }
        )

        søknadprosess.boolsk(1).besvar(false)
        søknadprosess.boolsk(3).besvar(false)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertEquals(2, json["subsumsjoner"][0]["subsumsjoner"].size())
            assertFalse(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
            assertFalse(json["subsumsjoner"][0]["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
            assertFalse(json["subsumsjoner"][0]["subsumsjoner"][1]["lokalt_resultat"].asBoolean())
        }

        søknadprosess.boolsk(1).besvar(true)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertEquals(1, json["subsumsjoner"][0]["subsumsjoner"].size())
            assertTrue(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
            assertTrue(json["subsumsjoner"][0]["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
        }
    }

    @Test
    fun `kombinasjoner av samensatte subsumsjoner`() {
        val søknadprosess = søknadprosess(
            "alle".alle(
                "deltre nivå 2" deltre { prototypeFakta.boolsk(1) er true },
                "alle nivå 2".alle(
                    prototypeFakta.boolsk(3) er true,
                    prototypeFakta.boolsk(5) er true
                )
            )
        )

        søknadprosess.boolsk(1).besvar(false)
        søknadprosess.boolsk(3).besvar(false)
        søknadprosess.boolsk(5).besvar(true)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertEquals(2, json["subsumsjoner"][0]["subsumsjoner"].size())
            assertEquals(1, json["subsumsjoner"][0]["subsumsjoner"][0]["subsumsjoner"].size())
            assertEquals(2, json["subsumsjoner"][0]["subsumsjoner"][1]["subsumsjoner"].size())

            assertFalse(json["subsumsjoner"][0]["subsumsjoner"][0]["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
            assertFalse(json["subsumsjoner"][0]["subsumsjoner"][1]["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
            assertTrue(json["subsumsjoner"][0]["subsumsjoner"][1]["subsumsjoner"][1]["lokalt_resultat"].asBoolean())
            assertFalse(json["subsumsjoner"][0]["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
            assertFalse(json["subsumsjoner"][0]["subsumsjoner"][1]["lokalt_resultat"].asBoolean())
            assertFalse(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
        }

        søknadprosess.boolsk(1).besvar(true)
        søknadprosess.boolsk(3).besvar(true)

        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertTrue(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
        }
    }

    @Test
    fun `godkjenningsubsumsjoner`() {
        val søknadprosess = søknadprosess(
            (
                prototypeFakta.boolsk(1) er true
                ).oppfyltGodkjentAv(prototypeFakta.boolsk(2))
        )

        søknadprosess.boolsk(1).besvar(true)
        søknadprosess.boolsk(2).besvar(false)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertEquals(2, json["subsumsjoner"][0]["subsumsjoner"].size())
        }

        søknadprosess.boolsk(2).besvar(true)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertEquals(2, json["subsumsjoner"][0]["subsumsjoner"].size())
        }

        søknadprosess.boolsk(1).besvar(false)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertEquals(1, json["subsumsjoner"][0]["subsumsjoner"].size())
        }
    }

    @Test
    fun ` template subsumsjoner`() {
        val template = "template" deltre {
            "alle".alle(
                prototypeFakta.boolsk(6) er true,
                prototypeFakta.boolsk(7) er true
            )
        }
        val søknadprosess = søknadprosess(
            prototypeFakta.generator(67) med template
        )
        søknadprosess.generator(67).besvar(3)
        søknadprosess.boolsk("6.1").besvar(true)
        søknadprosess.boolsk("6.2").besvar(true)
        søknadprosess.boolsk("6.3").besvar(false)

        søknadprosess.boolsk("7.1").besvar(true)
        søknadprosess.boolsk("7.2").besvar(false)
        søknadprosess.boolsk("7.3").besvar(false)

        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertEquals("Deltre subsumsjon", json["subsumsjoner"][0]["type"].asText())
            assertEquals(2, json["subsumsjoner"][0]["subsumsjoner"].size())
            assertEquals("Enkel subsumsjon", json["subsumsjoner"][0]["subsumsjoner"][0]["type"].asText())
            assertEquals("Alle subsumsjon", json["subsumsjoner"][0]["subsumsjoner"][1]["type"].asText())
            assertEquals(3, json["subsumsjoner"][0]["subsumsjoner"][1]["subsumsjoner"].size())
            assertEquals(
                "Deltre subsumsjon",
                json["subsumsjoner"][0]["subsumsjoner"][1]["subsumsjoner"][0]["type"].asText()
            )
            assertEquals(1, json["subsumsjoner"][0]["subsumsjoner"][1]["subsumsjoner"][0]["subsumsjoner"].size())
            assertEquals(
                "Alle subsumsjon",
                json["subsumsjoner"][0]["subsumsjoner"][1]["subsumsjoner"][0]["subsumsjoner"][0]["type"].asText()
            )
            assertEquals(
                2,
                json["subsumsjoner"][0]["subsumsjoner"][1]["subsumsjoner"][0]["subsumsjoner"][0]["subsumsjoner"].size()
            )
            assertTrue(json["subsumsjoner"][0]["subsumsjoner"][1]["subsumsjoner"][0]["subsumsjoner"][0]["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
            assertFalse(json["subsumsjoner"][0]["subsumsjoner"][1]["subsumsjoner"][2]["subsumsjoner"][0]["subsumsjoner"][1]["lokalt_resultat"].asBoolean())
        }
    }

    @Test
    fun `Komplekse seksjoner`() {
        assertFaktaStørrelseISeksjon(5, "seksjon8")
        assertFaktaStørrelseISeksjon(4, "seksjon9")
    }

    @Test
    fun `Genererte seksjoner kan bli sendt`() {
        val fakta = NyttEksempel().utredningsprosess
        fakta.heltall(15).besvar(3)
        var json = SaksbehandlerJsonBuilder(fakta, "seksjon8").resultat()
        assertEquals(8, json["fakta"].size())
    }

    @AfterEach
    fun clean() {
        clearStaticMockk(ResourceBundle::class)
    }

    private fun assertFaktaStørrelseISeksjon(expected: Int, seksjonNavn: String) {
        val json = SaksbehandlerJsonBuilder(NyttEksempel().utredningsprosess, seksjonNavn).resultat()
        assertEquals(expected, json["fakta"].size())
    }

    private fun søknadprosess(prototypeSubsumsjon: Subsumsjon): Utredningsprosess {
        val prototypeUtredningsprosess = Utredningsprosess(
            prototypeFakta,
            Seksjon(
                "søker",
                Rolle.søker,
                prototypeFakta.boolsk(1),
                prototypeFakta.boolsk(3),
                prototypeFakta.boolsk(5),
                prototypeFakta.boolsk(6),
                prototypeFakta.boolsk(7)
            ),
            Seksjon(
                "Genereres",
                Rolle.søker,
                prototypeFakta.boolsk(13),
                prototypeFakta.boolsk(14)
            ),
            Seksjon("saksbehandler2", Rolle.saksbehandler, prototypeFakta.boolsk(2)),
            Seksjon("saksbehandler4", Rolle.saksbehandler, prototypeFakta.boolsk(4)),
            Seksjon("saksbehandler5", Rolle.saksbehandler, prototypeFakta.boolsk(11)),
            Seksjon("saksbehandler67", Rolle.saksbehandler, prototypeFakta.boolsk(12)),
            rootSubsumsjon = prototypeSubsumsjon
        )

        return Versjon.Bygger(
            prototypeFakta,
            prototypeSubsumsjon,
            prototypeUtredningsprosess
        ).utredningsprosess(testPerson)
    }
}

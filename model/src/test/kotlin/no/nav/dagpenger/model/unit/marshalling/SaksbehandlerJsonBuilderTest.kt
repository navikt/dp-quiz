package no.nav.dagpenger.model.unit.marshalling

import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.mockkStatic
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.NyttEksempel
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.marshalling.SaksbehandlerJsonBuilder
import no.nav.dagpenger.model.marshalling.Språk.Companion.nynorsk
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.etter
import no.nav.dagpenger.model.regel.godkjentAv
import no.nav.dagpenger.model.regel.gyldigGodkjentAv
import no.nav.dagpenger.model.regel.med
import no.nav.dagpenger.model.regel.ugyldigGodkjentAv
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.seksjon.Versjon.UserInterfaceType.Web
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.hvisGyldig
import no.nav.dagpenger.model.subsumsjon.hvisUgyldig
import no.nav.dagpenger.model.subsumsjon.makro
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.Enumeration
import java.util.Locale
import java.util.ResourceBundle
import java.util.UUID

internal class SaksbehandlerJsonBuilderTest {
    private lateinit var prototypeSøknad: Søknad

    companion object {
        private var versjonId = 170
    }

    private class ResourceBundleMock(val lokal: Locale) : ResourceBundle() {
        override fun handleGetObject(key: String): Any {
            return "Oversatt tekst"
        }

        override fun getKeys(): Enumeration<String> {
            TODO("Not yet implemented")
        }
    }

    private val mockBundle = ResourceBundleMock(nynorsk)

    @BeforeEach
    fun setup() {
        versjonId--
        prototypeSøknad = Søknad(
            versjonId,
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
        mockkStatic(ResourceBundle::class.java.name)
        every {
            ResourceBundle.getBundle(any() as String, any() as Locale)
        } returns mockBundle
    }

    @Test
    fun `inkluderer genererte faktum som faktumet avhengerAv`() {
        val søknadsprosess = søknadprosess(
            (prototypeSøknad.heltall(67) er 1).godkjentAv(prototypeSøknad.boolsk(12))
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
            (prototypeSøknad.boolsk(1) er true).gyldigGodkjentAv(prototypeSøknad.boolsk(2)) hvisGyldig
                { (prototypeSøknad.boolsk(3) er true).ugyldigGodkjentAv(prototypeSøknad.boolsk(4)) }
        )

        søknadprosess.boolsk(1).besvar(true)
        val json = SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2", lokal = nynorsk).resultat()

        assertEquals("oppgave", json["@event_name"].asText())
        assertDoesNotThrow { UUID.fromString(json["søknad_uuid"].asText()) }
        assertEquals("saksbehandler2", json["seksjon_navn"].asText())
        assertEquals(2, json["fakta"].size())
        assertEquals("2", json["fakta"][0]["id"].asText())
        assertEquals("Oversatt tekst", json["fakta"][0]["navn"].asText())
        assertEquals("boolean", json["fakta"][0]["type"].asText())
        assertEquals(Locale("nn", "NO"), mockBundle.lokal)
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
            søknadprosess((prototypeSøknad.dato(10) etter prototypeSøknad.dato(8)).godkjentAv(prototypeSøknad.boolsk(11)))
        søknadprosess.dato(8).besvar(1.januar)
        søknadprosess.dato(9).besvar(10.januar)

        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler5").resultat().also { json ->
            assertEquals(4, json["fakta"].size())
            // assertTrue(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
        }
    }

    @Test
    fun `enkel subsumsjon`() {
        val søknadprosess = søknadprosess(prototypeSøknad.boolsk(1) er true)
        søknadprosess.boolsk(1).besvar(true)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertTrue(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
        }
    }

    @Test
    fun `subsumsjon med gyldig sti`() {
        val søknadprosess = søknadprosess(
            prototypeSøknad.boolsk(1) er true hvisGyldig {
                prototypeSøknad.boolsk(3) er true
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
    fun `subsumsjon med ugyldig sti`() {
        val søknadprosess = søknadprosess(
            prototypeSøknad.boolsk(1) er true hvisUgyldig {
                prototypeSøknad.boolsk(3) er true
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
                prototypeSøknad.boolsk(1) er true,
                prototypeSøknad.boolsk(3) er true
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
                prototypeSøknad.boolsk(1) er true,
                prototypeSøknad.boolsk(3) er true
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
    fun `makro subsumsjon`() {
        val søknadprosess = søknadprosess(
            "makro" makro {
                prototypeSøknad.boolsk(1) er true hvisUgyldig {
                    prototypeSøknad.boolsk(3) er true
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
                "makro nivå 2" makro { prototypeSøknad.boolsk(1) er true },
                "alle nivå 2".alle(
                    prototypeSøknad.boolsk(3) er true,
                    prototypeSøknad.boolsk(5) er true
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
                prototypeSøknad.boolsk(1) er true
                ).gyldigGodkjentAv(prototypeSøknad.boolsk(2))
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
        val template = "template" makro {
            "alle".alle(
                prototypeSøknad.boolsk(6) er true,
                prototypeSøknad.boolsk(7) er true
            )
        }
        val søknadprosess = søknadprosess(
            prototypeSøknad.generator(67) med template
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
            assertEquals("Makro subsumsjon", json["subsumsjoner"][0]["type"].asText())
            assertEquals(2, json["subsumsjoner"][0]["subsumsjoner"].size())
            assertEquals("Enkel subsumsjon", json["subsumsjoner"][0]["subsumsjoner"][0]["type"].asText())
            assertEquals("Alle subsumsjon", json["subsumsjoner"][0]["subsumsjoner"][1]["type"].asText())
            assertEquals(3, json["subsumsjoner"][0]["subsumsjoner"][1]["subsumsjoner"].size())
            assertEquals(
                "Makro subsumsjon",
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
        val fakta = NyttEksempel().søknadprosess
        fakta.heltall(15).besvar(3)
        var json = SaksbehandlerJsonBuilder(fakta, "seksjon8").resultat()
        assertEquals(8, json["fakta"].size())
    }

    @AfterEach
    fun clean() {
        clearStaticMockk(ResourceBundle::class)
    }

    private fun assertFaktaStørrelseISeksjon(expected: Int, seksjonNavn: String) {
        val json = SaksbehandlerJsonBuilder(NyttEksempel().søknadprosess, seksjonNavn).resultat()
        assertEquals(expected, json["fakta"].size())
    }

    private fun søknadprosess(prototypeSubsumsjon: Subsumsjon): Søknadprosess {
        val prototypeFaktagrupper = Søknadprosess(
            prototypeSøknad,
            Seksjon(
                "søker",
                Rolle.søker,
                prototypeSøknad.boolsk(1),
                prototypeSøknad.boolsk(3),
                prototypeSøknad.boolsk(5),
                prototypeSøknad.boolsk(6),
                prototypeSøknad.boolsk(7)
            ),
            Seksjon(
                "Genereres",
                Rolle.søker,
                prototypeSøknad.boolsk(13),
                prototypeSøknad.boolsk(14)
            ),
            Seksjon("saksbehandler2", Rolle.saksbehandler, prototypeSøknad.boolsk(2)),
            Seksjon("saksbehandler4", Rolle.saksbehandler, prototypeSøknad.boolsk(4)),
            Seksjon("saksbehandler5", Rolle.saksbehandler, prototypeSøknad.boolsk(11)),
            Seksjon("saksbehandler67", Rolle.saksbehandler, prototypeSøknad.boolsk(12)),
            rootSubsumsjon = prototypeSubsumsjon
        )

        return Versjon.Bygger(
            prototypeSøknad,
            prototypeSubsumsjon,
            mapOf(Web to prototypeFaktagrupper)
        ).søknadprosess(testPerson, Web)
    }
}

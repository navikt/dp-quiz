package no.nav.dagpenger.model.unit.marshalling

import io.mockk.clearStaticMockk
import io.mockk.every
import io.mockk.mockkStatic
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.NyttEksempel
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.marshalling.SaksbehandlerJsonBuilder
import no.nav.dagpenger.model.marshalling.Språk.Companion.nynorsk
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.gyldigGodkjentAv
import no.nav.dagpenger.model.regel.med
import no.nav.dagpenger.model.regel.ugyldigGodkjentAv
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.seksjon.Versjon.UserInterfaceType.Web
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.eller
import no.nav.dagpenger.model.subsumsjon.makro
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.så
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
            ja nei "f1" id 1,
            ja nei "f2" id 2 avhengerAv 1,
            ja nei "f3" id 3,
            ja nei "f4" id 4 avhengerAv 3,
            ja nei "f5" id 5,
            ja nei "f6" id 6,
            ja nei "f7" id 7,
            heltall faktum "f67" id 67 genererer 6 og 7
        )
        mockkStatic(ResourceBundle::class.java.name)
        every {
            ResourceBundle.getBundle(any() as String, any() as Locale)
        } returns mockBundle
    }

    @Test
    fun `bygger oppgave event`() {
        val søknadprosess = søknadprosess(
            prototypeSøknad.ja(1) er true gyldigGodkjentAv prototypeSøknad.ja(2) så
                (prototypeSøknad.ja(3) er true ugyldigGodkjentAv prototypeSøknad.ja(4))
        )

        søknadprosess.ja(1).besvar(true)
        val json = SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2", lokal = nynorsk).resultat()

        assertEquals("oppgave", json["@event_name"].asText())
        assertDoesNotThrow { UUID.fromString(json["søknad_uuid"].asText()) }
        assertEquals("saksbehandler2", json["seksjon_navn"].asText())
        assertEquals(2, json["fakta"].size())
        assertEquals("2", json["fakta"][0]["id"].asText())
        assertEquals("Oversatt tekst", json["fakta"][0]["navn"].asText())
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

    @Test
    fun `subsumsjon med ugyldig sti`() {
        val søknadprosess = søknadprosess(
            prototypeSøknad.ja(1) er true eller (
                prototypeSøknad.ja(3) er true
                )
        )
        søknadprosess.ja(1).besvar(false)
        søknadprosess.ja(3).besvar(false)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(2, json["subsumsjoner"].size())
            assertFalse(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
            assertFalse(json["subsumsjoner"][1]["lokalt_resultat"].asBoolean())
        }

        søknadprosess.ja(1).besvar(true)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertTrue(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
        }
    }

    @Test
    fun `allesubsumsjon`() {
        val søknadprosess = søknadprosess(
            "alle".alle(
                prototypeSøknad.ja(1) er true,
                prototypeSøknad.ja(3) er true
            )
        )
        søknadprosess.ja(1).besvar(true)
        søknadprosess.ja(3).besvar(false)
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
                prototypeSøknad.ja(1) er true,
                prototypeSøknad.ja(3) er true
            )
        )
        søknadprosess.ja(1).besvar(true)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertEquals(2, json["subsumsjoner"][0]["subsumsjoner"].size())
            assertTrue(json["subsumsjoner"][0]["lokalt_resultat"].isNull)
            assertTrue(json["subsumsjoner"][0]["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
            assertTrue(json["subsumsjoner"][0]["subsumsjoner"][1]["lokalt_resultat"].isNull)
        }

        søknadprosess.ja(3).besvar(false)
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
            "makro" makro(
                prototypeSøknad.ja(1) er true eller (
                    prototypeSøknad.ja(3) er true
                    )
                )
        )

        søknadprosess.ja(1).besvar(false)
        søknadprosess.ja(3).besvar(false)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertEquals(2, json["subsumsjoner"][0]["subsumsjoner"].size())
            assertFalse(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
            assertFalse(json["subsumsjoner"][0]["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
            assertFalse(json["subsumsjoner"][0]["subsumsjoner"][1]["lokalt_resultat"].asBoolean())
        }

        søknadprosess.ja(1).besvar(true)
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
                "makro nivå 2" makro (prototypeSøknad.ja(1) er true),
                "alle nivå 2".alle(
                    prototypeSøknad.ja(3) er true,
                    prototypeSøknad.ja(5) er true
                )
            )
        )

        søknadprosess.ja(1).besvar(false)
        søknadprosess.ja(3).besvar(false)
        søknadprosess.ja(5).besvar(true)
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

        søknadprosess.ja(1).besvar(true)
        søknadprosess.ja(3).besvar(true)

        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertTrue(json["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
        }
    }

    @Test
    fun `godkjenningsubsumsjoner`() {
        val søknadprosess = søknadprosess(
            (
                prototypeSøknad.ja(1) er true
                ) gyldigGodkjentAv prototypeSøknad.ja(2)
        )

        søknadprosess.ja(1).besvar(true)
        søknadprosess.ja(2).besvar(false)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertEquals(2, json["subsumsjoner"][0]["subsumsjoner"].size())
        }

        søknadprosess.ja(2).besvar(true)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertEquals(2, json["subsumsjoner"][0]["subsumsjoner"].size())
        }

        søknadprosess.ja(1).besvar(false)
        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertEquals(1, json["subsumsjoner"][0]["subsumsjoner"].size())
        }
    }

    @Test
    fun ` template subsumsjoner`() {
        val template = "template" makro (
            "alle".alle(
                prototypeSøknad.ja(6) er true,
                prototypeSøknad.ja(7) er true
            )
            )

        val søknadprosess = søknadprosess(
            prototypeSøknad.generator(67) med template
        )
        søknadprosess.generator(67).besvar(3)
        søknadprosess.ja("6.1").besvar(true)
        søknadprosess.ja("6.2").besvar(true)
        søknadprosess.ja("6.3").besvar(false)

        søknadprosess.ja("7.1").besvar(true)
        søknadprosess.ja("7.2").besvar(false)
        søknadprosess.ja("7.3").besvar(false)

        SaksbehandlerJsonBuilder(søknadprosess, "saksbehandler2").resultat().also { json ->
            assertEquals(1, json["subsumsjoner"].size())
            assertEquals("Makro subsumsjon", json["subsumsjoner"][0]["type"].asText())
            assertEquals(2, json["subsumsjoner"][0]["subsumsjoner"].size())
            assertEquals("Enkel subsumsjon", json["subsumsjoner"][0]["subsumsjoner"][0]["type"].asText())
            assertEquals("Alle subsumsjon", json["subsumsjoner"][0]["subsumsjoner"][1]["type"].asText())
            assertEquals(3, json["subsumsjoner"][0]["subsumsjoner"][1]["subsumsjoner"].size())
            assertEquals("Makro subsumsjon", json["subsumsjoner"][0]["subsumsjoner"][1]["subsumsjoner"][0]["type"].asText())
            assertEquals(1, json["subsumsjoner"][0]["subsumsjoner"][1]["subsumsjoner"][0]["subsumsjoner"].size())
            assertEquals("Alle subsumsjon", json["subsumsjoner"][0]["subsumsjoner"][1]["subsumsjoner"][0]["subsumsjoner"][0]["type"].asText())
            assertEquals(2, json["subsumsjoner"][0]["subsumsjoner"][1]["subsumsjoner"][0]["subsumsjoner"][0]["subsumsjoner"].size())
            assertTrue(json["subsumsjoner"][0]["subsumsjoner"][1]["subsumsjoner"][0]["subsumsjoner"][0]["subsumsjoner"][0]["lokalt_resultat"].asBoolean())
            assertFalse(json["subsumsjoner"][0]["subsumsjoner"][1]["subsumsjoner"][2]["subsumsjoner"][0]["subsumsjoner"][1]["lokalt_resultat"].asBoolean())
        }
    }

    @Test
    fun `Komplekse seksjoner`() {
        assertSeksjonSize(7, "seksjon8")
        assertSeksjonSize(4, "seksjon9")
    }

    @Test
    fun `Genererte seksjoner kan bli sendt`() {
        val fakta = NyttEksempel().søknadprosess
        fakta.heltall(15).besvar(3)
        var json = SaksbehandlerJsonBuilder(fakta, "seksjon8").resultat()
        assertEquals(10, json["fakta"].size())
    }

    @AfterEach
    fun clean() {
        clearStaticMockk(ResourceBundle::class)
    }

    private fun assertSeksjonSize(expected: Int, seksjonNavn: String) {
        val json = SaksbehandlerJsonBuilder(NyttEksempel().søknadprosess, seksjonNavn).resultat()
        assertEquals(expected, json["fakta"].size())
    }

    private fun søknadprosess(prototypeSubsumsjon: Subsumsjon): Søknadprosess {
        val prototypeFaktagrupper = Søknadprosess(
            prototypeSøknad,
            Seksjon(
                "søker",
                Rolle.søker,
                prototypeSøknad.ja(1),
                prototypeSøknad.ja(3),
                prototypeSøknad.ja(5),
                prototypeSøknad.ja(6),
                prototypeSøknad.ja(7)
            ),
            Seksjon("saksbehandler2", Rolle.saksbehandler, prototypeSøknad.ja(2)),
            Seksjon("saksbehandler4", Rolle.saksbehandler, prototypeSøknad.ja(4)),
            rootSubsumsjon = prototypeSubsumsjon
        )

        Versjon(
            prototypeSøknad,
            prototypeSubsumsjon,
            mapOf(Web to prototypeFaktagrupper)
        )

        return Versjon.id(versjonId).søknadprosess(testPerson, Web)
    }
}

package no.nav.dagpenger.model.unit.marshalling

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.helpers.TestProsesser
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.marshalling.ResultatJsonBuilder
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfylt
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ResultatJsonBuilderTest {
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
        )
    }

    @Test
    fun `bygger prossess_resultat event`() {
        val søknadprosess = søknadprosess(
            prototypeFakta.boolsk(1) er true,
        )
        assertThrows<IllegalStateException> {
            ResultatJsonBuilder(søknadprosess).resultat()
        }

        søknadprosess.boolsk(1).besvar(true)
        ResultatJsonBuilder(søknadprosess).resultat().also {
            assertTrue(it["resultat"].asBoolean())
        }

        søknadprosess.boolsk(1).besvar(false)
        ResultatJsonBuilder(søknadprosess).resultat().also {
            assertFalse(it["resultat"].asBoolean())
            assertEquals(0, it["versjon_id"].asInt())
            assertEquals("test", it["versjon_navn"].asText())
        }
    }

    @Test
    fun `inkluderer kun mulige paths`() {
        val søknadprosess = søknadprosess(
            prototypeFakta.boolsk(1) er true hvisOppfylt {
                prototypeFakta.boolsk(2) er true
            } hvisIkkeOppfylt {
                prototypeFakta.boolsk(3) er true
            },
        )
        søknadprosess.boolsk(1).besvar(true)
        søknadprosess.boolsk(2).besvar(true)
        ResultatJsonBuilder(søknadprosess).resultat().also {
            assertTrue(it["resultat"].asBoolean())

            assertEquals(2, it["subsumsjoner"].size())
        }
    }

    @Test
    fun `inkluderer besvartAv`() {
        val søknadprosess = søknadprosess(
            prototypeFakta.boolsk(1) er true hvisOppfylt {
                prototypeFakta.boolsk(2) er true
            } hvisIkkeOppfylt {
                prototypeFakta.boolsk(3) er true
            },
        )
        søknadprosess.boolsk(1).besvar(true, "A123456")
        søknadprosess.boolsk(2).besvar(true)
        ResultatJsonBuilder(søknadprosess).resultat().also {
            assertEquals("A123456", it["fakta"][0]["besvartAv"].asText())
        }
    }

    private fun søknadprosess(prototypeSubsumsjon: Subsumsjon): Prosess = Prosess(
        TestProsesser.Test,
        prototypeFakta,
        Seksjon(
            "søker",
            Rolle.søker,
            prototypeFakta.boolsk(1),
            prototypeFakta.boolsk(3),
            prototypeFakta.boolsk(5),
            prototypeFakta.boolsk(6),
            prototypeFakta.boolsk(7),
        ),
        Seksjon("saksbehandler2", Rolle.saksbehandler, prototypeFakta.boolsk(2)),
        Seksjon("saksbehandler4", Rolle.saksbehandler, prototypeFakta.boolsk(4)),
        rootSubsumsjon = prototypeSubsumsjon,
    )
}

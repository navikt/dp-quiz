package no.nav.dagpenger.model.unit.marshalling

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.marshalling.ResultatJsonBuilder
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.eller
import no.nav.dagpenger.model.subsumsjon.så
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class ResultatJsonBuilderTest {
    private lateinit var prototypeSøknad: Søknad

    companion object {
        private var versjonId = runCatching { Versjon.siste }.getOrDefault(0)
    }

    @BeforeEach
    fun setup() {
        versjonId++
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
    }

    @Test
    fun `bygger prossess_resultat event`() {
        val søknadprosess = søknadprosess(
            prototypeSøknad.ja(1) er true
        )
        assertThrows<IllegalStateException> {
            ResultatJsonBuilder(søknadprosess).resultat()
        }

        søknadprosess.ja(1).besvar(true)
        ResultatJsonBuilder(søknadprosess).resultat().also {
            assertTrue(it["resultat"].asBoolean())
        }

        søknadprosess.ja(1).besvar(false)
        ResultatJsonBuilder(søknadprosess).resultat().also {
            assertFalse(it["resultat"].asBoolean())
        }
    }

    @Test
    fun `inkluderer kun mulige paths`() {
        val søknadprosess = søknadprosess(
            prototypeSøknad.ja(1) er true så (prototypeSøknad.ja(2) er true) eller (prototypeSøknad.ja(3) er true)
        )
        søknadprosess.ja(1).besvar(true)
        søknadprosess.ja(2).besvar(true)
        ResultatJsonBuilder(søknadprosess).resultat().also {
            assertTrue(it["resultat"].asBoolean())

            assertEquals(2, it["subsumsjoner"].size())
        }
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
            mapOf(Versjon.UserInterfaceType.Web to prototypeFaktagrupper)
        )

        return Versjon.id(versjonId).søknadprosess(testPerson, Versjon.UserInterfaceType.Web)
    }
}

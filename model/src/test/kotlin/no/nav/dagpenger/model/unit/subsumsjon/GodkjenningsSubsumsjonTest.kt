package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.godkjentAv
import no.nav.dagpenger.model.regel.ikkeOppfyltGodkjentAv
import no.nav.dagpenger.model.regel.oppfyltGodkjentAv
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class GodkjenningsSubsumsjonTest {
    private lateinit var godkjenningsSubsumsjon: Subsumsjon
    private lateinit var faktum: Faktum<Boolean>
    private lateinit var godkjenning: Faktum<Boolean>

    @Test
    fun `Godkjenning uansett resultat av child`() {
        søknadprosess { fakta -> (fakta boolsk 1 er true).godkjentAv(fakta boolsk 2) }

        assertEquals(null, godkjenningsSubsumsjon.resultat())
        faktum.besvar(true)
        assertEquals(true, godkjenningsSubsumsjon.resultat())
        godkjenning.besvar(false)
        assertEquals(false, godkjenningsSubsumsjon.resultat())
        godkjenning.besvar(true)
        assertEquals(true, godkjenningsSubsumsjon.resultat())
        faktum.besvar(false)
        assertEquals(false, godkjenningsSubsumsjon.resultat())
        assertFalse(godkjenning.erBesvart())
        godkjenning.besvar(true)
        assertEquals(false, godkjenningsSubsumsjon.resultat())
        godkjenning.besvar(false)
        assertEquals(true, godkjenningsSubsumsjon.resultat())
    }

    @Test
    fun `Godkjenning av oppfylt sti av child`() {
        søknadprosess { fakta -> (fakta boolsk 1 er true).oppfyltGodkjentAv(fakta boolsk 2) }

        assertEquals(null, godkjenningsSubsumsjon.resultat())
        faktum.besvar(true)
        assertEquals(true, godkjenningsSubsumsjon.resultat())
        godkjenning.besvar(false)
        assertEquals(null, godkjenningsSubsumsjon.resultat())
        godkjenning.besvar(true)
        assertEquals(true, godkjenningsSubsumsjon.resultat())
        faktum.besvar(false)
        assertEquals(false, godkjenningsSubsumsjon.resultat())
        assertFalse(godkjenning.erBesvart())
        godkjenning.besvar(true)
        assertEquals(false, godkjenningsSubsumsjon.resultat())
        godkjenning.besvar(false)
        assertEquals(null, godkjenningsSubsumsjon.resultat())
    }

    @Test
    fun `Godkjenning av ikke oppfylt sti av child`() {
        søknadprosess { fakta -> (fakta boolsk 1 er true).ikkeOppfyltGodkjentAv(fakta boolsk 2) }
        assertEquals(null, godkjenningsSubsumsjon.resultat())
        faktum.besvar(true)
        assertEquals(true, godkjenningsSubsumsjon.resultat())
        godkjenning.besvar(false)
        assertEquals(true, godkjenningsSubsumsjon.resultat())
        godkjenning.besvar(true)
        assertEquals(true, godkjenningsSubsumsjon.resultat())
        faktum.besvar(false)
        assertEquals(false, godkjenningsSubsumsjon.resultat())
        assertFalse(godkjenning.erBesvart())
        godkjenning.besvar(true)
        assertEquals(false, godkjenningsSubsumsjon.resultat())
        godkjenning.besvar(false)
        assertEquals(null, godkjenningsSubsumsjon.resultat())
    }

    @Test
    fun `Trenger avhengighet for å godkjenne`() {
        val prototypeSøknad = Søknad(
            testversjon,
            boolsk faktum "f1" id 1,
            boolsk faktum "approve1" id 2
        )

        assertThrows<IllegalArgumentException> {
            (prototypeSøknad.boolsk(1) er true).oppfyltGodkjentAv(
                prototypeSøknad.boolsk(
                    2
                )
            )
        }
        assertThrows<IllegalArgumentException> {
            (prototypeSøknad.boolsk(1) er true).ikkeOppfyltGodkjentAv(
                prototypeSøknad.boolsk(2)
            )
        }
        assertThrows<IllegalArgumentException> { (prototypeSøknad.boolsk(1) er true).godkjentAv(prototypeSøknad.boolsk(2)) }
    }

    private fun søknadprosess(block: (Søknad) -> Subsumsjon): Søknadprosess {
        val søknad = Søknad(
            testversjon,
            boolsk faktum "faktum" id 1,
            boolsk faktum "godkjenning" id 2 avhengerAv 1
        )

        faktum = søknad boolsk 1
        godkjenning = søknad boolsk 2

        godkjenningsSubsumsjon = block(søknad)
        return Søknadprosess(
            søknad,
            Seksjon("seksjon", Rolle.søker, faktum),
            Seksjon("seksjon", Rolle.saksbehandler, godkjenning),
            rootSubsumsjon = godkjenningsSubsumsjon
        )
    }
}

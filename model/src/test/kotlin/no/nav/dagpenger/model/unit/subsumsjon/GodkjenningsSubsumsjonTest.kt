package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.godkjentAv
import no.nav.dagpenger.model.subsumsjon.ikkeOppfyltGodkjentAv
import no.nav.dagpenger.model.subsumsjon.oppfyltGodkjentAv
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
        assertEquals(false, godkjenningsSubsumsjon.resultat())
        godkjenning.besvar(true)
        assertEquals(true, godkjenningsSubsumsjon.resultat())
        faktum.besvar(false)
        assertEquals(false, godkjenningsSubsumsjon.resultat())
        assertFalse(godkjenning.erBesvart())
        godkjenning.besvar(true)
        assertEquals(false, godkjenningsSubsumsjon.resultat())
        godkjenning.besvar(false)
        assertEquals(false, godkjenningsSubsumsjon.resultat())
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
        assertEquals(true, godkjenningsSubsumsjon.resultat())
    }

    @Test
    fun `Trenger avhengighet for å godkjenne`() {
        val prototypeFakta = Fakta(
            testversjon,
            boolsk faktum "f1" id 1,
            boolsk faktum "approve1" id 2
        )

        assertThrows<IllegalArgumentException> {
            (prototypeFakta.boolsk(1) er true).oppfyltGodkjentAv(
                prototypeFakta.boolsk(
                    2
                )
            )
        }
        assertThrows<IllegalArgumentException> {
            (prototypeFakta.boolsk(1) er true).ikkeOppfyltGodkjentAv(
                prototypeFakta.boolsk(2)
            )
        }
        assertThrows<IllegalArgumentException> { (prototypeFakta.boolsk(1) er true).godkjentAv(prototypeFakta.boolsk(2)) }
    }

    private fun søknadprosess(block: (Fakta) -> Subsumsjon): Utredningsprosess {
        val fakta = Fakta(
            testversjon,
            boolsk faktum "faktum" id 1,
            boolsk faktum "godkjenning" id 2 avhengerAv 1
        )

        faktum = fakta boolsk 1
        godkjenning = fakta boolsk 2

        godkjenningsSubsumsjon = block(fakta)
        return Utredningsprosess(
            fakta,
            Seksjon("seksjon", Rolle.søker, faktum),
            Seksjon("seksjon", Rolle.saksbehandler, godkjenning),
            rootSubsumsjon = godkjenningsSubsumsjon
        )
    }
}

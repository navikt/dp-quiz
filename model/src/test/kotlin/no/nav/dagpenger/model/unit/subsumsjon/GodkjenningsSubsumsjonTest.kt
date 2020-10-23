package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.godkjentAv
import no.nav.dagpenger.model.regel.gyldigGodkjentAv
import no.nav.dagpenger.model.regel.ugyldigGodkjentAv
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class GodkjenningsSubsumsjonTest {
    private lateinit var godkjenningsSubsumsjon: Subsumsjon
    private lateinit var faktum: Faktum<Boolean>
    private lateinit var godkjenning: Faktum<Boolean>

    @Test
    fun `Godkjenning uansett resultat av child`() {
        søknad { fakta -> fakta ja 1 er true godkjentAv (fakta ja 2) }

        assertEquals(null, godkjenningsSubsumsjon.resultat())
        faktum.besvar(true, Rolle.søker)
        assertEquals(true, godkjenningsSubsumsjon.resultat())
        godkjenning.besvar(false, Rolle.saksbehandler)
        assertEquals(false, godkjenningsSubsumsjon.resultat())
        godkjenning.besvar(true, Rolle.saksbehandler)
        assertEquals(true, godkjenningsSubsumsjon.resultat())
        faktum.besvar(false, Rolle.søker)
        assertEquals(false, godkjenningsSubsumsjon.resultat())
        assertFalse(godkjenning.erBesvart())
        godkjenning.besvar(true, Rolle.saksbehandler)
        assertEquals(false, godkjenningsSubsumsjon.resultat())
        godkjenning.besvar(false, Rolle.saksbehandler)
        assertEquals(true, godkjenningsSubsumsjon.resultat())
    }

    @Test
    fun `Godkjenning av gyldig sti av child`() {
        søknad { fakta -> fakta ja 1 er true gyldigGodkjentAv  (fakta ja 2) }

        assertEquals(null, godkjenningsSubsumsjon.resultat())
        faktum.besvar(true, Rolle.søker)
        assertEquals(true, godkjenningsSubsumsjon.resultat())
        godkjenning.besvar(false, Rolle.saksbehandler)
        assertEquals(false, godkjenningsSubsumsjon.resultat())
        godkjenning.besvar(true, Rolle.saksbehandler)
        assertEquals(true, godkjenningsSubsumsjon.resultat())
        faktum.besvar(false, Rolle.søker)
        assertEquals(false, godkjenningsSubsumsjon.resultat())
        assertFalse(godkjenning.erBesvart())
        godkjenning.besvar(true, Rolle.saksbehandler)
        assertEquals(false, godkjenningsSubsumsjon.resultat())
        godkjenning.besvar(false, Rolle.saksbehandler)
        assertEquals(false, godkjenningsSubsumsjon.resultat())
    }

    @Test
    fun `Godkjenning av ugyldig sti av child`() {
        søknad { fakta -> fakta ja 1 er true ugyldigGodkjentAv  (fakta ja 2) }

        assertEquals(null, godkjenningsSubsumsjon.resultat())
        faktum.besvar(true, Rolle.søker)
        assertEquals(true, godkjenningsSubsumsjon.resultat())
        godkjenning.besvar(false, Rolle.saksbehandler)
        assertEquals(true, godkjenningsSubsumsjon.resultat())
        godkjenning.besvar(true, Rolle.saksbehandler)
        assertEquals(true, godkjenningsSubsumsjon.resultat())
        faktum.besvar(false, Rolle.søker)
        assertEquals(false, godkjenningsSubsumsjon.resultat())
        assertFalse(godkjenning.erBesvart())
        godkjenning.besvar(true, Rolle.saksbehandler)
        assertEquals(false, godkjenningsSubsumsjon.resultat())
        godkjenning.besvar(false, Rolle.saksbehandler)
        assertEquals(true, godkjenningsSubsumsjon.resultat())
    }

    private fun søknad(block: (Fakta) -> Subsumsjon): Søknad{
        val fakta = Fakta(
                ja nei "faktum" id 1,
                ja nei "godkjenning" id 2 avhengerAv 1
        )

        faktum = fakta ja 1
        godkjenning = fakta ja 2

        godkjenningsSubsumsjon = block(fakta)
        return Søknad(
                fakta,
                Seksjon("seksjon", Rolle.søker, faktum),
                Seksjon("seksjon", Rolle.saksbehandler, godkjenning),
                rootSubsumsjon = godkjenningsSubsumsjon
        )
    }
}

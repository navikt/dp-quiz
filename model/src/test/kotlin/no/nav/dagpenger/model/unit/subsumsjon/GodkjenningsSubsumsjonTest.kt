package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.godkjentAv
import no.nav.dagpenger.model.regel.gyldigGodkjentAv
import no.nav.dagpenger.model.regel.ugyldigGodkjentAv
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class GodkjenningsSubsumsjonTest {

    @Test
    fun `Godkjenning uansett resultat av child`() {
        val faktum = FaktumNavn(1, "faktum").faktum(Boolean::class.java)
        val subsumsjon = faktum er true
        val godkjenning = FaktumNavn(2, "godkjenning").faktum(Boolean::class.java)
        val godkjenningsSubsumsjon = subsumsjon godkjentAv godkjenning
        godkjenning avhengerAv faktum
        Søknad(Seksjon("seksjon", Rolle.søker, faktum), Seksjon("seksjon", Rolle.saksbehandler, godkjenning))

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
        val faktum = FaktumNavn(1, "faktum").faktum(Boolean::class.java)
        val subsumsjon = faktum er true
        val godkjenning = FaktumNavn(2, "godkjenning").faktum(Boolean::class.java)
        val godkjenningsSubsumsjon = subsumsjon gyldigGodkjentAv godkjenning
        godkjenning avhengerAv faktum
        Søknad(Seksjon("seksjon", Rolle.søker, faktum), Seksjon("seksjon", Rolle.saksbehandler, godkjenning))

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
        val faktum = FaktumNavn(1, "faktum").faktum(Boolean::class.java)
        val subsumsjon = faktum er true
        val godkjenning = FaktumNavn(2, "godkjenning").faktum(Boolean::class.java)
        val godkjenningsSubsumsjon = subsumsjon ugyldigGodkjentAv godkjenning
        godkjenning avhengerAv faktum
        Søknad(Seksjon("seksjon", Rolle.søker, faktum), Seksjon("seksjon", Rolle.saksbehandler, godkjenning))

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
}

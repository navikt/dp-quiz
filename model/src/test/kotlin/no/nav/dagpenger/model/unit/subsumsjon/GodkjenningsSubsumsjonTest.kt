package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.godkjentAv
import no.nav.dagpenger.model.regel.gyldigGodkjentAv
import no.nav.dagpenger.model.regel.ugyldigGodkjentAv
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class GodkjenningsSubsumsjonTest {
    private lateinit var godkjenningsSubsumsjon: Subsumsjon
    private lateinit var faktum: Faktum<Boolean>
    private lateinit var godkjenning: Faktum<Boolean>

    @Test
    fun `Godkjenning uansett resultat av child`() {
        søknadprosess { fakta -> fakta ja 1 er true godkjentAv (fakta ja 2) }

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
    fun `Godkjenning av gyldig sti av child`() {
        søknadprosess { fakta -> fakta ja 1 er true gyldigGodkjentAv (fakta ja 2) }

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
    fun `Godkjenning av ugyldig sti av child`() {
        søknadprosess { fakta -> fakta ja 1 er true ugyldigGodkjentAv (fakta ja 2) }

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

    private fun søknadprosess(block: (Søknad) -> Subsumsjon): Søknadprosess {
        val søknad = Søknad(
            ja nei "faktum" id 1,
            ja nei "godkjenning" id 2 avhengerAv 1
        )

        faktum = søknad ja 1
        godkjenning = søknad ja 2

        godkjenningsSubsumsjon = block(søknad)
        return Søknadprosess(
            søknad,
            Seksjon("seksjon", Rolle.søker, faktum),
            Seksjon("seksjon", Rolle.saksbehandler, godkjenning),
            rootSubsumsjon = godkjenningsSubsumsjon
        )
    }
}

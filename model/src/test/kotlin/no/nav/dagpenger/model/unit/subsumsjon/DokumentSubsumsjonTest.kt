package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.regel.av
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

internal class DokumentSubsumsjonTest {
    private lateinit var dokumentFaktum: Faktum<Dokument>
    private lateinit var dokumentGodkjenning: Faktum<Boolean>
    private lateinit var subsumsjon: Subsumsjon

    @BeforeEach
    fun setUp() {
        dokumentFaktum = FaktumNavn(1, "dokument").faktum()
        dokumentGodkjenning = FaktumNavn(2, "saksbehandler godkjenner").faktum()
        dokumentGodkjenning avhengerAv dokumentFaktum
        subsumsjon = dokumentGodkjenning av dokumentFaktum
        assertEquals(null, subsumsjon.resultat())
        dokumentFaktum.besvar(Dokument(1.januar))
    }

    @Test
    fun `Subsumsjon blir true selv om ikke saksbehandler har godkjent`() {
        assertEquals(true, subsumsjon.resultat())
    }

    @Test
    fun `Subsumsjon blir false når saksbehandler har avslått`() {
        dokumentGodkjenning.besvar(false)
        assertEquals(false, subsumsjon.resultat())
    }

    @Test
    fun `Subsumsjon blir true når saksbehandler har godkjent`() {
        dokumentGodkjenning.besvar(true)
        assertEquals(true, subsumsjon.resultat())
    }

    @Test
    fun `Endre et faktum tilbakestiller svaret på det avhengige faktumet`() {
        assertEquals(true, subsumsjon.resultat())
        dokumentGodkjenning.besvar(false)
        assertEquals(false, subsumsjon.resultat())

        dokumentFaktum.besvar(Dokument(2.januar))
        assertFalse(dokumentGodkjenning.erBesvart())
        assertEquals(true, subsumsjon.resultat())

        dokumentGodkjenning.besvar(false)
        assertEquals(false, subsumsjon.resultat())
    }
}

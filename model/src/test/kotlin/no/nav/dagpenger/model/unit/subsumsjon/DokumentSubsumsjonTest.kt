package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.regel.av
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class DokumentSubsumsjonTest {
    private lateinit var dokumentFaktum: Faktum<Dokument>
    private lateinit var dokumentGodkjenning: Faktum<Boolean>
    private lateinit var subsumsjon: Subsumsjon

    @BeforeEach
    fun setUp() {
        dokumentFaktum = "dokument".faktum()
        dokumentGodkjenning = "saksbehandler godkjenner".faktum()
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
}

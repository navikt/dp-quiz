package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.testSøknad
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
        val faktagrupper = Fakta(
            dokument faktum "dokument" id 1,
            ja nei "saksbehandler godkjenner" id 2 avhengerAv 1
        ).testSøknad()

        dokumentFaktum = faktagrupper dokument 1
        dokumentGodkjenning = faktagrupper ja 2
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

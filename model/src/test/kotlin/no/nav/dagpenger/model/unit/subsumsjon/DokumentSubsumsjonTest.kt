package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.regel.dokumenteresAv
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class DokumentSubsumsjonTest {
    private lateinit var dokumentFaktum: Faktum<Dokument>
    private lateinit var dokumentGodkjenning: Faktum<Boolean>
    private lateinit var subsumsjon: Subsumsjon

    @BeforeEach
    fun setUp() {
        val søknadprosess =
            Fakta(
                testversjon,
                dokument faktum "dokument" id 1,
                boolsk faktum "saksbehandler godkjenner" id 2 avhengerAv 1,
            ).testSøknadprosess()

        dokumentFaktum = søknadprosess dokument 1
        dokumentGodkjenning = søknadprosess boolsk 2
        subsumsjon = dokumentGodkjenning dokumenteresAv dokumentFaktum
        assertEquals(null, subsumsjon.resultat())
        dokumentFaktum.besvar(Dokument(1.januar, "urn:sid:sse"))
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

        dokumentFaktum.besvar(Dokument(2.januar, "urn:sid:sse"))
        assertFalse(dokumentGodkjenning.erBesvart())
        assertEquals(true, subsumsjon.resultat())

        dokumentGodkjenning.besvar(false)
        assertEquals(false, subsumsjon.resultat())
    }
}

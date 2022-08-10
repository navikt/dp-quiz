package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.regel.er

import no.nav.dagpenger.model.regel.sannsynliggjøresAv
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals

class DokumentasjonskravSubsumsjonTest {

    private val søknad = Søknad(
        testversjon,
        boolsk faktum "faktum" id 1,
        dokument faktum "dokument" id 2,
        boolsk faktum "godkjenning" id 3 avhengerAv 2
    )

    private val faktum = søknad boolsk 1
    private val dokumentet = søknad dokument 2
    private val godkjenning = søknad boolsk 3

    @Test
    fun `noe`() {
        val subsumsjon = "må svare ja hvis ikke må en dokumentere neiet".minstEnAv(
            (faktum er false).sannsynliggjøresAv(dokumentet).godkjentAv(godkjenning),
            faktum er true
        )

        assertEquals(null, subsumsjon.resultat())
        faktum.besvar(true)
        assertEquals(true, subsumsjon.resultat())

        faktum.besvar(false)
        assertEquals(true, subsumsjon.resultat())
        dokumentet.besvar(Dokument(LocalDateTime.now(), "urn:sid:1"))
        assertEquals(true, subsumsjon.resultat())
        godkjenning.besvar(false)
        assertEquals(false, subsumsjon.resultat())

        dokumentet.besvar(Dokument(LocalDateTime.now(), "urn:sid:2"))
        assertEquals(true, subsumsjon.resultat())

        godkjenning.besvar(true)
        assertEquals(true, subsumsjon.resultat())

        godkjenning.besvar(false)
        assertEquals(false, subsumsjon.resultat())
    }
}

package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.med
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.godkjentAv
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.sannsynliggjøresAv
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SannsynliggjøringsSubsumsjonTest {
    private val søknad = Søknad(
        testversjon,
        boolsk faktum "faktum" id 1,
        dokument faktum "dokument" id 2,
        boolsk faktum "godkjenning" id 3 avhengerAv 2,
        heltall faktum "generator" id 4 genererer 5 og 6 og 7,
        boolsk faktum "generert-boolsk1" id 5,
        boolsk faktum "generert-boolsk2" id 6,
        dokument faktum "dokument for generator" id 7,
        boolsk faktum "godkjenning for generator" id 8 avhengerAv 7
    )
    private val faktum = søknad boolsk 1
    private val dokumentet = søknad dokument 2
    private val godkjenning = søknad boolsk 3
    private val generator = søknad generator 4
    private val generatorB1 = søknad boolsk 5
    private val generatorB2 = søknad boolsk 6
    private val generatorDokument = søknad dokument 7
    private val generatorGodkjenning = søknad boolsk 8

    @Test
    fun `Skal lage sannsynliggjøring for en subsumsjon som kan dokumenteres og skal godkjennes `() {
        val subsumsjon = "må svare ja hvis ikke må en dokumentere neiet".minstEnAv(
            (faktum er false).sannsynliggjøresAv(dokumentet).godkjentAv(godkjenning),
            faktum er true
        )

        assertEquals(setOf(faktum as GrunnleggendeFaktum), subsumsjon.nesteFakta())

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

        faktum.besvar(true)
        assertEquals(true, subsumsjon.resultat())
        assertFalse(dokumentet.erBesvart())
        assertFalse(godkjenning.erBesvart())
    }

    @Test
    fun `Skal lage sannsynliggjøring for en subsumsjon med generator som kan dokumenteres og skal godkjennes `() {
        val subsumsjon = generator.med(
            "deltre".deltre {
                "generator".alle(generatorB1 er true, generatorB2 er true).sannsynliggjøresAv(generatorDokument)
            }
        ).godkjentAv(generatorGodkjenning)
        val søknadprosess = søknad.testSøknadprosess(subsumsjon) {
            listOf(
                søknad.seksjon("bruker", Rolle.søker, 4, 5, 6),
                søknad.seksjon("bruker", Rolle.saksbehandler, 7, 8)
            )
        }

        with(søknadprosess) {
            generator(4).besvar(2)
            boolsk("5.1").besvar(true)
            boolsk("6.1").besvar(true)
        }
        assertEquals(null, søknadprosess.resultat())

        with(søknadprosess) {
            boolsk("5.2").besvar(true)
            boolsk("6.2").besvar(true)
        }
        assertEquals(true, søknadprosess.resultat())

        with(søknadprosess) {
            boolsk("8").besvar(false)
        }
        assertEquals(false, søknadprosess.resultat())

        with(søknadprosess) {
            boolsk("8").besvar(true)
            boolsk("6.2").besvar(false)
        }
        assertEquals(false, søknadprosess.resultat())

        with(søknadprosess) {
            dokument("7.1").besvar(Dokument(LocalDateTime.now(), "urn:sid:1"))
            dokument("7.2").besvar(Dokument(LocalDateTime.now(), "urn:sid:2"))
        }
        assertEquals(false, søknadprosess.resultat())

        with(søknadprosess) {
            boolsk("8").besvar(true)
            boolsk("6.2").besvar(true)
        }
        assertEquals(true, søknadprosess.resultat())
        with(søknadprosess) {
            assertTrue(dokument("7.1").erBesvart())
            assertFalse(dokument("7.2").erBesvart())
        }
    }
}

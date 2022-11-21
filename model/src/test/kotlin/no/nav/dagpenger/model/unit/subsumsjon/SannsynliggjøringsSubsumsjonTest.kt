package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.GyldigeValg
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.med
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.godkjentAv
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.sannsynliggjøresAv
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class SannsynliggjøringsSubsumsjonTest {
    private val søknad = Søknad(
        testversjon,
        boolsk faktum "faktum" id 1,
        dokument faktum "dokument" id 2 avhengerAv 1,
        dokument faktum "dokument2" id 21 avhengerAv 1,
        boolsk faktum "godkjenning" id 3 avhengerAv 2 og 21,
        heltall faktum "generator" id 4 genererer 5 og 6 og 7,
        boolsk faktum "generert-boolsk1" id 5,
        boolsk faktum "generert-boolsk2" id 6,
        dokument faktum "dokument for generator" id 7 avhengerAv 5 og 6,
        boolsk faktum "godkjenning for generator" id 8 avhengerAv 7
    )
    private val faktum = søknad boolsk 1
    private val dokument1 = søknad dokument 2
    private val dokument2 = søknad dokument 21
    private val godkjenning = søknad boolsk 3
    private val generator = søknad generator 4
    private val generatorB1 = søknad boolsk 5
    private val generatorB2 = søknad boolsk 6
    private val generatorDokument = søknad dokument 7
    private val generatorGodkjenning = søknad boolsk 8

    @Test
    fun `Skal lage sannsynliggjøring for en subsumsjon som kan dokumenteres og skal godkjennes `() {
        val subsumsjon = "må svare ja hvis ikke må en dokumentere neiet".minstEnAv(
            (faktum er false).sannsynliggjøresAv(dokument1, dokument2).godkjentAv(godkjenning),
            faktum er true
        )

        assertEquals(setOf(faktum as GrunnleggendeFaktum), subsumsjon.nesteFakta())

        faktum.besvar(false)

        assertEquals(true, subsumsjon.resultat())

        dokument1.besvar(Dokument(LocalDateTime.now(), "urn:sid:1"))
        dokument2.besvar(Dokument(LocalDateTime.now(), "urn:sid:3"))

        assertEquals(true, subsumsjon.resultat())

        godkjenning.besvar(false)

        assertEquals(false, subsumsjon.resultat())

        dokument1.besvar(Dokument(LocalDateTime.now(), "urn:sid:2"))
        dokument2.besvar(Dokument(LocalDateTime.now(), "urn:sid:4"))

        assertEquals(true, subsumsjon.resultat())

        godkjenning.besvar(true)
        assertEquals(true, subsumsjon.resultat())

        godkjenning.besvar(false)
        assertEquals(false, subsumsjon.resultat())

        faktum.besvar(true)
        assertEquals(true, subsumsjon.resultat())
        assertFalse(dokument1.erBesvart())
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

    @Test
    fun `Lager bare avhengigheter til fakta som sannsynligjøres en gang`() {
        val subsumsjon = generator.med(
            "deltre".deltre {
                "generator".alle(generatorB1 er true, generatorB2 er true).sannsynliggjøresAv(generatorDokument)
            }
        ).godkjentAv(generatorGodkjenning)
        val prosess = søknad.testSøknadprosess(subsumsjon)

        repeat(10) { søknad.testSøknadprosess(subsumsjon) }

        assertEquals(2, DuplikatVisitor(prosess).avhengigheter)
    }

    class DuplikatVisitor(søknad: Søknadprosess) : SøknadprosessVisitor {
        var avhengigheter = 0

        init {
            søknad.accept(this)
        }

        override fun <R : Comparable<R>> visit(
            faktum: TemplateFaktum<R>,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFakta: Set<Faktum<*>>,
            roller: Set<Rolle>,
            clazz: Class<R>,
            gyldigeValg: GyldigeValg?
        ) {
            if (id != "7") return
            avhengigheter = avhengerAvFakta.size
        }
    }
}

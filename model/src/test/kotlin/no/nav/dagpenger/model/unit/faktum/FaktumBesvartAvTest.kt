package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.GyldigeValg
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.visitor.SøknadVisitor
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class FaktumBesvartAvTest {

    @Test
    fun `Sjekk at ident blir lagt på når saksbehandler besvarer`() {
        val søknad = Søknad(
            testversjon,
            boolsk faktum "f1" id 1,
        ).testSøknadprosess()
        val ja1 = søknad.boolsk(1)

        ja1.besvar(true, "A123456")
        assertEquals("A123456", BesvartAvVisitor(søknad).identer.first())
    }

    private class BesvartAvVisitor(søknadprosess: Søknadprosess) : SøknadVisitor {

        val identer = mutableListOf<String>()
        init {
            søknadprosess.søknad.accept(this)
        }

        override fun <R : Comparable<R>> visitMedSvar(
            faktum: GrunnleggendeFaktum<R>,
            tilstand: Faktum.FaktumTilstand,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            avhengerAvFakta: Set<Faktum<*>>,
            godkjenner: Set<Faktum<*>>,
            roller: Set<Rolle>,
            clazz: Class<R>,
            svar: R,
            besvartAv: String?,
            gyldigeValg: GyldigeValg?
        ) {
            besvartAv?.let { identer.add(it) }
        }
    }
}

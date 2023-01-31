package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.GyldigeValg
import no.nav.dagpenger.model.faktum.LandGrupper
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.visitor.SøknadVisitor
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class FaktumBesvartAvTest {

    @Test
    fun `Sjekk at ident blir lagt på når saksbehandler besvarer`() {
        val fakta = Fakta(
            testversjon,
            boolsk faktum "f1" id 1,
        ).testSøknadprosess()
        val ja1 = fakta.boolsk(1)

        ja1.besvar(true, "A123456")
        assertEquals("A123456", BesvartAvVisitor(fakta).identer.first())
    }

    private class BesvartAvVisitor(utredningsprosess: Utredningsprosess) : SøknadVisitor {

        val identer = mutableListOf<String>()
        init {
            utredningsprosess.fakta.accept(this)
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
            gyldigeValg: GyldigeValg?,
            landGrupper: LandGrupper?
        ) {
            besvartAv?.let { identer.add(it) }
        }
    }
}

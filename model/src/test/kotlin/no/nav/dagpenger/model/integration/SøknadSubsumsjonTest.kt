package no.nav.dagpenger.model.integration

import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Inntekt.Companion.månedlig
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.helpers.Eksempel
import no.nav.dagpenger.model.helpers.desember
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.visitor.SøknadVisitor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SøknadSubsumsjonTest {

    private lateinit var m: Eksempel
    private lateinit var søknad: Søknad
    private lateinit var rootSubsumsjon: Subsumsjon

    @BeforeEach
    fun setUp() {
        m = Eksempel()
        søknad = m.søknad
        rootSubsumsjon = m.rootSubsumsjon
    }

    @Test
    fun `Søknad subsumsjon integrasjonstest`() {
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(5, fakta.size)
            assertEquals(setOf(m.f1Boolean, m.f2Dato, m.f3Dato, m.f4Dato, m.f5Dato), fakta)
        }

        assertEquals(m.seksjon1, søknad.nesteSeksjon(rootSubsumsjon))
        assertEquals(2, m.seksjon1.fakta().size)
        assertEquals(setOf(m.f1Boolean, m.f2Dato), m.seksjon1.fakta())
        m.f1Boolean.besvar(true, Rolle.nav)
        m.f2Dato.besvar(31.desember, Rolle.nav)
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(3, fakta.size)
            assertEquals(setOf(m.f3Dato, m.f4Dato, m.f5Dato), fakta)
        }

        assertEquals(m.seksjon3, søknad.nesteSeksjon(rootSubsumsjon))
        assertEquals(5, m.seksjon3.fakta().size)
        assertEquals(setOf(m.f3Dato, m.f4Dato, m.f5Dato, m.f_3_4_5Dato, m.f13Dato), m.seksjon3.fakta())
        m.f3Dato.besvar(1.januar)
        m.f4Dato.besvar(2.januar)
        m.f5Dato.besvar(3.januar)
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(setOf(m.f10Boolean), fakta)
        }

        assertEquals(m.seksjon4, søknad.nesteSeksjon(rootSubsumsjon))
        assertEquals(2, m.seksjon4.fakta().size)
        assertEquals(setOf(m.f10Boolean, m.f11Dokument), m.seksjon4.fakta())
        m.f10Boolean.besvar(false)
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(setOf(m.f11Dokument), fakta)
        }

        assertEquals(m.seksjon4, søknad.nesteSeksjon(rootSubsumsjon))
        assertEquals(2, m.seksjon4.fakta().size)
        assertEquals(setOf(m.f10Boolean, m.f11Dokument), m.seksjon4.fakta())
        m.f11Dokument.besvar(Dokument(4.januar))
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(4, fakta.size)
            assertEquals(setOf(m.f6Inntekt, m.f7Inntekt, m.f8Inntekt, m.f9Inntekt), fakta)
        }

        assertEquals(m.seksjon2, søknad.nesteSeksjon(rootSubsumsjon))
        assertEquals(4, m.seksjon2.fakta().size)
        assertEquals(setOf(m.f6Inntekt, m.f7Inntekt, m.f8Inntekt, m.f9Inntekt), m.seksjon2.fakta())
        m.f6Inntekt.besvar(20000.månedlig, Rolle.nav)
        m.f7Inntekt.besvar(10000.månedlig, Rolle.nav)
        m.f8Inntekt.besvar(5000.månedlig, Rolle.nav)
        m.f9Inntekt.besvar(2500.månedlig, Rolle.nav)
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(setOf(m.f14Boolean), fakta)
        }

        assertEquals(m.seksjon5, søknad.nesteSeksjon(rootSubsumsjon))
        assertEquals(4, m.seksjon5.fakta().size)
        assertEquals(setOf(m.f6Inntekt, m.f7Inntekt, m.f12Boolean, m.f14Boolean), m.seksjon5.fakta())
        m.f14Boolean.besvar(true, Rolle.saksbehandler)
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(0, fakta.size)
            assertEquals(emptySet<GrunnleggendeFaktum<*>>(), fakta)
        }
    }

    private fun Seksjon.fakta(): Set<Faktum<*>> =
        object : SøknadVisitor {
            lateinit var resultater: Set<Faktum<*>>
            override fun preVisit(seksjon: Seksjon, rolle: Rolle, fakta: Set<Faktum<*>>) {
                resultater = fakta
            }
        }.let {
            this@fakta.accept(it)
            it.resultater
        }
}

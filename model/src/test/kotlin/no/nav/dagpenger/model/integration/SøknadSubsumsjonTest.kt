package no.nav.dagpenger.model.integration

import no.nav.dagpenger.model.db.SøknadBuilder
import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumId
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Inntekt.Companion.månedlig
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.TemplateFaktum
import no.nav.dagpenger.model.helpers.Eksempel
import no.nav.dagpenger.model.helpers.desember
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.visitor.SøknadJsonBuilder
import no.nav.dagpenger.model.visitor.SøknadVisitor
import no.nav.helse.serde.assertDeepEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertNotNull

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
    fun `Generere faktum fra template dersom det ikke finnes`() {
        (1..18).forEach {
            assertNotNull(søknad.fakta[FaktumId(it)])
        }

        assertNull(søknad.fakta[FaktumId(16).indeks(1)])
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

        assertEquals(m.seksjon4, søknad.nesteSeksjon(rootSubsumsjon))
        assertEquals(5, m.seksjon4.fakta().size)
        assertEquals(setOf(m.f3Dato, m.f4Dato, m.f5Dato, m.f_3_4_5Dato, m.f13Dato), m.seksjon4.fakta())
        m.f3Dato.besvar(1.januar)
        m.f4Dato.besvar(2.januar)
        m.f5Dato.besvar(3.januar)
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(setOf(m.f10Boolean), fakta)
        }

        assertEquals(m.seksjon5, søknad.nesteSeksjon(rootSubsumsjon))
        assertEquals(2, m.seksjon5.fakta().size)
        assertEquals(setOf(m.f10Boolean, m.f11Dokument), m.seksjon5.fakta())
        m.f10Boolean.besvar(false)
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(setOf(m.f11Dokument), fakta)
        }

        assertEquals(m.seksjon5, søknad.nesteSeksjon(rootSubsumsjon))
        assertEquals(2, m.seksjon5.fakta().size)
        assertEquals(setOf(m.f10Boolean, m.f11Dokument), m.seksjon5.fakta())
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
            assertEquals(setOf(m.f15Int), fakta)
        }

        assertEquals(m.seksjon3, søknad.nesteSeksjon(rootSubsumsjon))
        assertEquals(1, m.seksjon3.fakta().size)
        assertEquals(setOf(m.f15Int), m.seksjon3.fakta())
        m.f15Int.besvar(2, Rolle.nav)
        assertEquals(3, m.seksjon3.fakta().size) // Genererte 2 til
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(2, fakta.size) // Feltene i første genererte subsumsjon
            assertEquals(listOf("16.1", "16.2"), fakta.map { it.id })
        }

        assertEquals(m.seksjon3, søknad.nesteSeksjon(rootSubsumsjon))
        assertEquals(3, m.seksjon3.fakta().size)
        assertEquals(listOf("15", "16.1", "16.2"), m.seksjon3.fakta().map { it.id })
        (m.seksjon3.first { it.id == "16.1" } as Faktum<Int>).besvar(17, Rolle.nav)
        (m.seksjon3.first { it.id == "16.2" } as Faktum<Int>).besvar(19, Rolle.nav)
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(listOf("17.1"), fakta.map { it.id })
        }

        assertEquals(m.søknad[7], søknad.nesteSeksjon(rootSubsumsjon))
        assertEquals(2, m.søknad[7].fakta().size)
        assertEquals(listOf("16.1", "17.1"), m.søknad[7].fakta().map { it.id })
        (m.søknad[7].first { it.id == "17.1" } as Faktum<Boolean>).besvar(true)
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(setOf(m.f14Boolean), fakta)
        }

        assertEquals(m.seksjon8, søknad.nesteSeksjon(rootSubsumsjon))
        assertEquals(7, m.seksjon8.fakta().size)
        assertEquals(listOf("6", "7", "12", "14", "18.1", "18.2", "19").sorted(), m.seksjon8.fakta().map { it.id }.sorted())
        m.f14Boolean.besvar(true, Rolle.saksbehandler)
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(0, fakta.size)
            assertEquals(emptySet<GrunnleggendeFaktum<*>>(), fakta)
        }
    }

    @Test
    fun `Avvisning av søker faktum`() {
        m.f1Boolean.besvar(true, Rolle.nav)
        m.f2Dato.besvar(31.desember, Rolle.nav)
        m.f3Dato.besvar(1.januar)
        m.f4Dato.besvar(2.januar)
        m.f5Dato.besvar(3.januar)

        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(setOf(m.f10Boolean), fakta)
        }

        m.f10Boolean.besvar(false)
        m.f11Dokument.besvar(Dokument(1.januar))
        m.f12Boolean.besvar(false, Rolle.saksbehandler)

        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(setOf(m.f13Dato), fakta)
        }

        m.f13Dato.besvar(1.februar)
        assertEquals(true, rootSubsumsjon.resultat())

        m.f19Boolean.besvar(false, Rolle.saksbehandler)
        assertEquals(false, rootSubsumsjon.resultat())

        assertMarshalling(m.søknad)
    }

    private fun assertMarshalling(original: Søknad) {
        val jsonString = SøknadJsonBuilder(original).toString()
        val clone = SøknadBuilder(jsonString).resultat()
        assertDeepEquals(original, clone)
    }

    private fun Seksjon.fakta(): Set<Faktum<*>> =
        object : SøknadVisitor {
            lateinit var resultater: Set<Faktum<*>>

            override fun postVisit(søknad: Søknad) {
                rootSubsumsjon.resultat()
            }

            override fun preVisit(seksjon: Seksjon, rolle: Rolle, fakta: Set<Faktum<*>>) {
                resultater = fakta.filterNot { it is TemplateFaktum<*> }.toSet()
            }
        }.let {
            this@fakta.accept(it)
            it.resultater
        }
}

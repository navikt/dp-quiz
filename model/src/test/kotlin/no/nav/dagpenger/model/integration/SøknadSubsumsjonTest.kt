package no.nav.dagpenger.model.integration

import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Inntekt.Companion.månedlig
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.TemplateFaktum
import no.nav.dagpenger.model.helpers.NyEksempel
import no.nav.dagpenger.model.helpers.desember
import no.nav.dagpenger.model.helpers.februar
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.søknad.Faktagrupper
import no.nav.dagpenger.model.søknad.Seksjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SøknadSubsumsjonTest {

    private lateinit var faktagrupper: Faktagrupper

    @BeforeEach
    fun setUp() {
        faktagrupper = NyEksempel().faktagrupper
    }

    @Test
    fun `Søknad subsumsjon integrasjonstest`() {
        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(5, fakta.size)
            assertIder(fakta, 1, 2, 3, 4, 5)
        }

        assertEquals(faktagrupper[0], faktagrupper.nesteSeksjon())
        assertEquals(2, faktagrupper[0].fakta().size)
        assertIder(faktagrupper[0].fakta(), 1, 2)

        faktagrupper.ja(1).besvar(true, Rolle.nav)
        faktagrupper.dato(2).besvar(31.desember, Rolle.nav)
        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(3, fakta.size)
            assertIder(fakta, 3, 4, 5)
        }

        assertEquals(faktagrupper[3], faktagrupper.nesteSeksjon())
        assertEquals(5, faktagrupper[3].fakta().size)

        assertIder(faktagrupper[3].fakta(), 3, 4, 5, 345, 13)
        faktagrupper.dato(3).besvar(1.januar)
        faktagrupper.dato(4).besvar(2.januar)
        faktagrupper.dato(5).besvar(3.januar)
        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 10)
        }

        assertEquals(faktagrupper[4], faktagrupper.nesteSeksjon())
        assertEquals(2, faktagrupper[4].fakta().size)
        assertIder(faktagrupper[4].fakta(), 10, 11)
        faktagrupper.ja(10).besvar(false)
        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 11)
        }

        assertEquals(faktagrupper[4], faktagrupper.nesteSeksjon())
        assertEquals(2, faktagrupper[4].fakta().size)
        assertIder(faktagrupper[4].fakta(), 10, 11)
        faktagrupper.dokument(11).besvar(Dokument(4.januar))

        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(4, fakta.size)
            assertIder(fakta, 6, 8, 7, 9)
        }

        assertEquals(faktagrupper[1], faktagrupper.nesteSeksjon())
        assertEquals(4, faktagrupper[1].fakta().size)
        assertIder(faktagrupper[1].fakta(), 6, 7, 8, 9)

        faktagrupper.inntekt(6).besvar(20000.månedlig, Rolle.nav)
        faktagrupper.inntekt(7).besvar(10000.månedlig, Rolle.nav)
        faktagrupper.inntekt(8).besvar(5000.månedlig, Rolle.nav)
        faktagrupper.inntekt(9).besvar(2500.månedlig, Rolle.nav)
        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 15)
        }

        assertEquals(faktagrupper[2], faktagrupper.nesteSeksjon())
        assertEquals(1, faktagrupper[2].fakta().size)
        assertIder(faktagrupper[2].fakta(), 15)
        faktagrupper.generator(15).besvar(2, Rolle.nav)
        assertEquals(3, faktagrupper[2].fakta().size) // Genererte 2 til
        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(2, fakta.size) // Feltene i første genererte subsumsjon
            assertEquals(listOf("16.1", "16.2"), fakta.map { it.id })
        }

        assertEquals(faktagrupper[2], faktagrupper.nesteSeksjon())
        assertEquals(3, faktagrupper[2].fakta().size)
        assertEquals(listOf("15", "16.1", "16.2"), faktagrupper[2].fakta().map { it.id })
        (faktagrupper[2].first { it.id == "16.1" } as Faktum<Int>).besvar(17, Rolle.nav)
        (faktagrupper[2].first { it.id == "16.2" } as Faktum<Int>).besvar(19, Rolle.nav)
        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(listOf("17.1"), fakta.map { it.id })
        }

        assertEquals(faktagrupper[7], faktagrupper.nesteSeksjon())
        assertEquals(2, faktagrupper[7].fakta().size)
        assertEquals(listOf("16.1", "17.1"), faktagrupper[7].fakta().map { it.id })
        (faktagrupper[7].first { it.id == "17.1" } as Faktum<Boolean>).besvar(true)
        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertIder(fakta, 14)
        }

        assertEquals(faktagrupper[9], faktagrupper.nesteSeksjon())
        assertEquals(7, faktagrupper[9].fakta().size)
        assertEquals(listOf("6", "7", "12", "14", "18.1", "18.2", "19").sorted(), faktagrupper[9].fakta().map { it.id }.sorted())
        faktagrupper.ja(14).besvar(true, Rolle.saksbehandler)
        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(0, fakta.size)
            assertEquals(emptySet<GrunnleggendeFaktum<*>>(), fakta)
        }
    }

    @Test
    fun `Avvisning av søker faktum`() {
        faktagrupper.ja(1).besvar(true, Rolle.nav)
        faktagrupper.dato(2).besvar(31.desember, Rolle.nav)
        faktagrupper.dato(3).besvar(1.januar)
        faktagrupper.dato(4).besvar(2.januar)
        faktagrupper.dato(5).besvar(3.januar)

        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(setOf(faktagrupper.ja(10)), fakta)
        }

        faktagrupper.ja(10).besvar(false)
        faktagrupper.dokument(11).besvar(Dokument(1.januar))
        faktagrupper.ja(12).besvar(false, Rolle.saksbehandler)

        faktagrupper.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(setOf(faktagrupper.dato(13)), fakta)
        }

        faktagrupper.dato(13).besvar(1.februar)
        assertEquals(true, faktagrupper.resultat())

        faktagrupper.ja(19).besvar(false, Rolle.saksbehandler)
        assertEquals(false, faktagrupper.resultat())
    }

    private fun assertIder(fakta: Set<Faktum<*>>, vararg ider: Int) {
        assertIder(fakta, *(ider.map { it.toString() }.toTypedArray()))
    }

    private fun assertIder(fakta: Set<Faktum<*>>, vararg ider: String) {
        assertEquals(ider.toList(), fakta.map { it.id })
    }

    private fun Seksjon.fakta() = this.filter { it !is TemplateFaktum }.toSet()
}

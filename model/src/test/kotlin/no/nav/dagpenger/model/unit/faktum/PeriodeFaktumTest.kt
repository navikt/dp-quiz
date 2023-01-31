package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.periode
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.helpers.testversjon
import no.nav.dagpenger.model.seksjon.Faktagrupper
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.LocalDate
import kotlin.test.assertTrue

class PeriodeFaktumTest {

    val prototypeFakta = Fakta(
        testversjon,
        periode faktum "periode" id 1
    )

    lateinit var søknad: Faktagrupper

    @BeforeEach
    fun setup() {
        søknad = prototypeFakta.testSøknadprosess(TomSubsumsjon)
    }

    @Test
    fun `Skal kunne besvare et periodefaktum`() {
        val førstePeriode = Periode(
            LocalDate.now().minusDays(90),
            LocalDate.now()
        )

        val periodeFaktum = søknad.periode(1)

        assertDoesNotThrow { periodeFaktum.besvar(førstePeriode) }
        assertTrue(periodeFaktum.erBesvart())

        val andrePeriode = Periode(
            LocalDate.now().minusDays(260),
            LocalDate.now().minusDays(50)
        )
        assertDoesNotThrow { periodeFaktum.besvar(andrePeriode) }
        assertTrue(periodeFaktum.erBesvart())
    }
}

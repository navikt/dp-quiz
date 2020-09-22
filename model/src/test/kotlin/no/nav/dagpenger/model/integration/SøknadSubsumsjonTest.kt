package no.nav.dagpenger.model.integration

import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.Inntekt.Companion.månedlig
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.helpers.desember
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.regel.MAKS_DATO
import no.nav.dagpenger.model.regel.av
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.etter
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.eller
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.så
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class SøknadSubsumsjonTest {

    private lateinit var f1Boolean: Faktum<Boolean>
    private lateinit var f2Dato: Faktum<LocalDate>
    private lateinit var f3Dato: Faktum<LocalDate>
    private lateinit var f4Dato: Faktum<LocalDate>
    private lateinit var f5Dato: Faktum<LocalDate>
    private lateinit var f_3_4_5Dato: Faktum<LocalDate>
    private lateinit var f6Inntekt: Faktum<Inntekt>
    private lateinit var f7Inntekt: Faktum<Inntekt>
    private lateinit var f8Inntekt: Faktum<Inntekt>
    private lateinit var f9Inntekt: Faktum<Inntekt>
    private lateinit var f10Boolean: Faktum<Boolean>
    private lateinit var f11Dokument: Faktum<Dokument>
    private lateinit var f12Boolean: Faktum<Boolean>
    private lateinit var f13Boolean: Faktum<Boolean>

    private lateinit var seksjon1: Seksjon
    private lateinit var seksjon2: Seksjon
    private lateinit var seksjon3: Seksjon
    private lateinit var seksjon4: Seksjon
    private lateinit var seksjon5: Seksjon

    private lateinit var søknad: Søknad

    private lateinit var rootSubsumsjon: Subsumsjon

    @BeforeEach
    fun setUp() {
        f1Boolean = FaktumNavn(1, "f1").faktum()
        f2Dato = FaktumNavn(2, "f2").faktum()
        f3Dato = FaktumNavn(3, "f3").faktum()
        f4Dato = FaktumNavn(4, "f4").faktum()
        f5Dato = FaktumNavn(5, "f5").faktum()
        f_3_4_5Dato = listOf(f3Dato, f4Dato, f5Dato).faktum(FaktumNavn(345, "345"), MAKS_DATO)
        f6Inntekt = FaktumNavn(6, "f6").faktum()
        f7Inntekt = FaktumNavn(7, "f7").faktum()
        f8Inntekt = FaktumNavn(8, "f8").faktum()
        f9Inntekt = FaktumNavn(9, "f9").faktum()
        f10Boolean = FaktumNavn(10, "f10").faktum()
        f11Dokument = FaktumNavn(11, "f11").faktum()
        f12Boolean = FaktumNavn(12, "f12").faktum()
        f12Boolean avhengerAv f11Dokument
        f13Boolean = FaktumNavn(13, "f13").faktum()


        seksjon1 = Seksjon(Rolle.nav, f1Boolean, f2Dato)
        seksjon2 = Seksjon(Rolle.nav, f6Inntekt, f7Inntekt, f8Inntekt, f9Inntekt)
        seksjon3 = Seksjon(Rolle.søker, f3Dato, f4Dato, f5Dato, f_3_4_5Dato)
        seksjon4 = Seksjon(Rolle.søker, f10Boolean, f11Dokument)
        seksjon5 = Seksjon(Rolle.saksbehandler, f6Inntekt, f7Inntekt, f12Boolean, f13Boolean)

        søknad = Søknad(seksjon1, seksjon2, seksjon3, seksjon4, seksjon5)

        val minstEnSubsumsjon = "minstEnSubsumsjon".minstEnAv(
            f6Inntekt minst f8Inntekt,
            f7Inntekt minst f9Inntekt
        ) så (f13Boolean er true)

        rootSubsumsjon = "rootSubsumsjon".alle(
            f1Boolean er true,
            f2Dato etter f_3_4_5Dato,
            f3Dato før f4Dato
        ) så ((f10Boolean er true) så minstEnSubsumsjon eller ((f12Boolean av f11Dokument) så minstEnSubsumsjon))
    }

    @Test
    fun `Søknad subsumsjon integrasjonstest`() {
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(5, fakta.size)
            assertEquals(setOf(f1Boolean, f2Dato, f3Dato, f4Dato, f5Dato), fakta)
        }

        f1Boolean.besvar(true, Rolle.nav)
        f2Dato.besvar(31.desember, Rolle.nav)
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(3, fakta.size)
            assertEquals(setOf(f3Dato, f4Dato, f5Dato), fakta)
        }

        f3Dato.besvar(1.januar)
        f4Dato.besvar(2.januar)
        f5Dato.besvar(3.januar)
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(setOf(f10Boolean), fakta)
        }

        f10Boolean.besvar(false)
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(setOf(f11Dokument), fakta)
        }

        f11Dokument.besvar(Dokument(4.januar))

        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(4, fakta.size)
            assertEquals(setOf(f6Inntekt, f7Inntekt, f8Inntekt, f9Inntekt), fakta)
        }

        f6Inntekt.besvar(20000.månedlig, Rolle.nav)
        f7Inntekt.besvar(10000.månedlig, Rolle.nav)
        f8Inntekt.besvar(5000.månedlig, Rolle.nav)
        f9Inntekt.besvar(2500.månedlig, Rolle.nav)

        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(setOf(f13Boolean), fakta)
        }

        f13Boolean.besvar(true, Rolle.saksbehandler)
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(0, fakta.size)
            assertEquals(emptySet<GrunnleggendeFaktum<*>>(), fakta)
        }
    }
}

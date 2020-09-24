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
import no.nav.dagpenger.model.visitor.SøknadVisitor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class SøknadSubsumsjonTest {
    private val fn1Boolean = FaktumNavn(1, "f1")
    private val fn2Dato = FaktumNavn(2, "f2")
    private val fn3Dato = FaktumNavn(3, "f3")
    private val fn4Dato = FaktumNavn(4, "f4")
    private val fn5Dato = FaktumNavn(5, "f5")
    private val fn_3_4_5Dato = FaktumNavn(345, "345")
    private val fn6Inntekt = FaktumNavn(6, "f6")
    private val fn7Inntekt = FaktumNavn(7, "f7")
    private val fn8Inntekt = FaktumNavn(8, "f8")
    private val fn9Inntekt = FaktumNavn(9, "f9")
    private val fn10Boolean = FaktumNavn(10, "f10")
    private val fn11Dokument = FaktumNavn(11, "f11")
    private val fn12Boolean = FaktumNavn(12, "f12")
    private val fn13Boolean = FaktumNavn(13, "f13")

    private val t1Boolean = fn1Boolean.faktum(Boolean::class.java)
    private val t2Dato = fn2Dato.faktum(LocalDate::class.java)
    private val t3Dato = fn3Dato.faktum(LocalDate::class.java)
    private val t4Dato = fn4Dato.faktum(LocalDate::class.java)
    private val t5Dato = fn5Dato.faktum(LocalDate::class.java)
    private val t_3_4_5Dato = listOf(t3Dato, t4Dato, t5Dato).faktum(fn_3_4_5Dato, MAKS_DATO)
    private val t6Inntekt = fn6Inntekt.faktum(Inntekt::class.java)
    private val t7Inntekt = fn7Inntekt.faktum(Inntekt::class.java)
    private val t8Inntekt = fn8Inntekt.faktum(Inntekt::class.java)
    private val t9Inntekt = fn9Inntekt.faktum(Inntekt::class.java)
    private val t10Boolean = fn10Boolean.faktum(Boolean::class.java)
    private val t11Dokument = fn11Dokument.faktum(Dokument::class.java)
    private val t12Boolean = fn12Boolean.faktum(Boolean::class.java).apply {
        this avhengerAv t11Dokument
    }
    private val t13Boolean = fn13Boolean.faktum(Boolean::class.java)

    private val minstEnSubsumsjon = "minstEnSubsumsjon".minstEnAv(
        t6Inntekt minst t8Inntekt,
        t7Inntekt minst t9Inntekt
    ) så (t13Boolean er true)

    /* ktlint-disable parameter-list-wrapping */
    private val templateSubsumsjon = "rootSubsumsjon".alle(
        t1Boolean er true,
        t2Dato etter t_3_4_5Dato,
        t3Dato før t4Dato
    ) så (
        (t10Boolean er true)
            så minstEnSubsumsjon
            eller (
                (t12Boolean av t11Dokument) så minstEnSubsumsjon
                )
        )

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
        f1Boolean = fn1Boolean.faktum(Boolean::class.java)
        f2Dato = fn2Dato.faktum(LocalDate::class.java)
        f3Dato = fn3Dato.faktum(LocalDate::class.java)
        f4Dato = fn4Dato.faktum(LocalDate::class.java)
        f5Dato = fn5Dato.faktum(LocalDate::class.java)
        f6Inntekt = fn6Inntekt.faktum(Inntekt::class.java)
        f7Inntekt = fn7Inntekt.faktum(Inntekt::class.java)
        f8Inntekt = fn8Inntekt.faktum(Inntekt::class.java)
        f9Inntekt = fn9Inntekt.faktum(Inntekt::class.java)
        f10Boolean = fn10Boolean.faktum(Boolean::class.java)
        f11Dokument = fn11Dokument.faktum(Dokument::class.java)
        f12Boolean = fn12Boolean.faktum(Boolean::class.java)
        f13Boolean = fn13Boolean.faktum(Boolean::class.java)
        f_3_4_5Dato = listOf(f3Dato, f4Dato, f5Dato).faktum(fn_3_4_5Dato, MAKS_DATO)
        f12Boolean avhengerAv f11Dokument

        seksjon1 = Seksjon(Rolle.nav, f1Boolean, f2Dato)
        seksjon2 = Seksjon(Rolle.nav, f6Inntekt, f7Inntekt, f8Inntekt, f9Inntekt)
        seksjon3 = Seksjon(Rolle.søker, f3Dato, f4Dato, f5Dato, f_3_4_5Dato)
        seksjon4 = Seksjon(Rolle.søker, f10Boolean, f11Dokument)
        seksjon5 = Seksjon(Rolle.saksbehandler, f6Inntekt, f7Inntekt, f12Boolean, f13Boolean)

        søknad = Søknad(seksjon1, seksjon2, seksjon3, seksjon4, seksjon5)

        rootSubsumsjon = templateSubsumsjon.deepCopy(søknad.faktaMap())
    }

    @Test
    @Disabled
    fun `Søknad subsumsjon integrasjonstest`() {
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(5, fakta.size)
            assertEquals(setOf(f1Boolean, f2Dato, f3Dato, f4Dato, f5Dato), fakta)
        }

        assertEquals(seksjon1, søknad.nesteSeksjon(rootSubsumsjon))
        assertEquals(2, seksjon1.fakta().size)
        assertEquals(setOf(f1Boolean, f2Dato), seksjon1.fakta())
        f1Boolean.besvar(true, Rolle.nav)
        f2Dato.besvar(31.desember, Rolle.nav)
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(3, fakta.size)
            assertEquals(setOf(f3Dato, f4Dato, f5Dato), fakta)
        }

        assertEquals(seksjon3, søknad.nesteSeksjon(rootSubsumsjon))
        assertEquals(4, seksjon3.fakta().size)
        assertEquals(setOf(f3Dato, f4Dato, f5Dato, f_3_4_5Dato), seksjon3.fakta())
        f3Dato.besvar(1.januar)
        f4Dato.besvar(2.januar)
        f5Dato.besvar(3.januar)
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(setOf(f10Boolean), fakta)
        }

        assertEquals(seksjon4, søknad.nesteSeksjon(rootSubsumsjon))
        assertEquals(2, seksjon4.fakta().size)
        assertEquals(setOf(f10Boolean, f11Dokument), seksjon4.fakta())
        f10Boolean.besvar(false)
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(setOf(f11Dokument), fakta)
        }

        assertEquals(seksjon4, søknad.nesteSeksjon(rootSubsumsjon))
        assertEquals(2, seksjon4.fakta().size)
        assertEquals(setOf(f10Boolean, f11Dokument), seksjon4.fakta())
        f11Dokument.besvar(Dokument(4.januar))
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(4, fakta.size)
            assertEquals(setOf(f6Inntekt, f7Inntekt, f8Inntekt, f9Inntekt), fakta)
        }

        assertEquals(seksjon2, søknad.nesteSeksjon(rootSubsumsjon))
        assertEquals(4, seksjon2.fakta().size)
        assertEquals(setOf(f6Inntekt, f7Inntekt, f8Inntekt, f9Inntekt), seksjon2.fakta())
        f6Inntekt.besvar(20000.månedlig, Rolle.nav)
        f7Inntekt.besvar(10000.månedlig, Rolle.nav)
        f8Inntekt.besvar(5000.månedlig, Rolle.nav)
        f9Inntekt.besvar(2500.månedlig, Rolle.nav)
        rootSubsumsjon.nesteFakta().also { fakta ->
            assertEquals(1, fakta.size)
            assertEquals(setOf(f13Boolean), fakta)
        }

        assertEquals(seksjon5, søknad.nesteSeksjon(rootSubsumsjon))
        assertEquals(4, seksjon5.fakta().size)
        assertEquals(setOf(f6Inntekt, f7Inntekt, f12Boolean, f13Boolean), seksjon5.fakta())
        f13Boolean.besvar(true, Rolle.saksbehandler)
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

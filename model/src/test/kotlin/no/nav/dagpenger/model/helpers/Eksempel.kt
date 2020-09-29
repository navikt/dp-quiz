package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.fakta.Alder
import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.GeneratorFaktum
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.TemplateFaktum
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.fakta.template
import no.nav.dagpenger.model.regel.MAKS_DATO
import no.nav.dagpenger.model.regel.av
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.etter
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.regel.med
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.eller
import no.nav.dagpenger.model.subsumsjon.makro
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.så
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import java.time.LocalDate

internal class Eksempel {

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
    private val fn13Dato = FaktumNavn(13, "f13")
    private val fn14Boolean = FaktumNavn(14, "f14")
    private val fn15Int = FaktumNavn(15, "f15")
    private val fn16LocalDate = FaktumNavn(16, "f16")
    private val fn17Boolean = FaktumNavn(17, "f17")
    private val fn18Boolean = FaktumNavn(18, "f18")

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
    private val t13Dato = fn13Dato.faktum(LocalDate::class.java)
    private val t14Boolean = fn14Boolean.faktum(Boolean::class.java)
    private val t16LocalDate = fn16LocalDate.template(LocalDate::class.java)
    private val t17Boolean = fn17Boolean.template(Boolean::class.java)
    private val t18Boolean = fn18Boolean.template(Boolean::class.java)
    private val t15Int = fn15Int.faktum(Int::class.java, t16LocalDate, t17Boolean, t18Boolean)

    /* ktlint-disable parameter-list-wrapping */
    private val templateSubsumsjon = "rootSubsumsjon".alle(
        t1Boolean er true,
        t2Dato etter t_3_4_5Dato,
        t3Dato før t4Dato
    ) så (
        "makro" makro
            (t10Boolean er true eller (t12Boolean av t11Dokument))
            så (
                "minstEnSubsumsjon".minstEnAv(
                    t6Inntekt minst t8Inntekt,
                    t7Inntekt minst t9Inntekt
                ) så (t15Int med ("makro" makro (t16LocalDate før FaktumNavn(19, "18").faktum(LocalDate::class.java)))
                        så (t14Boolean er true)
                )
            )
            eller (t2Dato etter t13Dato)
        )


    internal lateinit var f1Boolean: Faktum<Boolean>
    internal lateinit var f2Dato: Faktum<LocalDate>
    internal lateinit var f3Dato: Faktum<LocalDate>
    internal lateinit var f4Dato: Faktum<LocalDate>
    internal lateinit var f5Dato: Faktum<LocalDate>
    internal lateinit var f_3_4_5Dato: Faktum<LocalDate>
    internal lateinit var f6Inntekt: Faktum<Inntekt>
    internal lateinit var f7Inntekt: Faktum<Inntekt>
    internal lateinit var f8Inntekt: Faktum<Inntekt>
    internal lateinit var f9Inntekt: Faktum<Inntekt>
    internal lateinit var f10Boolean: Faktum<Boolean>
    internal lateinit var f11Dokument: Faktum<Dokument>
    internal lateinit var f12Boolean: Faktum<Boolean>
    internal lateinit var f13Dato: Faktum<LocalDate>
    internal lateinit var f14Boolean: Faktum<Boolean>
    internal lateinit var f15Int: GeneratorFaktum
    internal lateinit var f16LocalDate: TemplateFaktum<LocalDate>
    internal lateinit var f17Boolean: TemplateFaktum<Boolean>
    internal lateinit var f18Boolean: TemplateFaktum<Boolean>

    internal lateinit var seksjon1: Seksjon
    internal lateinit var seksjon2: Seksjon
    internal lateinit var seksjon3: Seksjon
    internal lateinit var seksjon4: Seksjon
    internal lateinit var seksjon5: Seksjon
    internal lateinit var seksjon6: Seksjon
    internal lateinit var seksjon7: Seksjon
    internal lateinit var seksjon8: Seksjon

    private lateinit var _søknad: Søknad
    private lateinit var _rootSubsumsjon: Subsumsjon

    internal val søknad: Søknad get() {
        if (!this::_søknad.isInitialized) byggModell()
        return _søknad
    }

    internal val rootSubsumsjon: Subsumsjon get() {
        if (!this::_rootSubsumsjon.isInitialized) byggModell()
        return _rootSubsumsjon
    }

    private fun byggModell() {
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
        f13Dato = fn13Dato.faktum(LocalDate::class.java)
        f14Boolean = fn14Boolean.faktum(Boolean::class.java)
        f15Int = fn15Int.faktum(Int::class.java, f16LocalDate, f17Boolean, f18Boolean)
        f16LocalDate = fn16LocalDate.template(LocalDate::class.java)
        f17Boolean = fn17Boolean.template(Boolean::class.java)
        f18Boolean = fn18Boolean.template(Boolean::class.java)
        f_3_4_5Dato = listOf(f3Dato, f4Dato, f5Dato).faktum(fn_3_4_5Dato, MAKS_DATO)

        seksjon1 = Seksjon(Rolle.nav, f1Boolean, f2Dato)
        seksjon2 = Seksjon(Rolle.nav, f6Inntekt, f7Inntekt, f8Inntekt, f9Inntekt)
        seksjon3 = Seksjon(Rolle.nav, f15Int, f16LocalDate)
        seksjon4 = Seksjon(Rolle.søker, f3Dato, f4Dato, f5Dato, f_3_4_5Dato, f13Dato)
        seksjon5 = Seksjon(Rolle.søker, f10Boolean, f11Dokument)
        seksjon6 = Seksjon(Rolle.søker, f15Int)
        seksjon7 = Seksjon(Rolle.søker, f16LocalDate, f17Boolean)
        seksjon8 = Seksjon(Rolle.saksbehandler, f6Inntekt, f7Inntekt, f12Boolean, f14Boolean, f18Boolean)

        _søknad = Søknad(seksjon1, seksjon2, seksjon3, seksjon4, seksjon5)

        _rootSubsumsjon = templateSubsumsjon.deepCopy(_søknad.faktaMap())
    }
}

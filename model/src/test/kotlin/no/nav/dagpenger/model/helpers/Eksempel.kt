package no.nav.dagpenger.model.helpers

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
import no.nav.dagpenger.model.regel.gyldigGodkjentAv
import no.nav.dagpenger.model.regel.med
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.regel.under
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.eller
import no.nav.dagpenger.model.subsumsjon.makro
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.så
import no.nav.dagpenger.model.subsumsjon.uansett
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import java.time.LocalDate

internal class Eksempel {

    private val fn1Boolean = FaktumNavn<Boolean>(1, "f1")
    private val fn2Dato = FaktumNavn<LocalDate>(2, "f2")
    private val fn3Dato = FaktumNavn<LocalDate>(3, "f3")
    private val fn4Dato = FaktumNavn<LocalDate>(4, "f4")
    private val fn5Dato = FaktumNavn<LocalDate>(5, "f5")
    private val fn_3_4_5Dato = FaktumNavn<LocalDate>(345, "345")
    private val fn6Inntekt = FaktumNavn<Inntekt>(6, "f6")
    private val fn7Inntekt = FaktumNavn<Inntekt>(7, "f7")
    private val fn8Inntekt = FaktumNavn<Inntekt>(8, "f8")
    private val fn9Inntekt = FaktumNavn<Inntekt>(9, "f9")
    private val fn10Boolean = FaktumNavn<Boolean>(10, "f10")
    private val fn11Dokument = FaktumNavn<Dokument>(11, "f11")
    private val fn12Boolean = FaktumNavn<Boolean>(12, "f12")
    private val fn13Dato = FaktumNavn<LocalDate>(13, "f13")
    private val fn14Boolean = FaktumNavn<Boolean>(14, "f14")
    private val fn15Int = FaktumNavn<Int>(15, "f15")
    private val fn16Int = FaktumNavn<Int>(16, "f16")
    private val fn17Boolean = FaktumNavn<Boolean>(17, "f17")
    private val fn18Boolean = FaktumNavn<Boolean>(18, "f18")
    private val fn19Boolean = FaktumNavn<Boolean>(19, "f19")

    private val p1Boolean = fn1Boolean.faktum()
    private val p2Dato = fn2Dato.faktum()
    private val p3Dato = fn3Dato.faktum()
    private val p4Dato = fn4Dato.faktum()
    private val p5Dato = fn5Dato.faktum()
    private val p_3_4_5Dato = listOf(p3Dato, p4Dato, p5Dato).faktum(fn_3_4_5Dato, MAKS_DATO)
    private val p6Inntekt = fn6Inntekt.faktum()
    private val p7Inntekt = fn7Inntekt.faktum()
    private val p8Inntekt = fn8Inntekt.faktum()
    private val p9Inntekt = fn9Inntekt.faktum()
    private val p10Boolean = fn10Boolean.faktum()
    private val p11Dokument = fn11Dokument.faktum()
    private val p12Boolean = fn12Boolean.faktum().apply {
        this avhengerAv p11Dokument
    }
    private val p13Dato = fn13Dato.faktum()
    private val p14Boolean = fn14Boolean.faktum()
    private val p16Int = fn16Int.template()
    private val p17Boolean = fn17Boolean.template()
    private val p18Boolean = fn18Boolean.template()
    private val p19Boolean = fn19Boolean.faktum().apply {
        this avhengerAv p2Dato
        this avhengerAv p13Dato
    }
    private val p15Int = fn15Int.faktum(p16Int, p17Boolean, p18Boolean)

    private val datosjekk = "datosjekk".alle(
        p1Boolean er true,
        p2Dato etter p_3_4_5Dato,
        p3Dato før p4Dato
    )

    private val dokumentOpplastning = "dokumentopplastning" makro (
        p10Boolean er true eller (p12Boolean av p11Dokument)
        )

    private val inntektValidering = "inntektvalidering".minstEnAv(
        p6Inntekt minst p8Inntekt,
        p7Inntekt minst p9Inntekt
    )

    private val alderSjekk = "aldersjekk" makro (
        p16Int under 18 så (p17Boolean er true)
        )

    private val personerGodkjenning = p15Int med (
        alderSjekk
        )uansett (p14Boolean er true)

    /* ktlint-disable parameter-list-wrapping */
    private val prototypeSubsumsjon =
        datosjekk så (
            dokumentOpplastning så (
                inntektValidering så (
                    personerGodkjenning
                    )
                ) eller (
                (p2Dato etter p13Dato) gyldigGodkjentAv p19Boolean
                )
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
    internal lateinit var f16Int: TemplateFaktum<Int>
    internal lateinit var f17Boolean: TemplateFaktum<Boolean>
    internal lateinit var f18Boolean: TemplateFaktum<Boolean>
    internal lateinit var f19Boolean: Faktum<Boolean>

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

    internal val søknad: Søknad
        get() {
            if (!this::_søknad.isInitialized) byggModell()
            return _søknad
        }

    internal val rootSubsumsjon: Subsumsjon
        get() {
            if (!this::_rootSubsumsjon.isInitialized) byggModell()
            return _rootSubsumsjon
        }

    private fun byggModell() {
        f1Boolean = fn1Boolean.faktum()
        f2Dato = fn2Dato.faktum()
        f3Dato = fn3Dato.faktum()
        f4Dato = fn4Dato.faktum()
        f5Dato = fn5Dato.faktum()
        f_3_4_5Dato = listOf(f3Dato, f4Dato, f5Dato).faktum(fn_3_4_5Dato, MAKS_DATO)
        f6Inntekt = fn6Inntekt.faktum()
        f7Inntekt = fn7Inntekt.faktum()
        f8Inntekt = fn8Inntekt.faktum()
        f9Inntekt = fn9Inntekt.faktum()
        f10Boolean = fn10Boolean.faktum()
        f11Dokument = fn11Dokument.faktum()
        f12Boolean = fn12Boolean.faktum().apply {
            this avhengerAv f11Dokument
        }
        f13Dato = fn13Dato.faktum()
        f14Boolean = fn14Boolean.faktum()
        f16Int = fn16Int.template()
        f17Boolean = fn17Boolean.template()
        f18Boolean = fn18Boolean.template()
        f19Boolean = fn19Boolean.faktum()
        f15Int = fn15Int.faktum(f16Int, f17Boolean, f18Boolean)

        seksjon1 = Seksjon("seksjon1", Rolle.nav, f1Boolean, f2Dato)
        seksjon2 = Seksjon("seksjon2", Rolle.nav, f6Inntekt, f7Inntekt, f8Inntekt, f9Inntekt)
        seksjon3 = Seksjon("seksjon3", Rolle.nav, f15Int, f16Int)
        seksjon4 = Seksjon("seksjon4", Rolle.søker, f3Dato, f4Dato, f5Dato, f_3_4_5Dato, f13Dato)
        seksjon5 = Seksjon("seksjon5", Rolle.søker, f10Boolean, f11Dokument)
        seksjon6 = Seksjon("seksjon6", Rolle.søker, f15Int)
        seksjon7 = Seksjon("seksjon7", Rolle.søker, f16Int, f17Boolean)
        seksjon8 = Seksjon("seksjon8", Rolle.saksbehandler, f6Inntekt, f7Inntekt, f12Boolean, f14Boolean, f18Boolean, f19Boolean)

        _søknad = Søknad(seksjon1, seksjon2, seksjon3, seksjon4, seksjon5, seksjon6, seksjon7, seksjon8)

        _rootSubsumsjon = prototypeSubsumsjon.deepCopy(_søknad)
    }
}

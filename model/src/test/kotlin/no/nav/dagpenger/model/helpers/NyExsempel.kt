package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.GeneratorFaktum
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.TemplateFaktum
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
import java.lang.IllegalArgumentException
import java.time.LocalDate

internal class Eksempel {
    val fakta = listOf(
            ja nei "f1" id 1,
            dato faktum "f2" id 2,
            dato faktum "f3" id 3,
            dato faktum "f4" id 4,
            dato faktum "f5" id 5,
            inntekt faktum "f6" id 6,
            inntekt faktum "f7" id 7,
            inntekt faktum "f8" id 8,
            inntekt faktum "f9" id 9,
            ja nei "f10" id 10,
            dokument faktum "f11" id 11,
            ja nei "f12" id 12,
            dato faktum "f13" id 13,
            ja nei "f14" id 14,
            heltall faktum "f15" id 15,
            heltall faktum "f16" id 16,
            ja nei "f17" id 17,
            ja nei "f18" id 18,
            ja nei "f19" id 19
    )

    private val Int.faktum: Faktum<*> get() = fakta.firstOrNull { it.id == this.toString() }
            ?: throw IllegalArgumentException("Unexpected Faktum for Id: $this")

    init {
        12.faktum avhengerAv 11.faktum
        19.faktum avhengerAv 2.faktum
        19.faktum avhengerAv 13.faktum
        15.faktum generates 16.faktum og 17.faktum og 18.faktum  // Morph to templates here?
        maks dato "345" av 3.faktum og 4.faktum og 5.faktum id 345  // Need to add to Fakta
    }

    private val datosjekk = "datosjekk".alle(
            1.faktum er true,
            2.faktum etter 345.faktum,
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

    private lateinit var prototypeSøknad: Søknad
    private lateinit var _rootSubsumsjon: Subsumsjon

    internal val søknad: Søknad get() {
        if (!this::prototypeSøknad.isInitialized) byggModell()
        return prototypeSøknad
    }

    internal val rootSubsumsjon: Subsumsjon get() {
        if (!this::_rootSubsumsjon.isInitialized) byggModell()
        return _rootSubsumsjon
    }

    private fun byggModell() {
        // f1Boolean = fn1Boolean.faktum(Boolean::class.java)
        // f2Dato = fn2Dato.faktum(LocalDate::class.java)
        // f3Dato = fn3Dato.faktum(LocalDate::class.java)
        // f4Dato = fn4Dato.faktum(LocalDate::class.java)
        // f5Dato = fn5Dato.faktum(LocalDate::class.java)
        // f_3_4_5Dato = listOf(f3Dato, p4Dato, p5Dato).faktum(fn_3_4_5Dato, MAKS_DATO)
        // f6Inntekt = fn6Inntekt.faktum(Inntekt::class.java)
        // f7Inntekt = fn7Inntekt.faktum(Inntekt::class.java)
        // f8Inntekt = fn8Inntekt.faktum(Inntekt::class.java)
        // f9Inntekt = fn9Inntekt.faktum(Inntekt::class.java)
        // f10Boolean = fn10Boolean.faktum(Boolean::class.java)
        // f11Dokument = fn11Dokument.faktum(Dokument::class.java)
        // f12Boolean = fn12Boolean.faktum(Boolean::class.java).apply {
        //     this avhengerAv f11Dokument
        // }
        // f13Dato = fn13Dato.faktum(LocalDate::class.java)
        // f14Boolean = fn14Boolean.faktum(Boolean::class.java)
        // f16Int = fn16Int.template(Int::class.java)
        // f17Boolean = fn17Boolean.template(Boolean::class.java)
        // f18Boolean = fn18Boolean.template(Boolean::class.java)
        // f19Boolean = fn19Boolean.faktum(Boolean::class.java)
        // f15Int = fn15Int.faktum(Int::class.java, p16Int, p17Boolean, p18Boolean)

        seksjon1 = Seksjon("seksjon1", Rolle.nav, p1Boolean, p2Dato)
        seksjon2 = Seksjon("seksjon2", Rolle.nav, p6Inntekt, p7Inntekt, p8Inntekt, p9Inntekt)
        seksjon3 = Seksjon("seksjon3", Rolle.nav, p15Int, p16Int)
        seksjon4 = Seksjon("seksjon4", Rolle.søker, p3Dato, p4Dato, p5Dato, p_3_4_5Dato, p13Dato)
        seksjon5 = Seksjon("seksjon5", Rolle.søker, p10Boolean, p11Dokument)
        seksjon6 = Seksjon("seksjon6", Rolle.søker, p15Int)
        seksjon7 = Seksjon("seksjon7", Rolle.søker, p16Int, p17Boolean)
        seksjon8 = Seksjon("seksjon8", Rolle.saksbehandler, p6Inntekt, p7Inntekt, p12Boolean, p14Boolean, p18Boolean, p19Boolean)

        prototypeSøknad = Søknad(prototypeSubsumsjon, seksjon1, seksjon2, seksjon3, seksjon4, seksjon5, seksjon6, seksjon7, seksjon8)

        søknad = prototypeSøknad.bygg()

        //_rootSubsumsjon = prototypeSubsumsjon.deepCopy(_søknad)

        f1Boolean = søknad.finnFaktum("1")
    }
}

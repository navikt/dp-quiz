package no.nav.dagpenger.model.regel

import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.subsumsjon.AvSubsumsjon
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.subsumsjon.GeneratorSubsumsjon
import no.nav.dagpenger.model.subsumsjon.GodkjenningsSubsumsjon
import no.nav.dagpenger.model.subsumsjon.MakroSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import java.time.LocalDate

interface Regel {
    val typeNavn: String
    fun resultat(fakta: List<Faktum<*>>): Boolean
    fun toString(fakta: List<Faktum<*>>): String
}

private class Etter(private val senesteDato: Faktum<LocalDate>, private val tidligsteDato: Faktum<LocalDate>) : Regel {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun resultat(fakta: List<Faktum<*>>) =
        (fakta[1] as Faktum<LocalDate>).svar() < (fakta[0] as Faktum<LocalDate>).svar()
    override fun toString(fakta: List<Faktum<*>>) = "Sjekk at '$fakta[0]' er etter '$fakta[1]'"
}

infix fun Faktum<LocalDate>.etter(tidligsteDato: Faktum<LocalDate>): Subsumsjon {
    return EnkelSubsumsjon(
        Etter(this, tidligsteDato),
        this,
        tidligsteDato
    )
}

private class Før(private val tidligsteDato: Faktum<LocalDate>, private val senesteDato: Faktum<LocalDate>) : Regel {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun resultat(fakta: List<Faktum<*>>) = (fakta[0] as Faktum<LocalDate>).svar() < (fakta[1] as Faktum<LocalDate>).svar()
    override fun toString(fakta: List<Faktum<*>>) = "Sjekk at '$fakta[0]' er før '$fakta[1]'"
}

infix fun Faktum<LocalDate>.før(senesteDato: Faktum<LocalDate>): Subsumsjon {
    return EnkelSubsumsjon(
        Før(this, senesteDato),
        this,
        senesteDato
    )
}

private class IkkeFør(private val tidligsteDato: Faktum<LocalDate>, private val senesteDato: Faktum<LocalDate>) : Regel {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun resultat(fakta: List<Faktum<*>>) = (fakta[0] as Faktum<LocalDate>).svar() >= (fakta[1] as Faktum<LocalDate>).svar()
    override fun toString(fakta: List<Faktum<*>>) = "Sjekk at '$fakta[0]' ikke er før '$fakta[1]'"
}

infix fun Faktum<LocalDate>.ikkeFør(senesteDato: Faktum<LocalDate>): Subsumsjon {
    return EnkelSubsumsjon(
        IkkeFør(this, senesteDato),
        this,
        senesteDato
    )
}

private class Minst(private val faktisk: Faktum<Inntekt>, private val terskel: Faktum<Inntekt>) : Regel {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun resultat(fakta: List<Faktum<*>>) = (fakta[0] as Faktum<Inntekt>).svar() >= (fakta[1] as Faktum<Inntekt>).svar()
    override fun toString(fakta: List<Faktum<*>>) = "Sjekk at '$fakta[0]' er minst '$fakta[1]'"
}

infix fun Faktum<Inntekt>.minst(terskel: Faktum<Inntekt>): Subsumsjon {
    return EnkelSubsumsjon(
        Minst(this, terskel),
        this,
        terskel
    )
}

private class Er<T : Comparable<T>>(private val faktum: Faktum<*>, private val other: T) : Regel {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun resultat(fakta: List<Faktum<*>>) = fakta[0].svar() == other
    override fun toString(fakta: List<Faktum<*>>) = "Sjekk at `$fakta[0]` er lik $other"
}

infix fun <T : Comparable<T>> Faktum<T>.er(other: T): Subsumsjon {
    return EnkelSubsumsjon(
        Er(this, other),
        this
    )
}

infix fun GeneratorFaktum.med(makro: MakroSubsumsjon): Subsumsjon {
    require(makro.gyldig == TomSubsumsjon && makro.ugyldig == TomSubsumsjon) {
        "Generator makroer kan ikke ha gyldig eller ugyldig stier"
    }

    return MakroSubsumsjon(
        this.navn,
        GeneratorSubsumsjon(
            Er(this, 0),
            this,
            makro
        )
    )
}

private class ErIkke(private val faktum: Faktum<Boolean>) : Regel {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun resultat(fakta: List<Faktum<*>>) = !(fakta[0] as Faktum<Boolean>).svar()
    override fun toString(fakta: List<Faktum<*>>) = "Sjekk at `$fakta[0]` ikke er sann"
}

fun erIkke(faktum: Faktum<Boolean>): Subsumsjon {
    return EnkelSubsumsjon(
        ErIkke(faktum),
        faktum
    )
}

private class Har(private val faktum: Faktum<Boolean>) : Regel {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun resultat(fakta: List<Faktum<*>>) = (fakta[0] as Faktum<Boolean>).svar()
    override fun toString(fakta: List<Faktum<*>>) = "Sjekk at `$fakta[0]` er sann"
}

fun har(faktum: Faktum<Boolean>): Subsumsjon {
    return EnkelSubsumsjon(
        Har(faktum),
        faktum
    )
}

private class Av(private val godkjenning: Faktum<Boolean>, private val dokument: Faktum<Dokument>) : Regel {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun resultat(fakta: List<Faktum<*>>) = true
    override fun toString(fakta: List<Faktum<*>>) = ""
}

infix fun Faktum<Boolean>.av(dokument: Faktum<Dokument>): Subsumsjon {
    return AvSubsumsjon(Av(this, dokument), dokument, this)
}

private class Under(private val alder: Faktum<Int>, private val maksAlder: Int) : Regel {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun resultat(fakta: List<Faktum<*>>) = (fakta[0] as Faktum<Int>).svar() < maksAlder
    override fun toString(fakta: List<Faktum<*>>) = "Sjekk at '$fakta[0]' er under $maksAlder"
}

infix fun Faktum<Int>.under(maksAlder: Int): Subsumsjon {
    return EnkelSubsumsjon(
        Under(this, maksAlder),
        this
    )
}

infix fun Subsumsjon.godkjentAv(faktum: Faktum<Boolean>) =
    GodkjenningsSubsumsjon(GodkjenningsSubsumsjon.Action.UansettAction, this, faktum).also {
        faktum.sjekkAvhengigheter()
    }

infix fun Subsumsjon.gyldigGodkjentAv(faktum: Faktum<Boolean>) =
    GodkjenningsSubsumsjon(GodkjenningsSubsumsjon.Action.JaAction, this, faktum).also {
        faktum.sjekkAvhengigheter()
    }

infix fun Subsumsjon.ugyldigGodkjentAv(faktum: Faktum<Boolean>) =
    GodkjenningsSubsumsjon(GodkjenningsSubsumsjon.Action.NeiAction, this, faktum).also {
        faktum.sjekkAvhengigheter()
    }

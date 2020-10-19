package no.nav.dagpenger.model.regel

import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.GeneratorFaktum
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.UtledetFaktum
import no.nav.dagpenger.model.subsumsjon.AvSubsumsjon
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.subsumsjon.GeneratorSubsumsjon
import no.nav.dagpenger.model.subsumsjon.GodkjenningsSubsumsjon
import no.nav.dagpenger.model.subsumsjon.MakroSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import no.nav.dagpenger.model.søknad.Søknad
import java.time.LocalDate

interface Regel {
    val typeNavn: String
    fun resultat(): Boolean
    fun deepCopy(søknad: Søknad): Regel
    fun deepCopy(indeks: Int, søknad: Søknad): Regel
}

private class Etter(private val senesteDato: Faktum<LocalDate>, private val tidligsteDato: Faktum<LocalDate>) : Regel {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun resultat() = tidligsteDato.svar() < senesteDato.svar()
    override fun toString() = "Sjekk at '${senesteDato.navn}' er etter '${tidligsteDato.navn}'"
    override fun deepCopy(søknad: Søknad): Regel {
        return Etter(søknad.faktum(senesteDato.navn) as Faktum<LocalDate>, søknad.faktum(tidligsteDato.navn) as Faktum<LocalDate>)
    }

    override fun deepCopy(indeks: Int, søknad: Søknad): Regel {
        return Etter(
            senesteDato.med(indeks, søknad) as Faktum<LocalDate>,
            tidligsteDato.med(indeks, søknad) as Faktum<LocalDate>
        )
    }
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
    override fun resultat() = tidligsteDato.svar() < senesteDato.svar()
    override fun toString() = "Sjekk at '${tidligsteDato.navn}' er før '${senesteDato.navn}'"
    override fun deepCopy(søknad: Søknad): Regel {
        return Før(søknad.faktum(tidligsteDato.navn) as Faktum<LocalDate>, søknad.faktum(senesteDato.navn) as Faktum<LocalDate>)
    }

    override fun deepCopy(indeks: Int, søknad: Søknad): Regel {
        return Før(
            tidligsteDato.med(indeks, søknad) as Faktum<LocalDate>,
            senesteDato.med(indeks, søknad) as Faktum<LocalDate>
        )
    }
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
    override fun resultat() = tidligsteDato.svar() >= senesteDato.svar()
    override fun toString() = "Sjekk at '${tidligsteDato.navn}' ikke er før '${senesteDato.navn}'"
    override fun deepCopy(søknad: Søknad): Regel {
        return IkkeFør(søknad.faktum(tidligsteDato.navn) as Faktum<LocalDate>, søknad.faktum(senesteDato.navn) as Faktum<LocalDate>)
    }

    override fun deepCopy(indeks: Int, søknad: Søknad): Regel {
        return IkkeFør(
            tidligsteDato.med(indeks, søknad) as Faktum<LocalDate>,
            senesteDato.med(indeks, søknad) as Faktum<LocalDate>
        )
    }
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
    override fun resultat() = faktisk.svar() >= terskel.svar()
    override fun toString() = "Sjekk at '${faktisk.navn}' er minst '${terskel.navn}'"
    override fun deepCopy(søknad: Søknad): Regel {
        return Minst(søknad.faktum(faktisk.navn) as Faktum<Inntekt>, søknad.faktum(terskel.navn) as Faktum<Inntekt>)
    }

    override fun deepCopy(indeks: Int, søknad: Søknad): Regel {
        return Minst(
            faktisk.med(indeks, søknad) as Faktum<Inntekt>,
            terskel.med(indeks, søknad) as Faktum<Inntekt>
        )
    }
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
    override fun resultat() = faktum.svar() == other
    override fun toString() = "Sjekk at `${faktum.navn}` er lik $other"
    override fun deepCopy(søknad: Søknad): Regel {
        return Er(søknad.faktum(faktum.navn) as Faktum<T>, other)
    }

    override fun deepCopy(indeks: Int, søknad: Søknad): Regel {
        return Er(faktum.med(indeks, søknad) as Faktum<T>, other)
    }
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
        this.navn.navn,
        GeneratorSubsumsjon(
            Er(this, 0),
            this,
            makro
        )
    )
}

private class ErIkke(private val faktum: Faktum<Boolean>) : Regel {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun resultat() = !faktum.svar()
    override fun toString() = "Sjekk at `${faktum.navn}` ikke er sann"
    override fun deepCopy(søknad: Søknad): Regel {
        return ErIkke(søknad.faktum(faktum.navn) as Faktum<Boolean>)
    }

    override fun deepCopy(indeks: Int, søknad: Søknad): Regel {
        return ErIkke(faktum.med(indeks, søknad) as Faktum<Boolean>)
    }
}

fun erIkke(faktum: Faktum<Boolean>): Subsumsjon {
    return EnkelSubsumsjon(
        ErIkke(faktum),
        faktum
    )
}

private class Har(private val faktum: Faktum<Boolean>) : Regel {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun resultat() = faktum.svar()
    override fun toString() = "Sjekk at `${faktum.navn}` er sann"
    override fun deepCopy(søknad: Søknad): Regel {
        return Har(søknad.faktum(faktum.navn) as Faktum<Boolean>)
    }

    override fun deepCopy(indeks: Int, søknad: Søknad): Regel {
        return Har(faktum.med(indeks, søknad) as Faktum<Boolean>)
    }
}

fun har(faktum: Faktum<Boolean>): Subsumsjon {
    return EnkelSubsumsjon(
        Har(faktum),
        faktum
    )
}

private class Av(private val godkjenning: Faktum<Boolean>, private val dokument: Faktum<Dokument>) : Regel {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun resultat() = dokument.erBesvart() && (!godkjenning.erBesvart() || godkjenning.svar())
    override fun toString() = "Sjekk at `${dokument.navn}` er ${if (resultat()) "godkjent" else "ikke godkjent"}"
    override fun deepCopy(søknad: Søknad): Regel {
        return Av(søknad.faktum(godkjenning.navn) as Faktum<Boolean>, søknad.faktum(dokument.navn) as Faktum<Dokument>)
    }

    override fun deepCopy(indeks: Int, søknad: Søknad): Regel {
        return Av(godkjenning.med(indeks, søknad) as Faktum<Boolean>, dokument.med(indeks, søknad) as Faktum<Dokument>)
    }
}

infix fun Faktum<Boolean>.av(dokument: Faktum<Dokument>): Subsumsjon {
    return AvSubsumsjon(Av(this, dokument), dokument, this)
}

private class Under(private val alder: Faktum<Int>, private val maksAlder: Int) : Regel {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun resultat() = alder.svar() < maksAlder
    override fun toString() = "Sjekk at '${alder.navn}' er under $maksAlder"
    override fun deepCopy(søknad: Søknad): Regel {
        return Under(søknad.faktum(alder.navn) as Faktum<Int>, maksAlder)
    }

    override fun deepCopy(indeks: Int, søknad: Søknad): Regel {
        return Under(alder.med(indeks, søknad) as Faktum<Int>, maksAlder)
    }
}

infix fun Faktum<Int>.under(maksAlder: Int): Subsumsjon {
    return EnkelSubsumsjon(
        Under(this, maksAlder),
        this
    )
}

infix fun Subsumsjon.godkjentAv(faktum: Faktum<Boolean>): Subsumsjon {
    return GodkjenningsSubsumsjon(GodkjenningsSubsumsjon.Action.UansettAction, this, faktum)
}

infix fun Subsumsjon.gyldigGodkjentAv(faktum: Faktum<Boolean>): Subsumsjon {
    return GodkjenningsSubsumsjon(GodkjenningsSubsumsjon.Action.JaAction, this, faktum)
}

infix fun Subsumsjon.ugyldigGodkjentAv(faktum: Faktum<Boolean>): Subsumsjon {
    return GodkjenningsSubsumsjon(GodkjenningsSubsumsjon.Action.NeiAction, this, faktum)
}

val MAKS_DATO = UtledetFaktum<LocalDate>::max
val MAKS_INNTEKT = UtledetFaktum<Inntekt>::max
val ALLE_JA = UtledetFaktum<Boolean>::alle

package no.nav.dagpenger.model.regel

import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Fakta
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
    fun bygg(fakta: Fakta): Regel
    fun deepCopy(indeks: Int, søknad: Søknad): Regel
}

private class Etter(private val senesteDato: Faktum<LocalDate>, private val tidligsteDato: Faktum<LocalDate>) : Regel {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun resultat() = tidligsteDato.svar() < senesteDato.svar()
    override fun toString() = "Sjekk at '$senesteDato' er etter '$tidligsteDato'"

    override fun deepCopy(søknad: Søknad): Regel {
        return Etter(søknad.faktum(senesteDato.faktumId) as Faktum<LocalDate>, søknad.faktum(tidligsteDato.faktumId) as Faktum<LocalDate>)
    }
    override fun bygg(fakta: Fakta) = Etter(fakta.dato(senesteDato.faktumId), fakta.dato(tidligsteDato.faktumId))

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
    override fun toString() = "Sjekk at '$tidligsteDato' er før '$senesteDato'"
    override fun deepCopy(søknad: Søknad): Regel {
        return Før(søknad.faktum(tidligsteDato.faktumId) as Faktum<LocalDate>, søknad.faktum(senesteDato.faktumId) as Faktum<LocalDate>)
    }

    override fun bygg(fakta: Fakta) = Før(fakta.dato(tidligsteDato.faktumId), fakta.dato(senesteDato.faktumId))

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
    override fun toString() = "Sjekk at '$tidligsteDato' ikke er før '$senesteDato'"
    override fun deepCopy(søknad: Søknad): Regel {
        return IkkeFør(søknad.faktum(tidligsteDato.faktumId) as Faktum<LocalDate>, søknad.faktum(senesteDato.faktumId) as Faktum<LocalDate>)
    }

    override fun bygg(fakta: Fakta) = IkkeFør(fakta.dato(tidligsteDato.faktumId), fakta.dato(senesteDato.faktumId))

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
    override fun toString() = "Sjekk at '$faktisk' er minst '$terskel'"
    override fun deepCopy(søknad: Søknad) =
        Minst(søknad.faktum(faktisk.faktumId) as Faktum<Inntekt>, søknad.faktum(terskel.faktumId) as Faktum<Inntekt>)

    override fun bygg(fakta: Fakta) = Minst(fakta.inntekt(faktisk.faktumId), fakta.inntekt(terskel.faktumId))

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
    override fun toString() = "Sjekk at `$faktum` er lik $other"
    override fun deepCopy(søknad: Søknad) = Er(søknad.faktum(faktum.faktumId) as Faktum<T>, other)

    override fun bygg(fakta: Fakta) = Er(fakta.id(faktum.faktumId) as Faktum<T>, other)

    override fun deepCopy(indeks: Int, søknad: Søknad) = Er(faktum.med(indeks, søknad) as Faktum<T>, other)
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
    override fun resultat() = !faktum.svar()
    override fun toString() = "Sjekk at `$faktum` ikke er sann"
    override fun deepCopy(søknad: Søknad) = ErIkke(søknad.faktum(faktum.faktumId) as Faktum<Boolean>)

    override fun bygg(fakta: Fakta) = ErIkke(fakta.ja(faktum.faktumId))

    override fun deepCopy(indeks: Int, søknad: Søknad) = ErIkke(faktum.med(indeks, søknad) as Faktum<Boolean>)
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
    override fun toString() = "Sjekk at `$faktum` er sann"
    override fun deepCopy(søknad: Søknad) = Har(søknad.faktum(faktum.faktumId) as Faktum<Boolean>)

    override fun bygg(fakta: Fakta) = Har(fakta.ja(faktum.faktumId))

    override fun deepCopy(indeks: Int, søknad: Søknad) = Har(faktum.med(indeks, søknad) as Faktum<Boolean>)
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
    override fun toString() = "Sjekk at `$dokument` er ${if (resultat()) "godkjent" else "ikke godkjent"}"
    override fun deepCopy(søknad: Søknad) =
        Av(søknad.faktum(godkjenning.faktumId) as Faktum<Boolean>, søknad.faktum(dokument.faktumId) as Faktum<Dokument>)

    override fun bygg(fakta: Fakta) =
        Av(fakta.ja(godkjenning.faktumId), fakta.dokument(dokument.faktumId))

    override fun deepCopy(indeks: Int, søknad: Søknad) =
        Av(godkjenning.med(indeks, søknad) as Faktum<Boolean>, dokument.med(indeks, søknad) as Faktum<Dokument>)
}

infix fun Faktum<Boolean>.av(dokument: Faktum<Dokument>): Subsumsjon {
    return AvSubsumsjon(Av(this, dokument), dokument, this)
}

private class Under(private val alder: Faktum<Int>, private val maksAlder: Int) : Regel {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun resultat() = alder.svar() < maksAlder
    override fun toString() = "Sjekk at '$alder' er under $maksAlder"
    override fun deepCopy(søknad: Søknad) = Under(søknad.faktum(alder.faktumId) as Faktum<Int>, maksAlder)

    override fun bygg(fakta: Fakta) = Under(fakta.heltall(alder.faktumId), maksAlder)

    override fun deepCopy(indeks: Int, søknad: Søknad) =
        Under(alder.med(indeks, søknad) as Faktum<Int>, maksAlder)
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

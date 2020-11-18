package no.nav.dagpenger.model.regel

import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
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
    fun deepCopy(søknadprosess: Søknadprosess): Regel
    fun bygg(søknad: Søknad): Regel
    fun deepCopy(indeks: Int, søknad: Søknad): Regel
}

private class Etter(private val senesteDato: Faktum<LocalDate>, private val tidligsteDato: Faktum<LocalDate>) : Regel {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun resultat(fakta: List<Faktum<*>>) = (fakta[1] as Faktum<LocalDate>).svar() < (fakta[0] as Faktum<LocalDate>).svar()
    override fun toString() = "Sjekk at '$senesteDato' er etter '$tidligsteDato'"

    override fun deepCopy(søknadprosess: Søknadprosess): Regel {
        return Etter(søknadprosess.faktum(senesteDato.faktumId) as Faktum<LocalDate>, søknadprosess.faktum(tidligsteDato.faktumId) as Faktum<LocalDate>)
    }
    override fun bygg(søknad: Søknad) = Etter(søknad.dato(senesteDato.faktumId), søknad.dato(tidligsteDato.faktumId))

    override fun deepCopy(indeks: Int, søknad: Søknad): Regel {
        return Etter(
            senesteDato.deepCopy(indeks, søknad) as Faktum<LocalDate>,
            tidligsteDato.deepCopy(indeks, søknad) as Faktum<LocalDate>
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
    override fun resultat(fakta: List<Faktum<*>>) = (fakta[0] as Faktum<LocalDate>).svar() < (fakta[1] as Faktum<LocalDate>).svar()
    override fun toString() = "Sjekk at '$tidligsteDato' er før '$senesteDato'"
    override fun deepCopy(søknadprosess: Søknadprosess): Regel {
        return Før(søknadprosess.faktum(tidligsteDato.faktumId) as Faktum<LocalDate>, søknadprosess.faktum(senesteDato.faktumId) as Faktum<LocalDate>)
    }

    override fun bygg(søknad: Søknad) = Før(søknad.dato(tidligsteDato.faktumId), søknad.dato(senesteDato.faktumId))

    override fun deepCopy(indeks: Int, søknad: Søknad): Regel {
        return Før(
            tidligsteDato.deepCopy(indeks, søknad) as Faktum<LocalDate>,
            senesteDato.deepCopy(indeks, søknad) as Faktum<LocalDate>
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
    override fun resultat(fakta: List<Faktum<*>>) = (fakta[0] as Faktum<LocalDate>).svar() >= (fakta[1] as Faktum<LocalDate>).svar()
    override fun toString() = "Sjekk at '$tidligsteDato' ikke er før '$senesteDato'"
    override fun deepCopy(søknadprosess: Søknadprosess): Regel {
        return IkkeFør(søknadprosess.faktum(tidligsteDato.faktumId) as Faktum<LocalDate>, søknadprosess.faktum(senesteDato.faktumId) as Faktum<LocalDate>)
    }

    override fun bygg(søknad: Søknad) = IkkeFør(søknad.dato(tidligsteDato.faktumId), søknad.dato(senesteDato.faktumId))

    override fun deepCopy(indeks: Int, søknad: Søknad): Regel {
        return IkkeFør(
            tidligsteDato.deepCopy(indeks, søknad) as Faktum<LocalDate>,
            senesteDato.deepCopy(indeks, søknad) as Faktum<LocalDate>
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
    override fun resultat(fakta: List<Faktum<*>>) = (fakta[0] as Faktum<Inntekt>).svar() >= (fakta[1] as Faktum<Inntekt>).svar()
    override fun toString() = "Sjekk at '$faktisk' er minst '$terskel'"
    override fun deepCopy(søknadprosess: Søknadprosess) =
        Minst(søknadprosess.faktum(faktisk.faktumId) as Faktum<Inntekt>, søknadprosess.faktum(terskel.faktumId) as Faktum<Inntekt>)

    override fun bygg(søknad: Søknad) = Minst(søknad.inntekt(faktisk.faktumId), søknad.inntekt(terskel.faktumId))

    override fun deepCopy(indeks: Int, søknad: Søknad): Regel {
        return Minst(
            faktisk.deepCopy(indeks, søknad) as Faktum<Inntekt>,
            terskel.deepCopy(indeks, søknad) as Faktum<Inntekt>
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
    override fun resultat(fakta: List<Faktum<*>>) = fakta[0].svar() == other
    override fun toString() = "Sjekk at `$faktum` er lik $other"
    override fun deepCopy(søknadprosess: Søknadprosess) = Er(søknadprosess.faktum(faktum.faktumId) as Faktum<T>, other)

    override fun bygg(søknad: Søknad) = Er(søknad.id(faktum.faktumId) as Faktum<T>, other)

    override fun deepCopy(indeks: Int, søknad: Søknad) = Er(faktum.deepCopy(indeks, søknad) as Faktum<T>, other)
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
    override fun toString() = "Sjekk at `$faktum` ikke er sann"
    override fun deepCopy(søknadprosess: Søknadprosess) = ErIkke(søknadprosess.faktum(faktum.faktumId) as Faktum<Boolean>)

    override fun bygg(søknad: Søknad) = ErIkke(søknad.ja(faktum.faktumId))

    override fun deepCopy(indeks: Int, søknad: Søknad) = ErIkke(faktum.deepCopy(indeks, søknad) as Faktum<Boolean>)
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
    override fun toString() = "Sjekk at `$faktum` er sann"
    override fun deepCopy(søknadprosess: Søknadprosess) = Har(søknadprosess.faktum(faktum.faktumId) as Faktum<Boolean>)

    override fun bygg(søknad: Søknad) = Har(søknad.ja(faktum.faktumId))

    override fun deepCopy(indeks: Int, søknad: Søknad) = Har(faktum.deepCopy(indeks, søknad) as Faktum<Boolean>)
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
    override fun deepCopy(søknadprosess: Søknadprosess) =
        Av(søknadprosess.faktum(godkjenning.faktumId) as Faktum<Boolean>, søknadprosess.faktum(dokument.faktumId) as Faktum<Dokument>)

    override fun bygg(søknad: Søknad) =
        Av(søknad.ja(godkjenning.faktumId), søknad.dokument(dokument.faktumId))

    override fun deepCopy(indeks: Int, søknad: Søknad) =
        Av(godkjenning.deepCopy(indeks, søknad) as Faktum<Boolean>, dokument.deepCopy(indeks, søknad) as Faktum<Dokument>)
}

infix fun Faktum<Boolean>.av(dokument: Faktum<Dokument>): Subsumsjon {
    return AvSubsumsjon(Av(this, dokument), dokument, this)
}

private class Under(private val alder: Faktum<Int>, private val maksAlder: Int) : Regel {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun resultat(fakta: List<Faktum<*>>) = (fakta[0] as Faktum<Int>).svar() < maksAlder
    override fun toString() = "Sjekk at '$alder' er under $maksAlder"
    override fun deepCopy(søknadprosess: Søknadprosess) = Under(søknadprosess.faktum(alder.faktumId) as Faktum<Int>, maksAlder)

    override fun bygg(søknad: Søknad) = Under(søknad.heltall(alder.faktumId), maksAlder)

    override fun deepCopy(indeks: Int, søknad: Søknad) =
        Under(alder.deepCopy(indeks, søknad) as Faktum<Int>, maksAlder)
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

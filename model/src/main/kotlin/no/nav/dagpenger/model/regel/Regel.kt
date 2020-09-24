package no.nav.dagpenger.model.regel

import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.UtledetFaktum
import no.nav.dagpenger.model.fakta.deepCopy
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import java.time.LocalDate

interface Regel {
    val typeNavn: String
    fun resultat(): Boolean
    fun deepCopy(faktaMap: Map<FaktumNavn, Faktum<*>>): Regel
}
private class Etter(private val senesteDato: Faktum<LocalDate>, private val tidligsteDato: Faktum<LocalDate>) : Regel {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun resultat() = tidligsteDato.svar() < senesteDato.svar()
    override fun toString() = "Sjekk at '${senesteDato.navn}' er etter '${tidligsteDato.navn}'"
    override fun deepCopy(faktaMap: Map<FaktumNavn, Faktum<*>>): Regel {
        return Etter(faktaMap[senesteDato.navn] as Faktum<LocalDate>, faktaMap[tidligsteDato.navn] as Faktum<LocalDate>)
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
    override fun deepCopy(faktaMap: Map<FaktumNavn, Faktum<*>>): Regel {
        return Før(faktaMap[tidligsteDato.navn] as Faktum<LocalDate>, faktaMap[senesteDato.navn] as Faktum<LocalDate>)
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
    override fun deepCopy(faktaMap: Map<FaktumNavn, Faktum<*>>): Regel {
        return IkkeFør(faktaMap[tidligsteDato.navn] as Faktum<LocalDate>, faktaMap[senesteDato.navn] as Faktum<LocalDate>)
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
    override fun deepCopy(faktaMap: Map<FaktumNavn, Faktum<*>>): Regel {
        return Minst(faktaMap[faktisk.navn] as Faktum<Inntekt>, faktaMap[terskel.navn] as Faktum<Inntekt>)
    }
}
infix fun Faktum<Inntekt>.minst(terskel: Faktum<Inntekt>): Subsumsjon {
    return EnkelSubsumsjon(
        Minst(this, terskel),
        this,
        terskel
    )
}
private class Er<T : Comparable<T>>(private val faktum: Faktum<*>, private val annen: T) : Regel {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun resultat() = faktum.svar() == annen
    override fun toString() = "Sjekk at `${faktum.navn}` er lik $annen"
    override fun deepCopy(faktaMap: Map<FaktumNavn, Faktum<*>>): Regel {
        return Er(faktaMap[faktum.navn] as Faktum<T>, annen)
    }
}

infix fun <T : Comparable<T>> Faktum<T>.er(annen: T): Subsumsjon {
    return EnkelSubsumsjon(
        Er(this, annen),
        this
    )
}

private class ErIkke(private val faktum: Faktum<Boolean>) : Regel {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun resultat() = !faktum.svar()
    override fun toString() = "Sjekk at `${faktum.navn}` ikke er sann"
    override fun deepCopy(faktaMap: Map<FaktumNavn, Faktum<*>>): Regel {
        return ErIkke(faktaMap[faktum.navn] as Faktum<Boolean>)
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
    override fun deepCopy(faktaMap: Map<FaktumNavn, Faktum<*>>): Regel {
        return Har(faktaMap[faktum.navn] as Faktum<Boolean>)
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
    override fun toString() = "Sjekk at `${dokument.navn}` er ${if (resultat()) "godkjent" else "ikke godkjent" }"
    override fun deepCopy(faktaMap: Map<FaktumNavn, Faktum<*>>): Regel {
        return Av(faktaMap[godkjenning.navn] as Faktum<Boolean>, faktaMap[dokument.navn] as Faktum<Dokument>)
    }
}
private class AvSubsumsjon private constructor(
    private val regel: Regel,
    private val dokument: Faktum<Dokument>,
    private val godkjenning: Faktum<Boolean>,
    gyldigSubsumsjon: Subsumsjon,
    ugyldigSubsumsjon: Subsumsjon
) : EnkelSubsumsjon(
    regel,
    setOf(dokument, godkjenning),
    gyldigSubsumsjon,
    ugyldigSubsumsjon
) {

    constructor(regel: Regel, dokument: Faktum<Dokument>, godkjenning: Faktum<Boolean>) : this(
        regel,
        dokument,
        godkjenning,
        TomSubsumsjon,
        TomSubsumsjon
    )

    override fun lokaltResultat(): Boolean? {
        return if (dokument.erBesvart()) regel.resultat() else null
    }

    override fun ukjenteFakta(): Set<GrunnleggendeFaktum<*>> =
        if (dokument.erBesvart()) emptySet() else dokument.grunnleggendeFakta()

    override fun deepCopy(faktaMap: Map<FaktumNavn, Faktum<*>>) = AvSubsumsjon(
        regel.deepCopy(faktaMap),
        faktaMap[dokument.navn ] as Faktum<Dokument>,
        faktaMap[godkjenning.navn] as Faktum<Boolean>,
        gyldigSubsumsjon.deepCopy(faktaMap),
        ugyldigSubsumsjon.deepCopy(faktaMap)
    )
}

infix fun Faktum<Boolean>.av(dokument: Faktum<Dokument>): Subsumsjon {
    return AvSubsumsjon(Av(this, dokument), dokument, this)
}

val MAKS_DATO = UtledetFaktum<LocalDate>::max

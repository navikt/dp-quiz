package no.nav.dagpenger.model.regel

import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.GeneratorFaktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.UtledetFaktum
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.subsumsjon.GeneratorSubsumsjon
import no.nav.dagpenger.model.subsumsjon.MakroSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import no.nav.dagpenger.model.søknad.Søknad
import java.time.LocalDate

interface Regel {
    val typeNavn: String
    fun resultat(): Boolean
    fun deepCopy(søknad: Søknad): Regel
    fun deepCopy(indeks: Int): Regel
}
private class Etter(private val senesteDato: Faktum<LocalDate>, private val tidligsteDato: Faktum<LocalDate>) : Regel {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun resultat() = tidligsteDato.svar() < senesteDato.svar()
    override fun toString() = "Sjekk at '${senesteDato.navn}' er etter '${tidligsteDato.navn}'"
    override fun deepCopy(søknad: Søknad): Regel {
        return Etter(søknad.faktaMap()[senesteDato.navn] as Faktum<LocalDate>, søknad.faktaMap()[tidligsteDato.navn] as Faktum<LocalDate>)
    }
    override fun deepCopy(indeks: Int): Regel {
        return Etter(senesteDato.tilFaktum(indeks) as Faktum<LocalDate>, tidligsteDato.tilFaktum(indeks) as Faktum<LocalDate>)
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
        return Før(søknad.faktaMap()[tidligsteDato.navn] as Faktum<LocalDate>, søknad.faktaMap()[senesteDato.navn] as Faktum<LocalDate>)
    }
    override fun deepCopy(indeks: Int): Regel {
        return Før(tidligsteDato.tilFaktum(indeks) as Faktum<LocalDate>, senesteDato.tilFaktum(indeks) as Faktum<LocalDate>)
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
        return IkkeFør(søknad.faktaMap()[tidligsteDato.navn] as Faktum<LocalDate>, søknad.faktaMap()[senesteDato.navn] as Faktum<LocalDate>)
    }
    override fun deepCopy(indeks: Int): Regel {
        return IkkeFør(tidligsteDato.tilFaktum(indeks) as Faktum<LocalDate>, senesteDato.tilFaktum(indeks) as Faktum<LocalDate>)
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
        return Minst(søknad.faktaMap()[faktisk.navn] as Faktum<Inntekt>, søknad.faktaMap()[terskel.navn] as Faktum<Inntekt>)
    }
    override fun deepCopy(indeks: Int): Regel {
        return Minst(faktisk.tilFaktum(indeks) as Faktum<Inntekt>, terskel.tilFaktum(indeks) as Faktum<Inntekt>)
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
        return Er(søknad.faktaMap()[faktum.navn] as Faktum<T>, other)
    }
    override fun deepCopy(indeks: Int): Regel {
        return Er(faktum.tilFaktum(indeks) as Faktum<T>, other)
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
        return ErIkke(søknad.faktaMap()[faktum.navn] as Faktum<Boolean>)
    }
    override fun deepCopy(indeks: Int): Regel {
        return ErIkke(faktum.tilFaktum(indeks) as Faktum<Boolean>)
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
        return Har(søknad.faktaMap()[faktum.navn] as Faktum<Boolean>)
    }
    override fun deepCopy(indeks: Int): Regel {
        return Har(faktum.tilFaktum(indeks) as Faktum<Boolean>)
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
    override fun deepCopy(søknad: Søknad): Regel {
        return Av(søknad.faktaMap()[godkjenning.navn] as Faktum<Boolean>, søknad.faktaMap()[dokument.navn] as Faktum<Dokument>)
    }
    override fun deepCopy(indeks: Int): Regel {
        return Av(godkjenning.tilFaktum(indeks) as Faktum<Boolean>, dokument.tilFaktum(indeks) as Faktum<Dokument>)
    }
}

private class AvSubsumsjon private constructor(
    regel: Regel,
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

    override fun deepCopy(søknad: Søknad) = AvSubsumsjon(
        regel.deepCopy(søknad),
        søknad.faktaMap()[dokument.navn] as Faktum<Dokument>,
        søknad.faktaMap()[godkjenning.navn] as Faktum<Boolean>,
        gyldigSubsumsjon.deepCopy(søknad),
        ugyldigSubsumsjon.deepCopy(søknad)
    )
}

infix fun Faktum<Boolean>.av(dokument: Faktum<Dokument>): Subsumsjon {
    return AvSubsumsjon(Av(this, dokument), dokument, this)
}

private class Under(private val alder: Faktum<Int>, private val maksAlder: Int) : Regel {
    override val typeNavn = this.javaClass.simpleName.toLowerCase()
    override fun resultat() = alder.svar() < maksAlder
    override fun toString() = "Sjekk at '${alder.navn}' er under $maksAlder"
    override fun deepCopy(søknad: Søknad): Regel {
        return Under(søknad.faktaMap()[alder.navn] as Faktum<Int>, maksAlder)
    }
    override fun deepCopy(indeks: Int): Regel {
        return Under(alder.tilFaktum(indeks) as Faktum<Int>, maksAlder)
    }
}

infix fun Faktum<Int>.under(maksAlder: Int): Subsumsjon {
    return EnkelSubsumsjon(
        Under(this, maksAlder),
        this
    )
}

val MAKS_DATO = UtledetFaktum<LocalDate>::max

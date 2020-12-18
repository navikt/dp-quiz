package no.nav.dagpenger.model.regel

import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.subsumsjon.GeneratorSubsumsjon
import no.nav.dagpenger.model.subsumsjon.GodkjenningsSubsumsjon
import no.nav.dagpenger.model.subsumsjon.GodkjenningsSubsumsjon.Action.JaAction
import no.nav.dagpenger.model.subsumsjon.GodkjenningsSubsumsjon.Action.NeiAction
import no.nav.dagpenger.model.subsumsjon.GodkjenningsSubsumsjon.Action.UansettAction
import no.nav.dagpenger.model.subsumsjon.MakroSubsumsjon
import no.nav.dagpenger.model.subsumsjon.MinstEnAvSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import java.time.LocalDate

interface Regel {
    val typeNavn: String
    fun saksbehandlerForklaring(fakta: List<Faktum<*>>): String = "saksbehandlerforklaring"
    fun resultat(fakta: List<Faktum<*>>): Boolean
    fun toString(fakta: List<Faktum<*>>): String
}

infix fun Faktum<LocalDate>.etter(tidligsteDato: Faktum<LocalDate>) = EnkelSubsumsjon(
    object : Regel {
        override val typeNavn = "etter"
        override fun resultat(fakta: List<Faktum<*>>) =
            (fakta[1] as Faktum<LocalDate>).svar() < (fakta[0] as Faktum<LocalDate>).svar()
        override fun toString(fakta: List<Faktum<*>>) = "Sjekk at '${fakta[0]}' er etter '${fakta[1]}'"
    },
    this,
    tidligsteDato
)

infix fun Faktum<LocalDate>.ikkeEtter(tom: Faktum<LocalDate>) = EnkelSubsumsjon(
    object : Regel {
        override val typeNavn = "ikke etter"
        override fun resultat(fakta: List<Faktum<*>>) =
            (fakta[0] as Faktum<LocalDate>).svar() <= (fakta[1] as Faktum<LocalDate>).svar()
        override fun toString(fakta: List<Faktum<*>>) = "Sjekk at '${fakta[0]}' er ikke etter '${fakta[1]}'"
    },
    this,
    tom
)

infix fun Faktum<LocalDate>.før(senesteDato: Faktum<LocalDate>) = EnkelSubsumsjon(
    object : Regel {
        override val typeNavn = "før"
        override fun resultat(fakta: List<Faktum<*>>) = (fakta[0] as Faktum<LocalDate>).svar() < (fakta[1] as Faktum<LocalDate>).svar()
        override fun toString(fakta: List<Faktum<*>>) = "Sjekk at '${fakta[0]}' er før '${fakta[1]}'"
    },
    this,
    senesteDato
)

infix fun Faktum<LocalDate>.ikkeFør(senesteDato: Faktum<LocalDate>) = EnkelSubsumsjon(
    object : Regel {
        override val typeNavn = "ikkeFør"
        override fun resultat(fakta: List<Faktum<*>>) = (fakta[0] as Faktum<LocalDate>).svar() >= (fakta[1] as Faktum<LocalDate>).svar()
        override fun toString(fakta: List<Faktum<*>>) = "Sjekk at '${fakta[0]}' ikke er før '${fakta[1]}'"
    },
    this,
    senesteDato
)

infix fun Faktum<LocalDate>.mellom(fom: Faktum<LocalDate>) = PeriodeSubsumsjonBuilder(this, fom)

class PeriodeSubsumsjonBuilder internal constructor(private val faktum: Faktum<LocalDate>, private val fom: Faktum<LocalDate>) {
    infix fun og(tom: Faktum<LocalDate>) = "periode innenfor sjekk".alle(
        faktum ikkeFør fom,
        faktum ikkeEtter tom
    )
}

infix fun Faktum<Inntekt>.minst(terskel: Faktum<Inntekt>) = EnkelSubsumsjon(
    object : Regel {
        override val typeNavn = "minst"
        override fun resultat(fakta: List<Faktum<*>>) = (fakta[0] as Faktum<Inntekt>).svar() >= (fakta[1] as Faktum<Inntekt>).svar()
        override fun toString(fakta: List<Faktum<*>>) = "Sjekk at '${fakta[0]}' er minst '${fakta[1]}'"
    },
    this,
    terskel
)

infix fun <T : Comparable<T>> Faktum<T>.er(other: T) = EnkelSubsumsjon(
    Er(other),
    this
)

private class Er<T : Comparable<T>>(private val other: T) : Regel {
    override val typeNavn = "er"
    override fun resultat(fakta: List<Faktum<*>>) = fakta[0].svar() == other
    override fun toString(fakta: List<Faktum<*>>) = "Sjekk at `${fakta[0]}` er lik $other"
}

infix fun GeneratorFaktum.med(makro: MakroSubsumsjon) = MakroSubsumsjon(
    this.navn,
    GeneratorSubsumsjon(
        Er(0),
        this,
        makro.apply {
            require(gyldig == TomSubsumsjon && ugyldig == TomSubsumsjon) {
                "Generator makroer kan ikke ha gyldig eller ugyldig stier"
            }
        },
        AlleSubsumsjon(this.navn, mutableListOf())
    )
)

infix fun GeneratorFaktum.har(makro: MakroSubsumsjon) = MakroSubsumsjon(
    this.navn,
    GeneratorSubsumsjon(
        Er(0),
        this,
        makro.apply {
            require(gyldig == TomSubsumsjon && ugyldig == TomSubsumsjon) {
                "Generator makroer kan ikke ha gyldig eller ugyldig stier"
            }
        },
        MinstEnAvSubsumsjon(this.navn, mutableListOf())
    )
)

fun erIkke(faktum: Faktum<Boolean>): Subsumsjon {
    return EnkelSubsumsjon(
        object : Regel {
            override val typeNavn = "er ikke"
            override fun resultat(fakta: List<Faktum<*>>) = !(fakta[0] as Faktum<Boolean>).svar()
            override fun toString(fakta: List<Faktum<*>>) = "Sjekk at `${fakta[0]}` ikke er sann"
        },
        faktum
    )
}

fun har(faktum: Faktum<Boolean>) = EnkelSubsumsjon(
    object : Regel {
        override val typeNavn = "har"
        override fun resultat(fakta: List<Faktum<*>>) = (fakta[0] as Faktum<Boolean>).svar()
        override fun toString(fakta: List<Faktum<*>>) = "Sjekk at `${fakta[0]}` er sann"
    },
    faktum
)

infix fun Faktum<Boolean>.av(dokument: Faktum<Dokument>): Subsumsjon =
    GodkjenningsSubsumsjon(
        JaAction,
        EnkelSubsumsjon(
            object : Regel {
                override val typeNavn = "dokumentgodkjenning"
                override fun resultat(fakta: List<Faktum<*>>) = true
                override fun toString(fakta: List<Faktum<*>>) = "Sjekk at dokument `${fakta[0]}` er opplastet"
            },
            dokument
        ),
        this as GrunnleggendeFaktum<Boolean>
    )

infix fun Faktum<Int>.under(maksAlder: Int): Subsumsjon {
    return EnkelSubsumsjon(
        Under(maksAlder),
        this
    )
}

private class Under(private val maksAlder: Int) : Regel {
    override val typeNavn = "under"
    override fun resultat(fakta: List<Faktum<*>>) = (fakta[0] as Faktum<Int>).svar() < maksAlder
    override fun toString(fakta: List<Faktum<*>>) = "Sjekk at '${fakta[0]}' er under $maksAlder"
}

infix fun Subsumsjon.godkjentAv(faktum: Faktum<Boolean>) =
    GodkjenningsSubsumsjon(UansettAction, this, faktum as GrunnleggendeFaktum<Boolean>).also {
        faktum.sjekkAvhengigheter()
    }

infix fun Subsumsjon.gyldigGodkjentAv(faktum: Faktum<Boolean>) =
    GodkjenningsSubsumsjon(JaAction, this, faktum as GrunnleggendeFaktum<Boolean>).also {
        faktum.sjekkAvhengigheter()
    }

infix fun Subsumsjon.ugyldigGodkjentAv(faktum: Faktum<Boolean>) =
    GodkjenningsSubsumsjon(NeiAction, this, faktum as GrunnleggendeFaktum<Boolean>).also {
        faktum.sjekkAvhengigheter()
    }

fun Subsumsjon.ugyldigGodkjentAv(faktum: Faktum<Boolean>, faktum2: Faktum<Boolean>) =
    GodkjenningsSubsumsjon(NeiAction, this, faktum as GrunnleggendeFaktum<Boolean>).also {
        faktum.sjekkAvhengigheter()
    }

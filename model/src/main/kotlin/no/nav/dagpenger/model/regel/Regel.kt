package no.nav.dagpenger.model.regel

import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.UtledetFaktum
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import java.time.LocalDate

interface Regel {
    val typeNavn: String
    fun resultat(): Boolean
}

infix fun Faktum<LocalDate>.etter(tidligsteDato: Faktum<LocalDate>): Subsumsjon {
    val senesteDato = this
    return EnkelSubsumsjon(
        object : Regel {
            override val typeNavn = this@etter::etter.name
            override fun resultat() = tidligsteDato.svar() < senesteDato.svar()
            override fun toString() = "Sjekk at '${senesteDato.navn}' er etter '${tidligsteDato.navn}'"
        },
        senesteDato,
        tidligsteDato
    )
}

infix fun Faktum<LocalDate>.før(senesteDato: Faktum<LocalDate>): Subsumsjon {
    val tidligsteDato = this
    return EnkelSubsumsjon(
        object : Regel {
            override val typeNavn = this@før::før.name
            override fun resultat() = tidligsteDato.svar() < senesteDato.svar()
            override fun toString() = "Sjekk at '${tidligsteDato.navn}' er før '${senesteDato.navn}'"
        },
        tidligsteDato,
        senesteDato
    )
}

infix fun Faktum<LocalDate>.ikkeFør(senesteDato: Faktum<LocalDate>): Subsumsjon {
    val tidligsteDato = this
    return EnkelSubsumsjon(
        object : Regel {
            override val typeNavn = this@ikkeFør::ikkeFør.name
            override fun resultat() = tidligsteDato.svar() >= senesteDato.svar()
            override fun toString() = "Sjekk at '${tidligsteDato.navn}' ikke er før '${senesteDato.navn}'"
        },
        tidligsteDato,
        senesteDato
    )
}

infix fun Faktum<Inntekt>.minst(terskel: Faktum<Inntekt>): Subsumsjon {
    val faktisk = this
    return EnkelSubsumsjon(
        object : Regel {
            override val typeNavn = this@minst::minst.name
            override fun resultat() = faktisk.svar() >= terskel.svar()
            override fun toString() = "Sjekk at '${faktisk.navn}' er minst '${terskel.navn}'"
        },
        faktisk,
        terskel
    )
}

infix fun <T : Comparable<T>> Faktum<T>.er(annen: T): Subsumsjon {
    val faktum = this
    return EnkelSubsumsjon(
        object : Regel {
            override val typeNavn = this@er::er.name
            override fun resultat() = faktum.svar() == annen
            override fun toString() = "Sjekk at `${faktum.navn}` er lik $annen"
        },
        faktum
    )
}

fun erIkke(faktum: Faktum<Boolean>): Subsumsjon {
    return EnkelSubsumsjon(
        object : Regel {
            override val typeNavn = ::erIkke.name
            override fun resultat() = !faktum.svar()
            override fun toString() = "Sjekk at `${faktum.navn}` ikke er sann"
        },
        faktum
    )
}

fun har(faktum: Faktum<Boolean>): Subsumsjon {
    return EnkelSubsumsjon(
        object : Regel {
            override val typeNavn = ::har.name
            override fun resultat() = faktum.svar()
            override fun toString() = "Sjekk at `${faktum.navn}` er sann"
        },
        faktum
    )
}

infix fun Faktum<Boolean>.av(dokument: Faktum<Dokument>): Subsumsjon {
    val godkjenning = this
    val regel = object : Regel {
        override val typeNavn = this@av::av.name
        override fun resultat() = dokument.erBesvart() && (!godkjenning.erBesvart() || godkjenning.svar())
        override fun toString() = "Sjekk at `${dokument.navn}` er ${if (resultat()) "godkjent" else "ikke godkjent" }"
    }
    return object : EnkelSubsumsjon(
        regel,
        godkjenning,
        dokument
    ) {
        override fun lokaltResultat(): Boolean? {
            return if (dokument.erBesvart()) regel.resultat() else null
        }

        override fun ukjenteFakta(): Set<GrunnleggendeFaktum<*>> =
            if (dokument.erBesvart()) emptySet() else dokument.grunnleggendeFakta()
    }

}

val MAKS_DATO = UtledetFaktum<LocalDate>::max

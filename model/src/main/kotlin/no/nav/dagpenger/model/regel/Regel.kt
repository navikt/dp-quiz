package no.nav.dagpenger.model.regel

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import java.time.LocalDate

interface Regel {
    fun konkluder(): Boolean
}

infix fun Faktum<LocalDate>.etter(tidligsteDato: Faktum<LocalDate>): Subsumsjon {
    val senesteDato = this
    return EnkelSubsumsjon(
        object : Regel {
            override fun konkluder() = tidligsteDato.svar() < senesteDato.svar()
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
            override fun konkluder() = tidligsteDato.svar() < senesteDato.svar()
            override fun toString() = "Sjekk at '${tidligsteDato.navn}' er før '${senesteDato.navn}'"
        },
        this,
        senesteDato
    )
}

infix fun Faktum<LocalDate>.ikkeFør(senesteDato: Faktum<LocalDate>): Subsumsjon {
    val tidligsteDato = this
    return EnkelSubsumsjon(
        object : Regel {
            override fun konkluder() = tidligsteDato.svar() >= senesteDato.svar()
            override fun toString() = "Sjekk at '${tidligsteDato.navn}' ikke er før '${senesteDato.navn}'"
        },
        this,
        senesteDato
    )
}

infix fun Faktum<Inntekt>.minst(terskel: Faktum<Inntekt>): Subsumsjon {
    val faktisk = this
    return EnkelSubsumsjon(
        object : Regel {
            override fun konkluder() = faktisk.svar() >= terskel.svar()
            override fun toString() = "Sjekk at '${faktisk.navn}' er minst '${terskel.navn}'"
        },
        this,
        terskel
    )
}

fun erIkke(faktum: Faktum<Boolean>): Subsumsjon {
    return EnkelSubsumsjon(
        object : Regel {
            override fun konkluder() = !faktum.svar()
            override fun toString() = "Sjekk at `${faktum.navn}` ikke er sann"
        }
    )
}

fun har(faktum: Faktum<Boolean>): Subsumsjon {
    return EnkelSubsumsjon(
        object : Regel {
            override fun konkluder() = faktum.svar()
            override fun toString() = "Sjekk at `${faktum.navn}` er sann"
        }
    )
}

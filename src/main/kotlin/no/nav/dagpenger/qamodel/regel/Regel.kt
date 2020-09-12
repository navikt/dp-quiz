package no.nav.dagpenger.qamodel.regel

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.fakta.Inntekt
import no.nav.dagpenger.qamodel.subsumsjon.EnkelSubsumsjon
import no.nav.dagpenger.qamodel.subsumsjon.Subsumsjon
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

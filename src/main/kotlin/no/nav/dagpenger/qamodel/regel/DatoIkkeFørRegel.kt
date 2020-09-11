package no.nav.dagpenger.qamodel.regel

import no.nav.dagpenger.qamodel.fakta.Faktum
import java.time.LocalDate

internal class DatoIkkeFørRegel(
    private val tidligsteDato: Faktum<LocalDate>,
    private val senesteDato: Faktum<LocalDate>
) : Regel {
    override fun konkluder() = tidligsteDato.svar() >= senesteDato.svar()
    override fun toString() = "Sjekk at '${tidligsteDato.navn}' ikke er før '${senesteDato.navn}'"
}

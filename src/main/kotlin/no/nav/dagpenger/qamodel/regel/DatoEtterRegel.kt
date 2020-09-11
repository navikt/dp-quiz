package no.nav.dagpenger.qamodel.regel

import no.nav.dagpenger.qamodel.fakta.Faktum
import java.time.LocalDate

internal class DatoEtterRegel(
    private val senesteDato: Faktum<LocalDate>,
    private val tidligsteDato: Faktum<LocalDate>,
) : Regel {
    override fun konkluder() = tidligsteDato.svar() < senesteDato.svar()
    override fun toString() = "Sjekk at '${senesteDato.navn}' er etter '${tidligsteDato.navn}'"
}

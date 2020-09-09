package no.nav.dagpenger.qamodel.regel

import no.nav.dagpenger.qamodel.fakta.Faktum
import java.time.LocalDate

internal object DatoIkkeFørRegel : Regel {
    override fun konkluder(fakta: List<Faktum<*>>): Boolean {
        return konkluder(fakta.first().svar() as LocalDate, fakta.last().svar() as LocalDate)
    }

    private fun konkluder(dato1: LocalDate, dato2: LocalDate) =
        !(dato1 < dato2)
}

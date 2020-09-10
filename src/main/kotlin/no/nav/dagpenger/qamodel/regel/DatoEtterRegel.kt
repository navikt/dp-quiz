package no.nav.dagpenger.qamodel.regel

import no.nav.dagpenger.qamodel.fakta.Faktum
import java.time.LocalDate

internal object DatoEtterRegel : Regel {
    override fun konkluder(fakta: Set<Faktum<*>>): Boolean {
        return konkluder(fakta.first().svar() as LocalDate, fakta.last().svar() as LocalDate)
    }

    private fun konkluder(bursdag67: LocalDate, søknadstidspunkt: LocalDate) =
        søknadstidspunkt < bursdag67
}

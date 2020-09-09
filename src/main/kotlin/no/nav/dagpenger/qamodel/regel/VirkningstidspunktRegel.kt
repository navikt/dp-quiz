package no.nav.dagpenger.qamodel.regel

import no.nav.dagpenger.qamodel.fakta.Faktum
import java.time.LocalDate

object VirkningstidspunktRegel: Regel {
    override fun konkluder(fakta: List<Faktum<*>>): Boolean {
        return konkluder(fakta.first().svar() as LocalDate, fakta.last().svar() as LocalDate)
    }

    private fun konkluder(bursdag67: LocalDate, søknadstidspunkt: LocalDate) =
            søknadstidspunkt < bursdag67

}

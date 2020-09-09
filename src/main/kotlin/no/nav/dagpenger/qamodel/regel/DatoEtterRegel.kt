package no.nav.dagpenger.qamodel.regel

import no.nav.dagpenger.qamodel.fakta.Faktum
import java.time.LocalDate

internal object DatoEtterRegel : Regel {
    override fun <R: Comparable<R>> konkluder(fakta: List<Faktum<R>>): Boolean {
        return konkluder(fakta.first().svar() as Comparable<R>, fakta.last().svar() as Comparable<R>)
    }

    private fun konkluder(bursdag67: Comparable<Any>, søknadstidspunkt: Comparable<Any>) =
        søknadstidspunkt.compareTo(bursdag67)
}

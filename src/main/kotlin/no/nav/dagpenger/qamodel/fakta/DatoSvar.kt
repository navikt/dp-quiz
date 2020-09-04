package no.nav.dagpenger.qamodel.fakta

import java.time.LocalDate

class DatoSvar(
    private val faktum: Faktum<LocalDate>,
    private val dato: LocalDate
) : Svar() {
    override fun equals(other: Any?): Boolean {
        return other is DatoSvar && this.equals(other)
    }

    private fun equals(other: DatoSvar): Boolean {
        return this.faktum == other.faktum && this.dato == other.dato
    }
}

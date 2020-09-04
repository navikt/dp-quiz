package no.nav.dagpenger.qamodel.fakta

import java.time.LocalDate
import no.nav.dagpenger.qamodel.handling.Handling

class DatoSvar(
    private val fakta: Fakta<LocalDate>,
    private val dato: LocalDate
) : Svar() {
    override fun equals(other: Any?): Boolean {
        return other is DatoSvar && this.equals(other)
    }

    private fun equals(other: DatoSvar): Boolean {
        return this.fakta == other.fakta && this.dato == other.dato
    }
}

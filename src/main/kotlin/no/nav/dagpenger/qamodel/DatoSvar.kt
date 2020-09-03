package no.nav.dagpenger.qamodel

import java.time.LocalDate

class DatoSvar(private val fakta: Fakta, private val dato: LocalDate, handling: Handling = Handling()) : Svar(handling) {
    override fun equals(other: Any?): Boolean {
        return other is DatoSvar && this.equals(other)
    }

    private fun equals(other: DatoSvar): Boolean {
        return this.fakta == other.fakta && this.dato == other.dato
    }
}

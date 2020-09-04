package no.nav.dagpenger.qamodel.fakta

import no.nav.dagpenger.qamodel.handling.Handling

class Ubesvart(private val fakta: Any, handling: Handling = Handling()) : Svar(handling) {
    override fun equals(other: Any?): Boolean {
        return other is Ubesvart && this.equals(other)
    }

    private fun equals(other: Ubesvart): Boolean {
        return this.fakta == other.fakta
    }
}

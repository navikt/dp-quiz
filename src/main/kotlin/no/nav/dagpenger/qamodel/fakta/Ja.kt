package no.nav.dagpenger.qamodel.fakta

import no.nav.dagpenger.qamodel.handling.Handling

class Ja(private val fakta: Fakta<Boolean>) : Svar() {
    override fun equals(other: Any?): Boolean {
        return other is Ja && this.equals(other)
    }

    private fun equals(other: Ja): Boolean {
        return this.fakta == other.fakta
    }
}

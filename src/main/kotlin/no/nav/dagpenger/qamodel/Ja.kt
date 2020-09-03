package no.nav.dagpenger.qamodel

class Ja(private val fakta: Fakta, handling: Handling = Handling()) : Svar(handling) {
    override fun equals(other: Any?): Boolean {
        return other is Ja && this.equals(other)
    }

    private fun equals(other: Ja): Boolean {
        return this.fakta == other.fakta
    }
}

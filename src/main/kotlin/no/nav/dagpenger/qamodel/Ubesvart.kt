package no.nav.dagpenger.qamodel

class Ubesvart(private val fakta: Any, handling: Handling = Handling()) : Svar(handling) {
    override fun equals(other: Any?): Boolean {
        return other is Ubesvart && this.equals(other)
    }

    private fun equals(other: Ubesvart): Boolean {
        return this.fakta == other.fakta
    }
}

package no.nav.dagpenger.qamodel

class Nei(private val fakta: Fakta<Boolean>, handling: Handling = Handling()) : Svar(handling) {
    override fun equals(other: Any?): Boolean {
        return other is Nei && this.equals(other)
    }

    private fun equals(other: Nei): Boolean {
        return this.fakta == other.fakta
    }
}

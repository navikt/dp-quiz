package no.nav.dagpenger.qamodel.fakta

class Nei(private val fakta: Fakta<Boolean>) : Svar() {
    override fun equals(other: Any?): Boolean {
        return other is Nei && this.equals(other)
    }

    private fun equals(other: Nei): Boolean {
        return this.fakta == other.fakta
    }
}

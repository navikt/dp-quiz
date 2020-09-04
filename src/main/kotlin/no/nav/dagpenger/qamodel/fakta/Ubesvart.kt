package no.nav.dagpenger.qamodel.fakta

class Ubesvart(private val fakta: Any) : Svar() {
    override fun equals(other: Any?): Boolean {
        return other is Ubesvart && this.equals(other)
    }

    private fun equals(other: Ubesvart): Boolean {
        return this.fakta == other.fakta
    }
}

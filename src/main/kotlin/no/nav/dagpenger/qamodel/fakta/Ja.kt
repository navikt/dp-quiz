package no.nav.dagpenger.qamodel.fakta

class Ja(private val faktum: Faktum<Boolean>) : Svar() {
    override fun equals(other: Any?): Boolean {
        return other is Ja && this.equals(other)
    }

    private fun equals(other: Ja): Boolean {
        return this.faktum == other.faktum
    }
}

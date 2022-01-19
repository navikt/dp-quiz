package no.nav.dagpenger.model.faktum

class Tekst(val verdi: String) : Comparable<Tekst> {

    companion object {
        private const val lengdeBegrensning = 2000
    }

    init {
        require(verdi.length <= lengdeBegrensning) {
            "Tekstverdien må være mindre eller lik $lengdeBegrensning, fant ${verdi.length} tegn."
        }
    }

    override fun equals(other: Any?): Boolean = other is Tekst && this.verdi == other.verdi

    override fun hashCode(): Int = verdi.hashCode()

    override fun compareTo(other: Tekst): Int = this.verdi.compareTo(other.verdi)
}

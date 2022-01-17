package no.nav.dagpenger.model.faktum

class Tekst(private val verdi: String) : Comparable<Tekst> {

    override fun equals(other: Any?): Boolean = other is Tekst && this.verdi == other.verdi

    override fun hashCode(): Int = verdi.hashCode()

    override fun compareTo(other: Tekst): Int = this.verdi.compareTo(other.verdi)
}

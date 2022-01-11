package no.nav.dagpenger.model.faktum

abstract class Valg internal constructor(private val valgteverdier: Set<String>) :
    Comparable<Valg>,
    Set<String> by valgteverdier {

    init {
        require(isNotEmpty()) { "Minst en verdi må være valgt" }
    }

    override fun compareTo(other: Valg): Int {
        return valgteverdier.size.compareTo(other.valgteverdier.size)
    }

    override fun equals(other: Any?): Boolean {
        return other is Valg && this.valgteverdier.containsAll(other.valgteverdier)
    }

    override fun hashCode(): Int {
        return valgteverdier.hashCode()
    }

    override fun toString(): String {
        return "Valg(verdier=$valgteverdier)"
    }

    abstract fun sjekk(gyldigeValg: Valg)
}

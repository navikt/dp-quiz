package no.nav.dagpenger.model.faktum

class Valg internal constructor(private val verdier: Set<String>) : Comparable<Valg>, Set<String> by verdier {

    constructor(vararg verdier: String) : this(verdier.toSet())

    override fun compareTo(other: Valg): Int {
        return verdier.size.compareTo(other.verdier.size)
    }

    override fun equals(other: Any?): Boolean {
        return other is Valg && this.verdier.containsAll(other.verdier)
    }

    override fun toString(): String {
        return "Valg(verdier=$verdier)"
    }
}

package no.nav.dagpenger.model.faktum

class Valg internal constructor(private val verdier: List<String> = mutableListOf()) : Comparable<Valg>, List<String> by verdier {

    constructor(vararg verdier: String) : this(verdier.toList())

    override fun compareTo(other: Valg): Int {
        return verdier.size.compareTo(other.verdier.size)
    }

    override fun equals(other: Any?): Boolean {
        return other is Valg && this.verdier.containsAll(other.verdier)
    }
}

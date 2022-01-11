package no.nav.dagpenger.model.faktum


class Valg internal constructor(private val valgteverdier: Set<String>) : Comparable<Valg>, Set<String> by valgteverdier {

    constructor(vararg verdier: String) : this(verdier.toSet())

    override fun compareTo(other: Valg): Int {
        return valgteverdier.size.compareTo(other.valgteverdier.size)
    }

    override fun equals(other: Any?): Boolean {
        return other is Valg && this.valgteverdier.containsAll(other.valgteverdier)
    }

    override fun toString(): String {
        return "Valg(verdier=$valgteverdier)"
    }

    fun sjekk(gyldigeValg: Valg) {
        if(isNotEmpty() && all { it in gyldigeValg }) {
            throw IllegalArgumentException("Valg $this er ikke et gyldig valg. Gyldige valg er $gyldigeValg")
        }
    }

}
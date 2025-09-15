package no.nav.dagpenger.model.faktum

abstract class Valg internal constructor(
    protected val valgteverdier: Set<String>,
) : Comparable<Valg>,
    Set<String> by valgteverdier {
    init {
        require(isNotEmpty()) { "Minst en verdi må være valgt" }
    }

    override fun compareTo(other: Valg): Int = valgteverdier.size.compareTo(other.valgteverdier.size)

    override fun toString(): String = "Valg(verdier=$valgteverdier)"
}

interface ValgteVerdier {
    fun sjekkMot(gyldigeValg: GyldigeValg)
}

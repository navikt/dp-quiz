package no.nav.dagpenger.model.faktum

class GyldigeValg(
    gyldigeValg: Set<String>,
) : Valg(gyldigeValg) {
    constructor(vararg gyldigeValg: String) : this(gyldigeValg.toSet())

    override fun equals(other: Any?): Boolean = other is GyldigeValg && this.valgteverdier.containsAll(other.valgteverdier)

    override fun hashCode(): Int = valgteverdier.hashCode()

    fun sjekk(valgteverdier: ValgteVerdier) = valgteverdier.sjekkMot(this)
}

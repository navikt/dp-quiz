package no.nav.dagpenger.model.faktum

class Flervalg(valgteverdier: Set<String>) : Valg(valgteverdier), ValgteVerdier {

    constructor(vararg verdier: String) : this(verdier.toSet())

    override fun equals(other: Any?): Boolean {
        return other is Flervalg && this.valgteverdier == other.valgteverdier
    }

    override fun hashCode(): Int {
        return valgteverdier.hashCode()
    }

    override fun sjekkMot(gyldigeValg: GyldigeValg) {
        require(all { it in gyldigeValg }) { "$this er ikke et gyldig valg. Gyldige: $gyldigeValg" }
    }
}

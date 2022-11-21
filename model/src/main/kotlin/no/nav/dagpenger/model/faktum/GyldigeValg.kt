package no.nav.dagpenger.model.faktum

class GyldigeValg(gyldigeValg: Set<String>) : Valg(gyldigeValg) {

    constructor(vararg gyldigeValg: String) : this(gyldigeValg.toSet())

    override fun equals(other: Any?): Boolean {
        return other is GyldigeValg && this.valgteverdier.containsAll(other.valgteverdier)
    }

    override fun hashCode(): Int {
        return valgteverdier.hashCode()
    }

    fun sjekk(valgteverdier: ValgteVerdier) = valgteverdier.sjekkMot(this)
}

class GyldigeValg2(gyldigeValg: Set<Int>) : Valg2(gyldigeValg) {

    constructor(vararg gyldigeValg: Int) : this(gyldigeValg.toSet())

    override fun equals(other: Any?): Boolean {
        return other is GyldigeValg2 && this.valgteverdier.containsAll(other.valgteverdier)
    }

    override fun hashCode(): Int {
        return valgteverdier.hashCode()
    }

    fun sjekk(valgteverdier: ValgteVerdier2) = valgteverdier.sjekkMot(this)
}

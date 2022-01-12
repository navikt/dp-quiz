package no.nav.dagpenger.model.faktum

class GyldigeValg(gyldigeValg: Set<String>) : Valg(gyldigeValg) {

    constructor(vararg gyldigeValg: String) : this(gyldigeValg.toSet())

    fun sjekk(valgteverdier: ValgteVerdier) = valgteverdier.sjekkMot(this)
}

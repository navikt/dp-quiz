package no.nav.dagpenger.model.faktum

class Envalg(valgteverdier: Set<String>) : Valg(valgteverdier), ValgteVerdier {

    constructor(vararg verdier: String) : this(verdier.toSet())

    override fun sjekkMot(gyldigeValg: GyldigeValg) {
        require(size == 1) { "Det er kun tillatt å velge en verdi" }
        require(all { it in gyldigeValg }) { "Valg $this er ikke et gyldig valg. Gyldige valg er $gyldigeValg" }
    }
}

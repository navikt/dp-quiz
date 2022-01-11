package no.nav.dagpenger.model.faktum

class Flervalg(valgteverdier: Set<String>) : Valg(valgteverdier) {

    constructor(vararg verdier: String) : this(verdier.toSet())

    override fun sjekk(gyldigeValg: Valg) {
        require(all { it in gyldigeValg }) { "Valg $this er ikke et gyldig valg. Gyldige valg er $gyldigeValg" }
    }
}

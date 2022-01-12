package no.nav.dagpenger.model.faktum

class GyldigeValg(gyldigeValg: Set<String>) : Valg(gyldigeValg) {

    constructor(vararg gyldigeValg: String) : this(gyldigeValg.toSet())

    override fun sjekk(gyldigeValg: Valg) {
        if (gyldigeValg == this) {
            throw IllegalArgumentException("Det gir ikke mening å sjekke mot seg selv (samme instans)")
        }
        gyldigeValg.sjekk(this)
    }

}

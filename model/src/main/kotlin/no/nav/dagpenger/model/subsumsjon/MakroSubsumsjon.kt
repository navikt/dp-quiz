package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumNavn

class MakroSubsumsjon private constructor(
    navn: String,
    private val child: Subsumsjon,
    gyldigSubsumsjon: Subsumsjon,
    ugyldigSubsumsjon: Subsumsjon
) : SammensattSubsumsjon(navn, listOf(child), gyldigSubsumsjon, ugyldigSubsumsjon) {

    internal constructor(navn: String, child: Subsumsjon) : this(navn, child, TomSubsumsjon, TomSubsumsjon)

    override fun deepCopy(faktaMap: Map<FaktumNavn, Faktum<*>>): Subsumsjon {
        return MakroSubsumsjon(
            navn,
            child.deepCopy(faktaMap),
            gyldigSubsumsjon.deepCopy(faktaMap),
            ugyldigSubsumsjon.deepCopy(faktaMap)
        )
    }

    override fun lokaltResultat() = child.resultat()
}

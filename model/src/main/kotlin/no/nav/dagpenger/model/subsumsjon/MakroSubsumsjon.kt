package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumNavn

class MakroSubsumsjon private constructor(
    navn: String,
    private val child: Subsumsjon,
    gyldigSubsumsjon: Subsumsjon,
    ugyldigSubsumsjon: Subsumsjon
) : Subsumsjon(navn, gyldigSubsumsjon, ugyldigSubsumsjon) {

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

    override fun nesteFakta() = child.nesteFakta()

    override fun enkelSubsumsjoner(vararg fakta: Faktum<*>) = child.enkelSubsumsjoner(*fakta)

    override fun fakta() = child.fakta()

    override fun get(indeks: Int): Subsumsjon {
        if (indeks != 0) throw IndexOutOfBoundsException("Makro har bare ett barn")
        return child
    }

    override fun iterator(): Iterator<Subsumsjon> {
        val iterator = listOf(child).iterator()
        return object : Iterator<Subsumsjon> {
            override fun hasNext() = iterator.hasNext()
            override fun next() = iterator.next()
        }
    }
}

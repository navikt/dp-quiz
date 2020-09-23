package no.nav.dagpenger.model.subsumsjon

import no.nav.dagpenger.model.fakta.Faktum

class MakroSubsumsjon internal constructor(navn: String, private val child: Subsumsjon) : Subsumsjon(navn) {
    override fun lokaltResultat() = child.resultat()

    override fun nesteFakta() = child.nesteFakta()

    override fun enkelSubsumsjoner(vararg fakta: Faktum<*>) = child.enkelSubsumsjoner(*fakta)

    override fun _sti(subsumsjon: Subsumsjon): List<Subsumsjon> = listOf(this) + child._sti(subsumsjon)

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

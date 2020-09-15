package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

interface Faktum<R: Any> {
    val navn: String

    infix fun besvar(r: R): Faktum<R>
    fun svar(): R
    fun leggTilHvis(kode: FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>)
    fun erBesvart(): Boolean
    fun accept(visitor: SubsumsjonVisitor)

    enum class FaktumTilstand {
        Ukjent,
        Kjent
    }
}

fun <R: Comparable<R>> Set<Faktum<R>>.faktum(navn: String, regel: FaktaRegel<R>): Faktum<R> = SammensattFaktum(navn, this, regel)
fun Set<Faktum<*>>.erBesvart() = this.all { it.erBesvart() }

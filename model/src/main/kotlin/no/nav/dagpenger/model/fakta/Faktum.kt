package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

interface Faktum<R : Comparable<R>> {
    val navn: String

    fun besvar(r: R, rolle: Rolle = Rolle.søker): Faktum<R>
    fun svar(): R
    fun grunnleggendeFakta(): Set<GrunnleggendeFaktum<*>>
    fun leggTilHvis(kode: FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>)
    fun erBesvart(): Boolean
    fun accept(visitor: SubsumsjonVisitor)

    enum class FaktumTilstand {
        Ukjent,
        Kjent
    }
}

fun <R : Comparable<R>> Collection<Faktum<R>>.faktum(navn: String, regel: FaktaRegel<R>): Faktum<R> =
    UtledetFaktum(navn, this.toSet(), regel)

fun Set<Faktum<*>>.erBesvart() = this.all { it.erBesvart() }
typealias FaktaRegel <R> = (UtledetFaktum<R>) -> R

fun <R : Comparable<R>> String.faktum(vararg roller: Rolle) = GrunnleggendeFaktum<R>(this, roller.toMutableSet())

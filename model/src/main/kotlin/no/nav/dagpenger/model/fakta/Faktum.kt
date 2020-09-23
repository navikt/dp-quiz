package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.visitor.FaktumVisitor

interface Faktum<R : Comparable<R>> {
    val navn: FaktumNavn
    val id: Int get() = navn.id
    val avhengigeFakta: MutableList<Faktum<*>>

    fun clazz(): Class<R>

    fun besvar(r: R, rolle: Rolle = Rolle.søker): Faktum<R> = this.also {
        avhengigeFakta.forEach {
            it.tilUbesvart()
        }
    }
    fun tilUbesvart()
    fun svar(): R
    fun grunnleggendeFakta(): Set<GrunnleggendeFaktum<*>>
    fun leggTilHvis(kode: FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>)
    fun erBesvart(): Boolean
    fun accept(visitor: FaktumVisitor)
    fun add(rolle: Rolle): Boolean
    infix fun avhengerAv(other: Faktum<*>) {
        other.avhengigeFakta.add(this)
    }

    fun faktaMap(): Map<FaktumNavn, Faktum<*>>

    enum class FaktumTilstand {
        Ukjent,
        Kjent
    }
}

fun <R : Comparable<R>> Collection<Faktum<R>>.faktum(navn: FaktumNavn, regel: FaktaRegel<R>): Faktum<R> =
    UtledetFaktum(navn, this.toSet(), regel)

fun Set<Faktum<*>>.erBesvart() = this.all { it.erBesvart() }
typealias FaktaRegel <R> = (UtledetFaktum<R>) -> R

fun <R : Comparable<R>> FaktumNavn.faktum(clazz: Class<R>) = GrunnleggendeFaktum<R>(this, clazz)

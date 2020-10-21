package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.visitor.FaktumVisitor

class UtledetFaktum<R : Comparable<R>> internal constructor(
    faktumId: FaktumId,
    navn: String,
    private val childFakta: MutableSet<Faktum<R>>,
    private val regel: FaktaRegel<R>
) : Faktum<R>(faktumId, navn) {

    internal fun max(): R = childFakta.maxOf { it.svar() }
    internal fun alle(): Boolean = childFakta.all { it.svar() as Boolean }

    override fun clazz() = childFakta.toList().first().clazz()

    override fun besvar(r: R, rolle: Rolle): Faktum<R> {
        throw IllegalArgumentException("Kan ikke besvare sammensatte faktum")
    }

    override fun svar(): R {
        childFakta.forEach { it.svar() }
        return regel(this)
    }

    internal fun addAll(fakta: List<Faktum<*>>) = this.childFakta.addAll(fakta as List<Faktum<R>>)

    override fun add(rolle: Rolle): Boolean = false // utledet faktum kan ikke settes av roller

    override fun grunnleggendeFakta(): Set<GrunnleggendeFaktum<*>> = childFakta.flatMap { it.grunnleggendeFakta() }.toSet()

    override fun leggTilHvis(kode: Faktum.FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>) {
        this.childFakta.forEach { it.leggTilHvis(kode, fakta) }
    }

    override fun erBesvart() = childFakta.all { it.erBesvart() }

    override fun accept(visitor: FaktumVisitor) {
        faktumId.accept(visitor)
        if (erBesvart()) visitor.preVisit(this, id, avhengigeFakta, childFakta, clazz(), svar())
        else visitor.preVisit(this, id, avhengigeFakta, childFakta, clazz())
        childFakta.forEach { it.accept(visitor) }
        visitor.postVisit(this, id, childFakta, clazz())
    }

    override fun bygg(byggetFakta: MutableMap<FaktumId, Faktum<*>>): UtledetFaktum<R> {
        if (byggetFakta.containsKey(faktumId)) return byggetFakta[faktumId] as UtledetFaktum<R>
        val childFakta = childFakta.map { it.bygg(byggetFakta) as Faktum<R> }.toMutableSet()
        return UtledetFaktum(faktumId, navn, childFakta, regel).also { byggetFakta[faktumId] = it }
    }

    internal fun child(faktaList: List<Faktum<*>>, indeks: Int) = childFakta.all { faktum ->
        faktaList.indexOf(faktum) < indeks
    }
}

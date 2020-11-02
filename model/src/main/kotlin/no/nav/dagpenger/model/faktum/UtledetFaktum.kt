package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.factory.FaktaRegel
import no.nav.dagpenger.model.visitor.FaktumVisitor

class UtledetFaktum<R : Comparable<R>> internal constructor(
    faktumId: FaktumId,
    navn: String,
    private val underordnede: MutableSet<Faktum<R>>,
    private val regel: FaktaRegel<R>
) : Faktum<R>(faktumId, navn) {

    internal fun max(): R = underordnede.maxOf { it.svar() }
    internal fun alle(): Boolean = underordnede.all { it.svar() as Boolean }
    internal fun valg(): Boolean = underordnede.all { it.svar() as Boolean }

    override fun clazz() = underordnede.toList().first().clazz()

    override fun besvar(r: R, rolle: Rolle): Faktum<R> {
        throw IllegalArgumentException("Kan ikke besvare sammensatte faktum")
    }

    override fun svar(): R {
        underordnede.forEach { it.svar() }
        return regel.strategy(this)
    }

    internal fun addAll(fakta: List<Faktum<*>>) = this.underordnede.addAll(fakta as List<Faktum<R>>)

    override fun add(rolle: Rolle): Boolean = false // utledet faktum kan ikke settes av roller

    override fun grunnleggendeFakta(): Set<GrunnleggendeFaktum<*>> =
        underordnede.flatMap { it.grunnleggendeFakta() }.toSet()

    override fun leggTilHvis(kode: Faktum.FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>) {
        this.underordnede.forEach { it.leggTilHvis(kode, fakta) }
    }

    override fun erBesvart() = underordnede.all { it.erBesvart() }

    override fun accept(visitor: FaktumVisitor) {
        faktumId.accept(visitor)
        if (erBesvart()) visitor.preVisit(
            this,
            id,
            avhengigeFakta,
            avhengerAvFakta,
            underordnede,
            clazz(),
            regel,
            svar()
        )
        else visitor.preVisit(this, id, avhengigeFakta, avhengerAvFakta, underordnede, clazz(), regel)
        underordnede.forEach { it.accept(visitor) }
        visitor.postVisit(this, id, underordnede, clazz())
    }

    override fun bygg(byggetFakta: MutableMap<FaktumId, Faktum<*>>): UtledetFaktum<R> {
        if (byggetFakta.containsKey(faktumId)) return byggetFakta[faktumId] as UtledetFaktum<R>
        val childFakta = underordnede.map { it.bygg(byggetFakta) as Faktum<R> }.toMutableSet()
        return UtledetFaktum(faktumId, navn, childFakta, regel).also { byggetFakta[faktumId] = it }
    }

    internal fun erDefinert(faktaList: List<Faktum<*>>, indeks: Int) = underordnede.all { faktum ->
        faktaList.indexOf(faktum) < indeks
    }
}
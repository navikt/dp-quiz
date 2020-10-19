package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.visitor.FaktumVisitor

class UtledetFaktum<R : Comparable<R>> internal constructor(
    override val faktumNavn: FaktumNavn,
    private val fakta: Set<Faktum<R>>,
    private val regel: FaktaRegel<R>
) : Faktum<R>() {

    internal fun max(): R = fakta.maxOf { it.svar() }
    internal fun alle(): Boolean = fakta.all { it.svar() as Boolean }

    override fun clazz() = fakta.toList().first().clazz()

    override fun besvar(r: R, rolle: Rolle): Faktum<R> {
        throw IllegalArgumentException("Kan ikke besvare sammensatte faktum")
    }

    override fun svar(): R {
        fakta.forEach { it.svar() }
        return regel(this)
    }

    override fun add(rolle: Rolle): Boolean = false // utledet faktum kan ikke settes av roller

    override fun grunnleggendeFakta(): Set<GrunnleggendeFaktum<*>> = fakta.flatMap { it.grunnleggendeFakta() }.toSet()

    override fun leggTilHvis(kode: Faktum.FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>) {
        this.fakta.forEach { it.leggTilHvis(kode, fakta) }
    }

    override fun erBesvart() = fakta.all { it.erBesvart() }

    override fun accept(visitor: FaktumVisitor) {
        faktumNavn.accept(visitor)
        if (erBesvart()) visitor.preVisit(this, id, avhengigeFakta, fakta, clazz(), svar())
        else visitor.preVisit(this, id, avhengigeFakta, fakta, clazz())
        fakta.forEach { it.accept(visitor) }
        visitor.postVisit(this, id, fakta, clazz())
    }

    override fun toString() = faktumNavn.toString()
}

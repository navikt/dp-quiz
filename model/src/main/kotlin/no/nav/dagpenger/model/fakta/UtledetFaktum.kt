package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.visitor.SubsumsjonVisitor

class UtledetFaktum<R : Comparable<R>> internal constructor(
    override val navn: String,
    private val fakta: Set<Faktum<R>>,
    private val regel: FaktaRegel<R>
) : Faktum<R> {
    internal val max: R get() = fakta.maxOf { it.svar() }

    override fun besvar(r: R, rolle: Rolle): Faktum<R> {
        throw IllegalArgumentException("Kan ikke besvare sammensatte faktum")
    }

    override fun svar(): R {
        fakta.forEach { it.svar() }
        return regel(this)
    }

    override fun grunnleggendeFakta(): Set<GrunnleggendeFaktum<*>> = fakta.flatMap { it.grunnleggendeFakta() }.toSet()

    override fun leggTilHvis(kode: Faktum.FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>) {
        this.fakta.forEach { it.leggTilHvis(kode, fakta) }
    }

    override fun erBesvart() = fakta.all { it.erBesvart() }

    override fun accept(visitor: SubsumsjonVisitor) {
        if (erBesvart()) visitor.preVisit(this, svar()) else visitor.preVisit(this)
        visitor.preVisit(fakta)
        fakta.forEach { it.accept(visitor) }
        visitor.postVisit(fakta)
        visitor.postVisit(this)
    }
}

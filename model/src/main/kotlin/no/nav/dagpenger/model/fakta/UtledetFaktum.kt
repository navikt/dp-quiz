package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.visitor.FaktumVisitor

class UtledetFaktum<R : Comparable<R>> internal constructor(
    override val navn: FaktumNavn,
    private val fakta: Set<Faktum<R>>,
    private val regel: FaktaRegel<R>
) : Faktum<R> {
    override val avhengigeFakta = mutableSetOf<Faktum<*>>()

    internal fun max(): R = fakta.maxOf { it.svar() }

    override fun clazz() = fakta.toList().first().clazz()

    override fun besvar(r: R, rolle: Rolle): Faktum<R> {
        throw IllegalArgumentException("Kan ikke besvare sammensatte faktum")
    }

    override fun faktaMap(): Map<FaktumNavn, Faktum<*>> {
        return mapOf(navn to this) + fakta.fold(mapOf<FaktumNavn, Faktum<*>> ()) { resultater, faktum ->
            resultater + faktum.faktaMap()
        }
    }

    override fun svar(): R {
        fakta.forEach { it.svar() }
        return regel(this)
    }

    override fun add(rolle: Rolle) = fakta.all { it.add(rolle) }

    override fun grunnleggendeFakta(): Set<GrunnleggendeFaktum<*>> = fakta.flatMap { it.grunnleggendeFakta() }.toSet()

    override fun leggTilHvis(kode: Faktum.FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>) {
        this.fakta.forEach { it.leggTilHvis(kode, fakta) }
    }

    override fun erBesvart() = fakta.all { it.erBesvart() }

    override fun tilUbesvart() {
        throw IllegalStateException("Kan ikke sette utleda faktum til ubesvart")
    }

    override fun accept(visitor: FaktumVisitor) {
        navn.accept(visitor)
        if (erBesvart()) visitor.preVisit(this, id, avhengigeFakta, fakta, clazz(), svar())
        else visitor.preVisit(this, id, avhengigeFakta, fakta, clazz())
        fakta.forEach { it.accept(visitor) }
        visitor.postVisit(this, id, fakta, clazz())
    }

    override fun toString() = navn.toString()
}

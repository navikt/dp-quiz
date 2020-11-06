package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.visitor.FaktumVisitor

class ValgFaktum internal constructor(
    faktumId: FaktumId,
    navn: String,
    private val underordnedeJa: MutableSet<Faktum<Boolean>>,
    private val underordnedeNei: MutableSet<Faktum<Boolean>>
) : Faktum<Boolean>(faktumId, navn) {
    private val underordnede get() = underordnedeJa + underordnedeNei

    override fun clazz() = Boolean::class.java

    override fun besvar(r: Boolean): Faktum<Boolean> {
        throw IllegalArgumentException("Kan ikke besvare valgfaktum")
    }

    override fun svar(): Boolean {
        if (underordnedeJa.any { it.erBesvart() }) return true
        if (underordnedeNei.any { it.erBesvart() }) return false
        throw IllegalStateException("Ingen svaralternativ er valgt")
    }

    internal fun addAllJa(fakta: List<Faktum<*>>) = underordnedeJa.addAll(fakta as List<Faktum<Boolean>>)
    internal fun addAllNei(fakta: List<Faktum<*>>) = underordnedeNei.addAll(fakta as List<Faktum<Boolean>>)

    override fun add(rolle: Rolle) = underordnede.all { it.add(rolle) }

    override fun grunnleggendeFakta(): Set<GrunnleggendeFaktum<*>> =
        underordnede.flatMap { it.grunnleggendeFakta() }.toSet()

    override fun leggTilHvis(kode: Faktum.FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>) {
        underordnede.forEach { it.leggTilHvis(kode, fakta) }
    }

    override fun erBesvart() = underordnedeJa.any { it.erBesvart() } || underordnedeNei.any { it.erBesvart() }

    override fun accept(visitor: FaktumVisitor) {
        faktumId.accept(visitor)
        if (erBesvart()) visitor.preVisit(
            this,
            id,
            avhengigeFakta,
            avhengerAvFakta,
            underordnedeJa,
            underordnedeNei,
            clazz(),
            svar()
        )
        else visitor.preVisit(this, id, avhengigeFakta, avhengerAvFakta, underordnedeJa, underordnedeNei, clazz())
        underordnede.forEach { it.accept(visitor) }
        visitor.postVisit(this, id, underordnedeJa, underordnedeNei, clazz())
    }

    override fun bygg(byggetFakta: MutableMap<FaktumId, Faktum<*>>): ValgFaktum {
        if (byggetFakta.containsKey(faktumId)) return byggetFakta[faktumId] as ValgFaktum
        val underordnedeJa = this.underordnedeJa.map { it.bygg(byggetFakta) as Faktum<Boolean> }.toMutableSet()
        val underordnedeNei = this.underordnedeNei.map { it.bygg(byggetFakta) as Faktum<Boolean> }.toMutableSet()
        return ValgFaktum(faktumId, navn, underordnedeJa, underordnedeNei).also { byggetFakta[faktumId] = it }
    }
}

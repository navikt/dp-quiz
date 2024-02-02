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
    internal fun min(): R = underordnede.minOf { it.svar() }
    internal fun multiplikasjon(): Inntekt =
        underordnede.first().svar() as Number * underordnede.last().svar() as Inntekt

    internal fun alle(): Boolean = underordnede.all { it.svar() as Boolean }

    override fun type() = underordnede.toList().first().type()

    override fun besvar(verdi: R, identer: String?): Faktum<R> {
        throw IllegalArgumentException("Kan ikke besvare sammensatte faktum")
    }

    override fun besvartAv(): String? = null

    override fun rehydrer(r: R, besvarer: String?): Faktum<R> {
        throw IllegalArgumentException("Kan ikke rehydrere sammensatte faktum")
    }

    override fun svar(): R {
        underordnede.forEach { it.svar() }
        return regel.strategy(this)
    }

    internal fun addAll(fakta: List<Faktum<*>>) = fakta.forEach { underordnet ->
        underordnede.add(underordnet as Faktum<R>)
        avhengigeFakta.forEach { avhengig ->
            underordnet.leggTilAvhengighet(avhengig)
        }
    }

    override fun leggTilAvhengighet(other: Faktum<*>) {
        super.leggTilAvhengighet(other)
        underordnede.forEach { it.leggTilAvhengighet(other) }
    }

    override fun grunnleggendeFakta(): Set<GrunnleggendeFaktum<*>> =
        underordnede.flatMap { it.grunnleggendeFakta() }.toSet()

    override fun leggTilHvis(kode: FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>) {
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
            type(),
            regel,
            svar()
        )
        else visitor.preVisit(this, id, avhengigeFakta, avhengerAvFakta, underordnede, type(), regel)
        underordnede.forEach { it.accept(visitor) }
        visitor.postVisit(this, id, underordnede, type())
    }

    override fun bygg(byggetFakta: MutableMap<FaktumId, Faktum<*>>): UtledetFaktum<R> {
        val childFakta = underordnede.map { it.bygg(byggetFakta) as Faktum<R> }.toMutableSet()

        if (byggetFakta.containsKey(faktumId)) return byggetFakta[faktumId] as UtledetFaktum<R>
        return UtledetFaktum(faktumId, navn, childFakta, regel).also { utledetFaktum ->
            byggetFakta[faktumId] = utledetFaktum
            this.avhengigeFakta.forEach { utledetFaktum.avhengigeFakta.add(it.bygg(byggetFakta)) }
            this.avhengerAvFakta.forEach { utledetFaktum.avhengerAvFakta.add(it.bygg(byggetFakta)) }
        }
    }

    internal fun erDefinert(faktaList: List<Faktum<*>>, indeks: Int) = underordnede.all { faktum ->
        faktaList.indexOf(faktum) < indeks
    }
}

package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.visitor.FaktumVisitor

class TemplateFaktum<R: Comparable<R>> internal constructor(override val navn: FaktumNavn, internal val clazz: Class<R>): Faktum<R> {

    override val avhengigeFakta = mutableSetOf<Faktum<*>>()
    private val roller = mutableSetOf<Rolle>()

    override fun clazz() = clazz

    override fun tilUbesvart() {
        //Ignorert
    }

    override fun svar(): R {
        throw IllegalStateException("Templates har ikke svar")
    }

    override fun grunnleggendeFakta(): Set<GrunnleggendeFaktum<*>> = emptySet()

    override fun leggTilHvis(kode: Faktum.FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>) {
        //Ignorert
    }

    override fun erBesvart() = false

    override fun accept(visitor: FaktumVisitor) {
        visitor.visit(this, id, avhengigeFakta, roller, clazz)
    }

    override fun add(rolle: Rolle) = roller.add(rolle)

    override fun faktaMap() = mapOf(navn to this)

}
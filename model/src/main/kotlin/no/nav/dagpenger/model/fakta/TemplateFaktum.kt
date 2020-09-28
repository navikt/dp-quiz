package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.visitor.FaktumVisitor

class TemplateFaktum<R: Comparable<R>> internal constructor(navn: FaktumNavn, clazz: Class<R>): Faktum<R> {
    override val navn: FaktumNavn
        get() = TODO("Not yet implemented")

    override val avhengigeFakta: MutableSet<Faktum<*>>
        get() = TODO("Not yet implemented")

    override fun clazz(): Class<R> {
        TODO("Not yet implemented")
    }

    override fun tilUbesvart() {
        TODO("Not yet implemented")
    }

    override fun svar(): R {
        TODO("Not yet implemented")
    }

    override fun grunnleggendeFakta(): Set<GrunnleggendeFaktum<*>> {
        TODO("Not yet implemented")
    }

    override fun leggTilHvis(kode: Faktum.FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>) {
        TODO("Not yet implemented")
    }

    override fun erBesvart(): Boolean {
        TODO("Not yet implemented")
    }

    override fun accept(visitor: FaktumVisitor) {
        TODO("Not yet implemented")
    }

    override fun add(rolle: Rolle): Boolean {
        TODO("Not yet implemented")
    }

    override fun faktaMap(): Map<FaktumNavn, Faktum<*>> {
        TODO("Not yet implemented")
    }

}
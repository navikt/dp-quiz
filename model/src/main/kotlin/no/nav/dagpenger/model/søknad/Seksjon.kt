package no.nav.dagpenger.model.søknad

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.visitor.SøknadVisitor

class Seksjon(private val rolle: Rolle, vararg fakta: Faktum<*>) : MutableSet<Faktum<*>> by fakta.toFaktaSet() {
    private val fakta = fakta.toMutableSet()

    init {
        fakta.toFaktaSet().forEach {
            it.add(rolle)
            it.add(this)
        }
    }

    internal operator fun contains(nesteFakta: Set<GrunnleggendeFaktum<*>>): Boolean {
        return nesteFakta.any { it in fakta.toFaktaSet() }
    }

    fun accept(visitor: SøknadVisitor) {
        visitor.preVisit(this, rolle, fakta)
        fakta.forEach { it.accept(visitor) }
        visitor.postVisit(this, rolle)
    }

    internal fun faktaMap(): Map<FaktumNavn, Faktum<*>> {
        return fakta.fold(mapOf()) { resultater, faktum ->
            resultater + faktum.faktaMap()
        }
    }
}

private fun <T : Faktum<*>> Array<T>.toFaktaSet(): MutableSet<GrunnleggendeFaktum<*>> =
    this.flatMap { it.grunnleggendeFakta() }.toMutableSet()

private fun <T : Faktum<*>> Set<T>.toFaktaSet(): MutableSet<GrunnleggendeFaktum<*>> =
    this.flatMap { it.grunnleggendeFakta() }.toMutableSet()

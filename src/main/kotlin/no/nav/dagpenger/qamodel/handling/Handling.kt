package no.nav.dagpenger.qamodel.handling

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.visitor.FaktumVisitor

abstract class Handling<R>(vararg fakta: Faktum<*>) {
    private val fakta: List<Faktum<*>> = fakta.toList()

    fun nesteSpørsmål() {
        fakta.forEach { it.spør() }
    }

    open fun utfør(r: R) {}

    internal fun accept(visitor: FaktumVisitor) {
        fakta.forEach { it.accept(visitor) }
    }
}

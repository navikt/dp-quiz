package no.nav.dagpenger.qamodel.handling

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.fakta.FaktumVisitor

abstract class Handling(vararg fakta: Faktum<*>) {
    private val fakta: List<Faktum<*>> = fakta.toList()

    fun nesteSpørsmål() {
        fakta.forEach { it.spør() }
    }

    open fun utfør() {}
    internal fun accept(visitor: FaktumVisitor) {
        fakta.forEach { it.accept(visitor) }
    }
}

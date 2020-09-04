package no.nav.dagpenger.qamodel.handling

import no.nav.dagpenger.qamodel.fakta.Faktum

abstract class Handling(
    vararg fakta: Faktum<*>
) {
    private val fakta: List<Faktum<*>> = fakta.toList()

    fun nesteSpørsmål() {
        fakta.forEach { it.spør() }
    }
}

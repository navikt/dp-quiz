package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.factory.FaktumFactory

interface DslFaktaseksjon {

    val fakta: List<FaktumFactory<*>>
    val alleVariabler: List<Int>

    fun fakta(): Array<FaktumFactory<*>> = fakta.toTypedArray()
    fun variabler(): IntArray = alleVariabler.toIntArray()
}

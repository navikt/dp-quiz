package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.factory.FaktumFactory

object Bostedsland {
    const val `hvilket land bor du i` = 6001

    private val fakta = listOf<FaktumFactory<*>>(
        envalg faktum "faktum.hvilket-land-bor-du-i"
            med "" id `hvilket land bor du i`
    )

    private val alleVariabler = listOf(
        `hvilket land bor du i`
    )

    fun fakta(): Array<FaktumFactory<*>> = fakta.toTypedArray()
    fun variabler(): IntArray = alleVariabler.toIntArray()
}

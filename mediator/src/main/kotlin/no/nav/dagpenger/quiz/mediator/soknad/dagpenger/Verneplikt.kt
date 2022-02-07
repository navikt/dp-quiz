package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.FaktumFactory

object Verneplikt {
    const val `avtjent militaer sivilforsvar tjeneste siste 12 mnd` = 7001

    private val fakta = listOf<FaktumFactory<*>>(
        boolsk faktum "faktum.avtjent-militaer-sivilforsvar-tjeneste-siste-12-mnd" id `avtjent militaer sivilforsvar tjeneste siste 12 mnd`
    )

    private val alleVariabler = listOf(
        `avtjent militaer sivilforsvar tjeneste siste 12 mnd`
    )

    fun fakta(): Array<FaktumFactory<*>> = fakta.toTypedArray()
    fun variabler(): IntArray = alleVariabler.toIntArray()
}

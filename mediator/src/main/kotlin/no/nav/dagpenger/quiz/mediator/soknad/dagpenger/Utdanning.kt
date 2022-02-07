package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory
import no.nav.dagpenger.model.factory.FaktumFactory

object Utdanning {
    const val utdanning = 2001

    private val fakta = listOf<FaktumFactory<*>>(
        BaseFaktumFactory.Companion.envalg faktum "faktum.utdanning"
            med "svar.nei"
            med "svar.nei-men-siste-6-mnd"
            med "svar.ja" id utdanning
    )

    private val alleVariabler = listOf(
        utdanning
    )

    fun fakta(): Array<FaktumFactory<*>> = fakta.toTypedArray()
    fun variabler(): IntArray = alleVariabler.toIntArray()
}

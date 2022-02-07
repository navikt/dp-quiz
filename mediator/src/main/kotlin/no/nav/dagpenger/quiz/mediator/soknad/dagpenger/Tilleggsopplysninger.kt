package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.factory.FaktumFactory

object Tilleggsopplysninger {
    const val tilleggsopplysninger = 4001

    private val fakta = listOf<FaktumFactory<*>>(
        tekst faktum "faktum.tilleggsopplysninger" id tilleggsopplysninger
    )

    private val alleVariabler = listOf(
        tilleggsopplysninger
    )

    fun fakta(): Array<FaktumFactory<*>> = fakta.toTypedArray()
    fun variabler(): IntArray = alleVariabler.toIntArray()
}

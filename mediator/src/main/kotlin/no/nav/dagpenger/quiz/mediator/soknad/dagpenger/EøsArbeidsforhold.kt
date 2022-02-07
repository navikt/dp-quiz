package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.FaktumFactory

object EøsArbeidsforhold {
    const val `eos arbeid siste 36 mnd` = 9001
    const val `eos arbeidsforhold` = 9002

    private val fakta = listOf<FaktumFactory<*>>(
        boolsk faktum "faktum.eos-arbeid-siste-36-mnd" id `eos arbeid siste 36 mnd`,
        heltall faktum "faktum.eos-arbeidsforhold" id `eos arbeidsforhold`
        // TODO: Generere arbeidsforhold
    )

    private val alleVariabler = listOf(
        `eos arbeid siste 36 mnd`,
        `eos arbeidsforhold`
    )

    fun fakta(): Array<FaktumFactory<*>> = fakta.toTypedArray()
    fun variabler(): IntArray = alleVariabler.toIntArray()
}

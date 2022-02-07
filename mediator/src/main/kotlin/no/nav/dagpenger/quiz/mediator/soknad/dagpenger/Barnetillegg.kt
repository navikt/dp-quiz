package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.factory.FaktumFactory

object Barnetillegg {
    const val `barn liste` = 1001
    const val `barn fornavn mellomnavn` = 1002
    const val `barn etternavn` = 1003
    const val `barn foedselsdato` = 1004
    const val `barn statsborgerskap` = 1005
    const val `forsoerger du barnet` = 1006
    const val `barn aarsinntekt over 1g` = 1007
    const val `barn inntekt` = 1008

    private val fakta = listOf<FaktumFactory<*>>(
        boolsk faktum "faktum.barn-aarsinntekt-over-1g" id `barn aarsinntekt over 1g`,
        heltall faktum "faktum.barn-inntekt" id `barn inntekt`,
        heltall faktum "faktum.barn-liste" id `barn liste`
            genererer `barn fornavn mellomnavn`
            og `barn etternavn`
            og `barn foedselsdato`
            og `barn statsborgerskap`
            og `forsoerger du barnet`,
        tekst faktum "faktum.barn-fornavn-mellomnavn" id `barn fornavn mellomnavn`,
        tekst faktum "faktum.barn-etternavn" id `barn etternavn`,
        dato faktum "faktum.barn-foedselsdato" id `barn foedselsdato`,
        envalg faktum "faktum.barn-statsborgerskap"
            med "NOR" id `barn statsborgerskap`,
        boolsk faktum "faktum.forsoerger-du-barnet" id `forsoerger du barnet`
    )

    private val alleVariabler = listOf(
        `barn liste`,
        `barn fornavn mellomnavn`,
        `barn etternavn`,
        `barn foedselsdato`,
        `barn statsborgerskap`,
        `forsoerger du barnet`,
        `barn aarsinntekt over 1g`,
        `barn inntekt`,
    )

    fun fakta(): Array<FaktumFactory<*>> = fakta.toTypedArray()
    fun variabler(): IntArray = alleVariabler.toIntArray()
}

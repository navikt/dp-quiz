package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.FaktumFactory

object KoronaFortsattRett {
    const val `oppbrukt dagpengeperiode` = 10001
    const val `onsker fortsette avsluttet periode` = 10002

    private val fakta = listOf<FaktumFactory<*>>(
        boolsk faktum "faktum.oppbrukt-dagpengeperiode" id `oppbrukt dagpengeperiode`,
        boolsk faktum "faktum.onsker-fortsette-avsluttet-periode" id `onsker fortsette avsluttet periode`
    )

    private val alleVariabler = listOf(
        `oppbrukt dagpengeperiode`,
        `onsker fortsette avsluttet periode`
    )

    fun fakta(): Array<FaktumFactory<*>> = fakta.toTypedArray()
    fun variabler(): IntArray = alleVariabler.toIntArray()
}

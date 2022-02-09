package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object KoronaFortsattRett : DslFaktaseksjon {
    const val `oppbrukt dagpengeperiode` = 10001
    const val `onsker fortsette avsluttet periode` = 10002

    override val fakta = listOf(
        boolsk faktum "faktum.oppbrukt-dagpengeperiode" id `oppbrukt dagpengeperiode`,
        boolsk faktum "faktum.onsker-fortsette-avsluttet-periode" id `onsker fortsette avsluttet periode`
    )
}

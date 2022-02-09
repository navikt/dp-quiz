package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Bostedsland : DslFaktaseksjon {
    const val `hvilket land bor du i` = 6001

    override val fakta = listOf(
        envalg faktum "faktum.hvilket-land-bor-du-i"
            med "" id `hvilket land bor du i`
    )
}

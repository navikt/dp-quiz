package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.land
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Bostedsland : DslFaktaseksjon {
    const val `hvilket land bor du i` = 6001

    override val fakta = listOf(
        land faktum "faktum.hvilket-land-bor-du-i" id `hvilket land bor du i`
    )
}

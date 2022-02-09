package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Verneplikt : DslFaktaseksjon {
    const val `avtjent militaer sivilforsvar tjeneste siste 12 mnd` = 7001

    override val fakta = listOf(
        boolsk faktum "faktum.avtjent-militaer-sivilforsvar-tjeneste-siste-12-mnd" id `avtjent militaer sivilforsvar tjeneste siste 12 mnd`
    )
}

package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Utdanning : DslFaktaseksjon {

    const val `tar du utdanning` = 2001
    const val `avsluttet utdanning siste 6 mnd` = 2002
    const val `planlegger utdanning med dagpenger` = 2003

    override val fakta = listOf(
        boolsk faktum "faktum.tar-du-utdanning" id `tar du utdanning`,
        boolsk faktum "faktum.avsluttet-utdanning-siste-6-mnd" id `avsluttet utdanning siste 6 mnd`,
        boolsk faktum "faktum.planlegger-utdanning-med-dagpenger" id `planlegger utdanning med dagpenger`
    )
}

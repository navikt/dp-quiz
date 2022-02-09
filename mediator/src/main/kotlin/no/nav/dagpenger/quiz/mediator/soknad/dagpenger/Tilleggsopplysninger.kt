package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Tilleggsopplysninger : DslFaktaseksjon {
    const val tilleggsopplysninger = 4001

    override val fakta = listOf(
        tekst faktum "faktum.tilleggsopplysninger" id tilleggsopplysninger
    )
}

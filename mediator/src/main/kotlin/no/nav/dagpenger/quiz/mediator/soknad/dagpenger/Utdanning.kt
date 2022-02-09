package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Utdanning : DslFaktaseksjon {
    const val utdanning = 2001

    override val fakta = listOf(
        envalg faktum "faktum.utdanning"
            med "svar.nei"
            med "svar.nei-men-siste-6-mnd"
            med "svar.ja" id utdanning
    )
}

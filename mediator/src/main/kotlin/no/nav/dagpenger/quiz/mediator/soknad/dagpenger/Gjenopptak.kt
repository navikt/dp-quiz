package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Gjenopptak : DslFaktaseksjon {
    const val `mottatt dagpenger siste 12 mnd` = 10001

    override val fakta = listOf(
        envalg faktum "faktum.mottatt-dagpenger-siste-12-mnd"
            med "svar.ja"
            med "svar.nei"
            med "svar.vet-ikke" id `mottatt dagpenger siste 12 mnd`
    )

    override fun seksjon(søknad: Søknad) = listOf(søknad.seksjon("gjenopptak", Rolle.søker, *this.databaseIder()))

    fun regeltre(søknad: Søknad): Subsumsjon = with(søknad) {
        envalg(`mottatt dagpenger siste 12 mnd`).utfylt()
    }
}
package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Verneplikt : DslFaktaseksjon {
    const val `avtjent militaer sivilforsvar tjeneste siste 12 mnd` = 7001

    override val fakta = listOf(
        boolsk faktum "faktum.avtjent-militaer-sivilforsvar-tjeneste-siste-12-mnd" id `avtjent militaer sivilforsvar tjeneste siste 12 mnd`,
    )

    override fun seksjon(søknad: Søknad) = listOf(søknad.seksjon("verneplikt", Rolle.søker, *this.databaseIder()))

    // https://lovdata.no/lov/2016-08-12-77/§6 Vernepliktsalder er 19 til og med 44 år
    // https://lovdata.no/lov/1997-02-28-19/§4-19
    override fun regeltre(søknad: Søknad): DeltreSubsumsjon = with(søknad) {
        "verneplikt".deltre {
            boolsk(`avtjent militaer sivilforsvar tjeneste siste 12 mnd`).utfylt()
        }
    }
}

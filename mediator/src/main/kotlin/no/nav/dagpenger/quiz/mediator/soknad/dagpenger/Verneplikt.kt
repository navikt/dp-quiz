package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.godkjentAv
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.sannsynliggjøresAv
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Verneplikt : DslFaktaseksjon {
    const val `avtjent militær sivilforsvar tjeneste siste 12 mnd` = 7001
    const val `avtjent militær sivilforsvar tjeneste siste 12 mnd dokumentasjon` = 7002
    const val `avtjent militær sivilforsvar tjeneste siste 12 mnd godkjenning` = 7003
    override val fakta = listOf(
        boolsk faktum "faktum.avtjent-militaer-sivilforsvar-tjeneste-siste-12-mnd" id `avtjent militær sivilforsvar tjeneste siste 12 mnd`,
        dokument faktum "faktum.avtjent-militaer-sivilforsvar-tjeneste-siste-12-mnd-dokumentasjon" id `avtjent militær sivilforsvar tjeneste siste 12 mnd dokumentasjon`,
        boolsk faktum "faktum.avtjent-militaer-sivilforsvar-tjeneste-siste-12-mnd-godkjenning" id `avtjent militær sivilforsvar tjeneste siste 12 mnd godkjenning` avhengerAv `avtjent militær sivilforsvar tjeneste siste 12 mnd dokumentasjon`
    )

    override fun seksjon(søknad: Søknad) = listOf(
        søknad.seksjon(
            "verneplikt",
            Rolle.søker,
            `avtjent militær sivilforsvar tjeneste siste 12 mnd`
        )
    )

    // https://lovdata.no/lov/2016-08-12-77/§6 Vernepliktsalder er 19 til og med 44 år
    // https://lovdata.no/lov/1997-02-28-19/§4-19
    override fun regeltre(søknad: Søknad): DeltreSubsumsjon = with(søknad) {
        "verneplikt".deltre {
            "må godkjennes hvis ja".minstEnAv(
                (boolsk(`avtjent militær sivilforsvar tjeneste siste 12 mnd`) er true).sannsynliggjøresAv(
                    dokument(
                        `avtjent militær sivilforsvar tjeneste siste 12 mnd dokumentasjon`
                    )
                ).godkjentAv(boolsk(`avtjent militær sivilforsvar tjeneste siste 12 mnd godkjenning`)),
                boolsk(`avtjent militær sivilforsvar tjeneste siste 12 mnd`) er false
            )
        }
    }
}

package no.nav.dagpenger.quiz.mediator.soknad.dagpenger.v248

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Fakta.Companion.seksjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.godkjentAv
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.sannsynliggjøresAv
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Verneplikt : DslFaktaseksjon {
    const val `avtjent militær sivilforsvar tjeneste siste 12 mnd` = 7001

    const val `dokumentasjon avtjent militær sivilforsvar tjeneste siste 12 mnd` = 7002
    const val `godkjenning avtjent militær sivilforsvar tjeneste siste 12 mnd` = 7003

    override val fakta = listOf(
        boolsk faktum "faktum.avtjent-militaer-sivilforsvar-tjeneste-siste-12-mnd" id `avtjent militær sivilforsvar tjeneste siste 12 mnd`,

        dokument faktum "faktum.dokument-avtjent-militaer-sivilforsvar-tjeneste-siste-12-mnd-dokumentasjon"
            id `dokumentasjon avtjent militær sivilforsvar tjeneste siste 12 mnd` avhengerAv `avtjent militær sivilforsvar tjeneste siste 12 mnd`,

        boolsk faktum "faktum.avtjent-militaer-sivilforsvar-tjeneste-siste-12-mnd-godkjenning"
            id `godkjenning avtjent militær sivilforsvar tjeneste siste 12 mnd`
            avhengerAv `dokumentasjon avtjent militær sivilforsvar tjeneste siste 12 mnd`
    )

    override fun seksjon(fakta: Fakta) = listOf(
        fakta.seksjon(
            "verneplikt",
            Rolle.søker,
            *spørsmålsrekkefølgeForSøker()
        )
    )

    // https://lovdata.no/lov/2016-08-12-77/§6 Vernepliktsalder er 19 til og med 44 år
    // https://lovdata.no/lov/1997-02-28-19/§4-19
    override fun regeltre(fakta: Fakta): DeltreSubsumsjon = with(fakta) {
        "verneplikt".deltre {
            "må godkjennes hvis ja".minstEnAv(
                (boolsk(`avtjent militær sivilforsvar tjeneste siste 12 mnd`) er true).sannsynliggjøresAv(
                    dokument(
                        `dokumentasjon avtjent militær sivilforsvar tjeneste siste 12 mnd`
                    )
                ).godkjentAv(boolsk(`godkjenning avtjent militær sivilforsvar tjeneste siste 12 mnd`)),
                boolsk(`avtjent militær sivilforsvar tjeneste siste 12 mnd`) er false
            )
        }
    }

    override val spørsmålsrekkefølgeForSøker = listOf(
        `avtjent militær sivilforsvar tjeneste siste 12 mnd`,
        `dokumentasjon avtjent militær sivilforsvar tjeneste siste 12 mnd`,
        `godkjenning avtjent militær sivilforsvar tjeneste siste 12 mnd`
    )
}

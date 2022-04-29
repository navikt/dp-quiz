package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.flervalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.land
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.periode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object AndreYtelser : DslFaktaseksjon {
    const val `andre ytelser mottatt eller sokt` = 5001
    const val `hvilke andre ytelser` = 5002
    const val `tjenestepensjon hvem utbetaler` = 5003
    const val `tjenestepensjon hvilken periode` = 5004
    const val `etterlonn arbeidsgiver hvem utbetaler` = 5005
    const val `etterlonn arbeidsgiver hvilken periode` = 5006
    const val `dagpenger hvilket eos land utbetaler` = 5007
    const val `dagpenger eos land hvilken periode` = 5008
    const val `hvilken annen ytelse` = 5009
    const val `annen ytelse hvem utebetaler` = 5010
    const val `annen ytelse hvilken periode` = 5011
    const val `utbetaling eller okonomisk gode tidligere arbeidsgiver` = 5012
    const val `okonomisk gode tidligere arbeidsgiver hva omfatter avtalen` = 5013

    override var fakta = listOf(
        boolsk faktum "faktum.andre-ytelser-mottatt-eller-sokt" id `andre ytelser mottatt eller sokt`,
        flervalg faktum "faktum.hvilke-andre-ytelser"
            med "svar.pensjon-offentlig-tjenestepensjon"
            med "svar.arbeidsloshet-garantikassen-for-fiskere"
            med "svar.garantilott-garantikassen-for-fiskere"
            med "svar.etterlonn-arbeidsgiver"
            med "svar.dagpenger-annet-eos-land"
            med "svar.annen-ytelse" id `hvilke andre ytelser`,
        tekst faktum "faktum.tjenestepensjon-hvem-utbetaler" id `tjenestepensjon hvem utbetaler`,
        periode faktum "faktum.tjenestepensjon-hvilken-periode" id `tjenestepensjon hvilken periode`,
        tekst faktum "faktum.etterlonn-arbeidsgiver-hvem-utbetaler" id `etterlonn arbeidsgiver hvem utbetaler`,
        periode faktum "faktum.etterlonn-arbeidsgiver-hvilken-periode" id `etterlonn arbeidsgiver hvilken periode`,
        land faktum "faktum.dagpenger-hvilket-eos-land-utbetaler" id `dagpenger hvilket eos land utbetaler`,
        periode faktum "faktum.dagpenger-eos-land-hvilken-periode" id `dagpenger eos land hvilken periode`,
        // @todo: Skal denne være tekst?
        tekst faktum "faktum.hvilken-annen-ytelse" id `hvilken annen ytelse`,
        // @todo: Skal denne være tekst?
        tekst faktum "faktum.annen-ytelse-hvem-utebetaler" id `annen ytelse hvem utebetaler`,
        periode faktum "faktum.annen-ytelse-hvilken-periode" id `annen ytelse hvilken periode`,
        boolsk faktum "faktum.utbetaling-eller-okonomisk-gode-tidligere-arbeidsgiver" id `utbetaling eller okonomisk gode tidligere arbeidsgiver`,
        tekst faktum "faktum.okonomisk-gode-tidligere-arbeidsgiver-hva-omfatter-avtalen" id `okonomisk gode tidligere arbeidsgiver hva omfatter avtalen`
    )
}

package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.envalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.flervalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object AndreYtelser : DslFaktaseksjon {
    const val `andre ytelser` = 5001
    const val `tjenestepensjon hvem utbetaler hvilken periode` = 5002
    const val `arbeidsloshet garantikassen for fiskere periode` = 5003
    const val `garantilott garantikassen for fiskere periode` = 5004
    const val `etterlonn hvem utbetaler hvilken periode` = 5005
    const val `vartpenger hvem utbetaler hvilken periode` = 5006
    const val `dagpenger annet eos land` = 5007
    const val `annen ytelse hvilken` = 5008
    const val `annen ytelse hvem utebetaler hvilken periode` = 5009
    const val `utbetaling okonomisk gode tidligere arbeidsgiver` = 5010
    const val `okonomisk gode tidligere arbeidsgiver hva omfatter avtalen` = 5011

    override var fakta = listOf(
        flervalg faktum "faktum.andre-ytelser"
            med "svar.pensjon-offentlig-tjenestepensjon"
            med "svar.arbeidsloshet-garantikassen-for-fiskere"
            med "svar.garantilott-garantikassen-for-fiskere"
            med "svar.etterlonn-arbeidsgiver"
            med "svar.vartpenger"
            med "svar.dagpenger-annet-eos-land"
            med "svar.annen-ytelse"
            med "svar.nei" id `andre ytelser`,
        tekst faktum "faktum.tjenestepensjon-hvem-utbetaler-hvilken-periode" id `tjenestepensjon hvem utbetaler hvilken periode`,
        tekst faktum "faktum.arbeidsloshet-garantikassen-for-fiskere-periode" id `arbeidsloshet garantikassen for fiskere periode`,
        tekst faktum "faktum.garantilott-garantikassen-for-fiskere-periode" id `garantilott garantikassen for fiskere periode`,
        tekst faktum "faktum.etterlonn-hvem-utbetaler-hvilken-periode" id `etterlonn hvem utbetaler hvilken periode`,
        tekst faktum "faktum.vartpenger-hvem-utbetaler-hvilken-periode" id `vartpenger hvem utbetaler hvilken periode`,
        envalg faktum "faktum.dagpenger-annet-eos-land"
            med "" id `dagpenger annet eos land`,
        tekst faktum "faktum.annen-ytelse-hvilken" id `annen ytelse hvilken`,
        tekst faktum "faktum.annen-ytelse-hvem-utebetaler-hvilken-periode" id `annen ytelse hvem utebetaler hvilken periode`,
        boolsk faktum "faktum.utbetaling-okonomisk-gode-tidligere-arbeidsgiver" id `utbetaling okonomisk gode tidligere arbeidsgiver`,
        tekst faktum "faktum.okonomisk-gode-tidligere-arbeidsgiver-hva-omfatter-avtalen" id `okonomisk gode tidligere arbeidsgiver hva omfatter avtalen`
    )
}

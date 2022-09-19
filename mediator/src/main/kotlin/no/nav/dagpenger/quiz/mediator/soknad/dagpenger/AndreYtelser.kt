package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.flervalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.land
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.periode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.minstEnAv
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
    const val `arbeidsløs GFF hvilken periode` = 5014
    const val `garantilott fra GFF hvilken periode` = 5015

    override var fakta = listOf(
        boolsk faktum "faktum.andre-ytelser-mottatt-eller-sokt" id `andre ytelser mottatt eller sokt`,
        flervalg faktum "faktum.hvilke-andre-ytelser"
            med "svar.pensjon-offentlig-tjenestepensjon"
            med "svar.arbeidsloshet-garantikassen-for-fiskere"
            med "svar.garantilott-garantikassen-for-fiskere"
            med "svar.etterlonn-arbeidsgiver"
            med "svar.dagpenger-annet-eos-land"
            med "svar.annen-ytelse" id `hvilke andre ytelser` avhengerAv `andre ytelser mottatt eller sokt`,
        tekst faktum "faktum.tjenestepensjon-hvem-utbetaler" id `tjenestepensjon hvem utbetaler` avhengerAv `andre ytelser mottatt eller sokt`,
        periode faktum "faktum.tjenestepensjon-hvilken-periode" id `tjenestepensjon hvilken periode` avhengerAv `andre ytelser mottatt eller sokt`,
        tekst faktum "faktum.etterlonn-arbeidsgiver-hvem-utbetaler" id `etterlonn arbeidsgiver hvem utbetaler` avhengerAv `andre ytelser mottatt eller sokt`,
        periode faktum "faktum.etterlonn-arbeidsgiver-hvilken-periode" id `etterlonn arbeidsgiver hvilken periode` avhengerAv `andre ytelser mottatt eller sokt`,
        land faktum "faktum.dagpenger-hvilket-eos-land-utbetaler" id `dagpenger hvilket eos land utbetaler` avhengerAv `andre ytelser mottatt eller sokt`,
        periode faktum "faktum.dagpenger-eos-land-hvilken-periode" id `dagpenger eos land hvilken periode` avhengerAv `andre ytelser mottatt eller sokt`,
        // @todo: Skal denne være tekst?
        tekst faktum "faktum.hvilken-annen-ytelse" id `hvilken annen ytelse` avhengerAv `andre ytelser mottatt eller sokt`,
        // @todo: Skal denne være tekst?
        tekst faktum "faktum.annen-ytelse-hvem-utebetaler" id `annen ytelse hvem utebetaler` avhengerAv `andre ytelser mottatt eller sokt`,
        periode faktum "faktum.annen-ytelse-hvilken-periode" id `annen ytelse hvilken periode` avhengerAv `andre ytelser mottatt eller sokt`,
        boolsk faktum "faktum.utbetaling-eller-okonomisk-gode-tidligere-arbeidsgiver" id `utbetaling eller okonomisk gode tidligere arbeidsgiver`,
        tekst faktum "faktum.okonomisk-gode-tidligere-arbeidsgiver-hva-omfatter-avtalen" id `okonomisk gode tidligere arbeidsgiver hva omfatter avtalen` avhengerAv `utbetaling eller okonomisk gode tidligere arbeidsgiver`,
        periode faktum "faktum.arbeidsløs-GFF-hvilken-periode" id `arbeidsløs GFF hvilken periode` avhengerAv `andre ytelser mottatt eller sokt`,
        periode faktum "faktum.garantilott-GFF-hvilken-periode" id `garantilott fra GFF hvilken periode` avhengerAv `andre ytelser mottatt eller sokt`,
        // @todo: det trengs et dokumentfaktum per mulig ytelse, siden flere av de kan velges samtidig
    )

    override fun seksjon(søknad: Søknad) = listOf(søknad.seksjon("andre-ytelser", Rolle.søker, *this.databaseIder()))
    override fun regeltre(søknad: Søknad): DeltreSubsumsjon = with(søknad) {
        "andre ytelser".deltre {
            "Har eller har ikke endre ytelser".minstEnAv(
                boolsk(`andre ytelser mottatt eller sokt`) er false,
                boolsk(`andre ytelser mottatt eller sokt`) er true hvisOppfylt {
                    "Har angitt at har en eller flere andre ytelser".minstEnAv(
                        tjenestepensjon(),
                        arbeidsløsGFF(),
                        garantiloggfraGFF(),
                        etterlønnFraArbeidsgiver(),
                        dagpengerFraAnnetEøsLand(),
                        annenYtelse()
                    )
                },
            ).hvisOppfylt {
                "Felles avsluttningsspørsmål".minstEnAv(
                    boolsk(`utbetaling eller okonomisk gode tidligere arbeidsgiver`) er false,
                    boolsk(`utbetaling eller okonomisk gode tidligere arbeidsgiver`) er true hvisOppfylt {
                        tekst(`okonomisk gode tidligere arbeidsgiver hva omfatter avtalen`).utfylt()
                    }
                )
            }
        }
    }

    private fun Søknad.tjenestepensjon() =
        flervalg(`hvilke andre ytelser`) er Flervalg("faktum.hvilke-andre-ytelser.svar.pensjon-offentlig-tjenestepensjon") hvisOppfylt {
            "Hvem utbetaler pensjonen?".alle(
                // Dokumentasjonskrav - Dokumentasjon på at man mottar denne ytelsen
                tekst(`tjenestepensjon hvem utbetaler`).utfylt(),
                periode(`tjenestepensjon hvilken periode`).utfylt()
            )
        }

    private fun Søknad.arbeidsløsGFF() =
        flervalg(`hvilke andre ytelser`) er Flervalg("faktum.hvilke-andre-ytelser.svar.arbeidsloshet-garantikassen-for-fiskere") hvisOppfylt {
            "Dokumentasjonskrav og periode".alle(
                // Dokumentasjonskrav - Dokumentasjon på at man mottar denne ytelsen
                periode(`arbeidsløs GFF hvilken periode`).utfylt()
            )
        }

    private fun Søknad.garantiloggfraGFF() =
        flervalg(`hvilke andre ytelser`) er Flervalg("faktum.hvilke-andre-ytelser.svar.garantilott-garantikassen-for-fiskere") hvisOppfylt {
            "Dokumentasjonskrav og periode".alle(
                // Dokumentasjonskrav - Dokumentasjon på at man mottar denne ytelsen
                periode(`garantilott fra GFF hvilken periode`).utfylt()
            )
        }

    private fun Søknad.etterlønnFraArbeidsgiver() =
        flervalg(`hvilke andre ytelser`) er Flervalg("faktum.hvilke-andre-ytelser.svar.etterlonn-arbeidsgiver") hvisOppfylt {
            "Dokumentasjonskrav og periode".alle(
                // Dokumentasjonskrav - Dokumentasjon på at man mottar denne ytelsen
                tekst(`etterlonn arbeidsgiver hvem utbetaler`).utfylt(),
                periode(`etterlonn arbeidsgiver hvilken periode`).utfylt()
            )
        }

    private fun Søknad.dagpengerFraAnnetEøsLand() =
        flervalg(`hvilke andre ytelser`) er Flervalg("faktum.hvilke-andre-ytelser.svar.dagpenger-annet-eos-land") hvisOppfylt {
            "Dokumentasjonskrav og periode".alle(
                // Dokumentasjonskrav - Dokumentasjon på at man mottar denne ytelsen
                land(`dagpenger hvilket eos land utbetaler`).utfylt(),
                periode(`dagpenger eos land hvilken periode`).utfylt()
            )
        }

    private fun Søknad.annenYtelse() =
        flervalg(`hvilke andre ytelser`) er Flervalg("faktum.hvilke-andre-ytelser.svar.annen-ytelse") hvisOppfylt {
            "Dokumentasjonskrav og periode".alle(
                // Dokumentasjonskrav - Dokumentasjon på at man mottar denne ytelsen
                tekst(`hvilken annen ytelse`).utfylt(),
                tekst(`annen ytelse hvem utebetaler`).utfylt(),
                periode(`annen ytelse hvilken periode`).utfylt()
            )
        }
}

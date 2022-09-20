package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
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
import no.nav.dagpenger.model.subsumsjon.godkjentAv
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.sannsynliggjøresAv
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object AndreYtelser : DslFaktaseksjon {
    const val `andre ytelser mottatt eller søkt` = 5001
    const val `hvilke andre ytelser` = 5002
    const val `tjenestepensjon hvem utbetaler` = 5003
    const val `tjenestepensjon hvilken periode` = 5004
    const val `etterlønn arbeidsgiver hvem utbetaler` = 5005
    const val `etterlønn arbeidsgiver hvilken periode` = 5006
    const val `dagpenger hvilket eøs land utbetaler` = 5007
    const val `dagpenger eøs land hvilken periode` = 5008
    const val `hvilken annen ytelse` = 5009
    const val `annen ytelse hvem utebetaler` = 5010
    const val `annen ytelse hvilken periode` = 5011
    const val `utbetaling eller økonomisk gode tidligere arbeidsgiver` = 5012
    const val `økonomisk gode tidligere arbeidsgiver hva omfatter avtalen` = 5013
    const val `arbeidsløs GFF hvilken periode` = 5014
    const val `garantilott fra GFF hvilken periode` = 5015

    const val `dokumentasjon tjenestepensjon` = 5016
    const val `godkjenning dokumentasjon tjenestepensjon` = 5017
    const val `dokumentasjon arbeidsløs GFF periode` = 5018
    const val `godkjenning dokumentasjon arbeidsløs GFF periode` = 5019
    const val `dokumentasjon garantilott fra GFF periode` = 5020
    const val `godkjenning dokumentasjon garantilott fra GFF periode` = 502
    const val `dokumentasjon etterlønn` = 5022
    const val `godkjenning dokumentasjon etterlønn` = 5023
    const val `dokumentasjon dagpenger eøs land` = 5024
    const val `godkjenning dokumentasjon dagpenger eøs land` = 5025
    const val `dokumentasjon annen ytelse` = 5026
    const val `godkjenning dokumentasjon annen ytelse` = 5027
    const val `dokumentasjon økonomiske goder fra tidligere arbeidsgiver` = 5028
    const val `godkjenning dokumentasjon økonomiske goder fra tidligere arbeidsgiver` = 5029

    override var fakta = listOf(
        boolsk faktum "faktum.andre-ytelser-mottatt-eller-sokt" id `andre ytelser mottatt eller søkt`,
        flervalg faktum "faktum.hvilke-andre-ytelser"
            med "svar.pensjon-offentlig-tjenestepensjon"
            med "svar.arbeidsloshet-garantikassen-for-fiskere"
            med "svar.garantilott-garantikassen-for-fiskere"
            med "svar.etterlonn-arbeidsgiver"
            med "svar.dagpenger-annet-eos-land"
            med "svar.annen-ytelse" id `hvilke andre ytelser` avhengerAv `andre ytelser mottatt eller søkt`,

        tekst faktum "faktum.tjenestepensjon-hvem-utbetaler" id `tjenestepensjon hvem utbetaler` avhengerAv `andre ytelser mottatt eller søkt`,
        periode faktum "faktum.tjenestepensjon-hvilken-periode" id `tjenestepensjon hvilken periode` avhengerAv `andre ytelser mottatt eller søkt`,
        dokument faktum "faktum.dokument-tjenestepensjon" id `dokumentasjon tjenestepensjon`,
        boolsk faktum "faktum.godkjenning-dokument-tjenestepensjon" id `godkjenning dokumentasjon tjenestepensjon` avhengerAv `dokumentasjon tjenestepensjon`,

        periode faktum "faktum.arbeidslos-GFF-hvilken-periode" id `arbeidsløs GFF hvilken periode` avhengerAv `andre ytelser mottatt eller søkt`,
        dokument faktum "faktum.dokument-arbeidslos-GFF-hvilken-periode" id `dokumentasjon arbeidsløs GFF periode`,
        boolsk faktum "faktum.godkjenning-dokument-arbeidslos-GFF-hvilken-periode" id `godkjenning dokumentasjon arbeidsløs GFF periode` avhengerAv `dokumentasjon arbeidsløs GFF periode`,

        periode faktum "faktum.garantilott-GFF-hvilken-periode" id `garantilott fra GFF hvilken periode` avhengerAv `andre ytelser mottatt eller søkt`,
        dokument faktum "faktum.dokument-garantilott-GFF-hvilken-periode" id `dokumentasjon garantilott fra GFF periode`,
        boolsk faktum "faktum.godkjenning-dokument-garantilott-GFF-hvilken-periode" id `godkjenning dokumentasjon garantilott fra GFF periode` avhengerAv `dokumentasjon garantilott fra GFF periode`,

        tekst faktum "faktum.etterlonn-arbeidsgiver-hvem-utbetaler" id `etterlønn arbeidsgiver hvem utbetaler` avhengerAv `andre ytelser mottatt eller søkt`,
        periode faktum "faktum.etterlonn-arbeidsgiver-hvilken-periode" id `etterlønn arbeidsgiver hvilken periode` avhengerAv `andre ytelser mottatt eller søkt`,
        dokument faktum "faktum.dokument-etterlonn" id `dokumentasjon etterlønn`,
        boolsk faktum "faktum.godkjenning-dokument-etterlonn" id `godkjenning dokumentasjon etterlønn` avhengerAv `dokumentasjon etterlønn`,

        land faktum "faktum.dagpenger-hvilket-eos-land-utbetaler" id `dagpenger hvilket eøs land utbetaler` avhengerAv `andre ytelser mottatt eller søkt`,
        periode faktum "faktum.dagpenger-eos-land-hvilken-periode" id `dagpenger eøs land hvilken periode` avhengerAv `andre ytelser mottatt eller søkt`,
        dokument faktum "faktum.dokument-dagpenger-eos-land" id `dokumentasjon dagpenger eøs land`,
        boolsk faktum "faktum.godkjenning-dokument-dagpenger-eos-land" id `godkjenning dokumentasjon dagpenger eøs land` avhengerAv `dokumentasjon dagpenger eøs land`,

        // @todo: Skal denne være tekst?
        tekst faktum "faktum.hvilken-annen-ytelse" id `hvilken annen ytelse` avhengerAv `andre ytelser mottatt eller søkt`,
        tekst faktum "faktum.annen-ytelse-hvem-utebetaler" id `annen ytelse hvem utebetaler` avhengerAv `andre ytelser mottatt eller søkt`,
        periode faktum "faktum.annen-ytelse-hvilken-periode" id `annen ytelse hvilken periode` avhengerAv `andre ytelser mottatt eller søkt`,
        dokument faktum "faktum.dokument-annen-ytelse" id `dokumentasjon annen ytelse`,
        boolsk faktum "faktum.godkjenning-dokument-annen-ytelse" id `godkjenning dokumentasjon annen ytelse` avhengerAv `dokumentasjon annen ytelse`,

        boolsk faktum "faktum.utbetaling-eller-okonomisk-gode-tidligere-arbeidsgiver" id `utbetaling eller økonomisk gode tidligere arbeidsgiver`,
        tekst faktum "faktum.okonomisk-gode-tidligere-arbeidsgiver-hva-omfatter-avtalen" id `økonomisk gode tidligere arbeidsgiver hva omfatter avtalen` avhengerAv `utbetaling eller økonomisk gode tidligere arbeidsgiver`,
        dokument faktum "faktum.dokument-okonomiske-goder-tidligere-arbeidsgiver" id `dokumentasjon økonomiske goder fra tidligere arbeidsgiver`,
        boolsk faktum "faktum.godkjenning-dokument-okonomiske-goder-tidligere-arbeidsgiver" id `godkjenning dokumentasjon økonomiske goder fra tidligere arbeidsgiver` avhengerAv `dokumentasjon økonomiske goder fra tidligere arbeidsgiver`
    )

    override fun seksjon(søknad: Søknad) = listOf(søknad.seksjon("andre-ytelser", Rolle.søker, *databaseIder()))
    override fun regeltre(søknad: Søknad): DeltreSubsumsjon = with(søknad) {
        "andre ytelser".deltre {
            "Har eller har ikke andre ytelser".minstEnAv(
                boolsk(`andre ytelser mottatt eller søkt`) er false,
                boolsk(`andre ytelser mottatt eller søkt`) er true hvisOppfylt {
                    "Har angitt at har en eller flere andre ytelser".minstEnAv(
                        tjenestepensjon(),
                        arbeidsløsGFF(),
                        garantilottFraGFF(),
                        etterlønnFraArbeidsgiver(),
                        dagpengerFraAnnetEøsLand(),
                        annenYtelse()
                    )
                },
            ).hvisOppfylt {
                "Felles avsluttningsspørsmål".minstEnAv(
                    boolsk(`utbetaling eller økonomisk gode tidligere arbeidsgiver`) er false,
                    (boolsk(`utbetaling eller økonomisk gode tidligere arbeidsgiver`) er true)
                        .sannsynliggjøresAv(dokument(`dokumentasjon økonomiske goder fra tidligere arbeidsgiver`))
                        .godkjentAv(boolsk(`godkjenning dokumentasjon økonomiske goder fra tidligere arbeidsgiver`)) hvisOppfylt {
                        tekst(`økonomisk gode tidligere arbeidsgiver hva omfatter avtalen`).utfylt()
                    }
                )
            }
        }
    }

    private fun Søknad.tjenestepensjon() =
        (flervalg(`hvilke andre ytelser`) er Flervalg("faktum.hvilke-andre-ytelser.svar.pensjon-offentlig-tjenestepensjon"))
            .sannsynliggjøresAv(dokument(`dokumentasjon tjenestepensjon`))
            .godkjentAv(boolsk(`godkjenning dokumentasjon tjenestepensjon`)) hvisOppfylt {
            "hvem utbetaler pensjonen og for hvilken periode".alle(
                tekst(`tjenestepensjon hvem utbetaler`).utfylt(),
                periode(`tjenestepensjon hvilken periode`).utfylt()

            )
        }

    private fun Søknad.arbeidsløsGFF() =
        (flervalg(`hvilke andre ytelser`) er Flervalg("faktum.hvilke-andre-ytelser.svar.arbeidsloshet-garantikassen-for-fiskere"))
            .sannsynliggjøresAv(dokument(`dokumentasjon arbeidsløs GFF periode`))
            .godkjentAv(boolsk(`godkjenning dokumentasjon arbeidsløs GFF periode`)) hvisOppfylt {
            "for hvilken periode".alle(
                periode(`arbeidsløs GFF hvilken periode`).utfylt()
            )
        }

    private fun Søknad.garantilottFraGFF() =
        (flervalg(`hvilke andre ytelser`) er Flervalg("faktum.hvilke-andre-ytelser.svar.garantilott-garantikassen-for-fiskere"))
            .sannsynliggjøresAv(dokument(`dokumentasjon garantilott fra GFF periode`))
            .godkjentAv(boolsk(`godkjenning dokumentasjon garantilott fra GFF periode`)) hvisOppfylt {
            "for hvilken periode".alle(
                periode(`garantilott fra GFF hvilken periode`).utfylt()
            )
        }

    private fun Søknad.etterlønnFraArbeidsgiver() =
        (flervalg(`hvilke andre ytelser`) er Flervalg("faktum.hvilke-andre-ytelser.svar.etterlonn-arbeidsgiver"))
            .sannsynliggjøresAv(dokument(`dokumentasjon etterlønn`))
            .godkjentAv(boolsk(`godkjenning dokumentasjon etterlønn`)) hvisOppfylt {
            "hvem utbetaler etterlønnen og for hvilken periode".alle(
                tekst(`etterlønn arbeidsgiver hvem utbetaler`).utfylt(),
                periode(`etterlønn arbeidsgiver hvilken periode`).utfylt()
            )
        }

    private fun Søknad.dagpengerFraAnnetEøsLand() =
        (flervalg(`hvilke andre ytelser`) er Flervalg("faktum.hvilke-andre-ytelser.svar.dagpenger-annet-eos-land"))
            .sannsynliggjøresAv(dokument(`dokumentasjon dagpenger eøs land`))
            .godkjentAv(boolsk(`godkjenning dokumentasjon dagpenger eøs land`)) hvisOppfylt {
            "hvilket land utbetaler og for hvilken periode".alle(
                land(`dagpenger hvilket eøs land utbetaler`).utfylt(),
                periode(`dagpenger eøs land hvilken periode`).utfylt()
            )
        }

    private fun Søknad.annenYtelse() =
        (flervalg(`hvilke andre ytelser`) er Flervalg("faktum.hvilke-andre-ytelser.svar.annen-ytelse"))
            .sannsynliggjøresAv(dokument(`dokumentasjon annen ytelse`))
            .godkjentAv(boolsk(`godkjenning dokumentasjon annen ytelse`)) hvisOppfylt {
            "hvem utbetaler og for hvilken periode".alle(
                tekst(`hvilken annen ytelse`).utfylt(),
                tekst(`annen ytelse hvem utebetaler`).utfylt(),
                periode(`annen ytelse hvilken periode`).utfylt()
            )
        }
}

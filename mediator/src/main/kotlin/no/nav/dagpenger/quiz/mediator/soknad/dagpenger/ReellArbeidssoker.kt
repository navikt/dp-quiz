package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.flervalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
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

object ReellArbeidssoker : DslFaktaseksjon {

    const val `Kan jobbe heltid` = 1
    const val `Årsak til kun deltid` = 2
    const val `Skriv kort om situasjonen din` = 3
    const val `Antall timer deltid du kan jobbe` = 4
    const val `Kan du jobbe i hele Norge` = 5
    const val `Årsak kan ikke jobbe i hele Norge` = 6
    const val `Kort om hvorfor ikke jobbe hele norge` = 7
    const val `Kan ta alle typer arbeid` = 8
    const val `Kan bytte yrke og eller gå ned i lønn` = 9
    const val `Dokumentasjon for redusert helse` = 10
    const val `Godkjenning av dokumentasjon for redusert helse` = 11
    const val `Dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov` = 12
    const val `Godkjenning av dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov` = 13
    const val `Dokumentasjon annen situasjon` = 14
    const val `Godkjenning av dokumentasjon annen situasjon` = 15
    const val `Dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klass eller barn med spesielle behov under 18` =
        16
    const val `Godkjenning av dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klass eller barn med spesielle behov under 18` =
        17

    override val fakta = listOf(
        boolsk faktum "faktum.jobbe-hel-deltid" id `Kan jobbe heltid`,
        flervalg faktum "faktum.kun-deltid-aarsak"
            med "svar.redusert-helse"
            med "svar.omsorg-baby"
            med "svar.eneansvar-barn"
            med "svar.omsorg-barn-spesielle-behov"
            med "svar.skift-turnus"
            med "svar.har-fylt-60"
            med "svar.annen-situasjon" id `Årsak til kun deltid` avhengerAv `Kan jobbe heltid`,
        heltall faktum "faktum.kun-deltid-aarsak-antall-timer" id `Antall timer deltid du kan jobbe` avhengerAv `Årsak til kun deltid` og `Skriv kort om situasjonen din`,
        tekst faktum "faktum.kort-om-hvorfor-kun-deltid" id `Skriv kort om situasjonen din` avhengerAv `Årsak til kun deltid`,

        boolsk faktum "faktum.jobbe-hele-norge" id `Kan du jobbe i hele Norge`,
        flervalg faktum "faktum.ikke-jobbe-hele-norge"
            med "svar.redusert-helse"
            med "svar.omsorg-baby"
            med "svar.eneansvar-barn"
            med "svar.omsorg-barn-spesielle-behov"
            med "svar.skift-turnus"
            med "svar.har-fylt-60"
            med "svar.annen-situasjon" id `Årsak kan ikke jobbe i hele Norge` avhengerAv `Kan du jobbe i hele Norge`,
        tekst faktum "faktum.kort-om-hvorfor-ikke-jobbe-hele-norge" id `Kort om hvorfor ikke jobbe hele norge` avhengerAv `Årsak kan ikke jobbe i hele Norge`,

        boolsk faktum "faktum.alle-typer-arbeid" id `Kan ta alle typer arbeid`,
        boolsk faktum "faktum.bytte-yrke-ned-i-lonn" id `Kan bytte yrke og eller gå ned i lønn`,

        dokument faktum "faktum.dokumentasjon-redusert-helse" id `Dokumentasjon for redusert helse`,
        boolsk faktum "faktum.godkjenning-dokumentasjon-redusert-helse" id `Godkjenning av dokumentasjon for redusert helse` avhengerAv `Dokumentasjon for redusert helse`,

        dokument faktum "faktum.dokumentasjon-eneansvar-delt-ansvar-barn-under-18-spesielle-behov" id `Dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov`,
        boolsk faktum "faktum.godkjenning-dokumentasjon-eneansvar-delt-ansvar-barn-under-18-spesielle-behov" id `Godkjenning av dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov` avhengerAv `Dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov`,

        dokument faktum "faktum.dokumentasjon-skift-turnus" id `Dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klass eller barn med spesielle behov under 18`,
        boolsk faktum "faktum.godkjenning-dokumentasjon-skift-turnus" id `Godkjenning av dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klass eller barn med spesielle behov under 18` avhengerAv `Dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klass eller barn med spesielle behov under 18`,

        dokument faktum "faktum.dokumentasjon-annen-situasjon" id `Dokumentasjon annen situasjon`,
        boolsk faktum "faktum.godkjenning-dokumentasjon-annen-situasjon" id `Godkjenning av dokumentasjon annen situasjon` avhengerAv `Dokumentasjon annen situasjon`
    )

    // https://lovdata.no/lov/1997-02-28-19/§4-5
    override fun regeltre(søknad: Søknad): DeltreSubsumsjon = with(søknad) {
        "reell arbeidssøker".deltre {
            "§ 4-5.Reelle arbeidssøkere".alle(
                `Søkers arbeidskapasitet`(),
                `Søkers evne til å flytte for arbeid`(),
                `Søkers arbeidsmobilitet`()
            )
        }
    }

    private fun Søknad.`Søkers arbeidskapasitet`() = "".minstEnAv(
        `Jobbe fulltid`(),
        `Jobbe deltid`()
    )

    private fun Søknad.`Jobbe fulltid`() = boolsk(`Kan jobbe heltid`) er true

    private fun Søknad.`Jobbe deltid`() =
        boolsk(`Kan jobbe heltid`) er false hvisOppfylt {
            `Årsak til deltid`() hvisOppfylt {
                heltall(`Antall timer deltid du kan jobbe`).utfylt()
            }
        }

    private fun Søknad.`Årsak til deltid`() = "Årsak til deltid".deltre {
        flervalg(`Årsak til kun deltid`).utfylt() hvisOppfylt {
            "Årsak til deltid".minstEnAv(
                `Redusert helse`(),
                flervalg(`Årsak til kun deltid`) er Flervalg("faktum.kun-deltid-aarsak.svar.omsorg-baby"),
                `Ansvar for barn med spesielle behov`(),
                flervalg(`Årsak til kun deltid`) er Flervalg("faktum.kun-deltid-aarsak.svar.omsorg-barn-spesielle-behov"),
                `Ansvar for barn tom syvende klasse eller barn med spesielle behov under atten når annen forelder jobber skift turnus eller ikke i nærområdet`(),
                flervalg(`Årsak til kun deltid`) er Flervalg("faktum.kun-deltid-aarsak.svar.har-fylt-60"),
                `Annen situasjon`(),
            )
        }
    }

    private fun Søknad.`Ansvar for barn tom syvende klasse eller barn med spesielle behov under atten når annen forelder jobber skift turnus eller ikke i nærområdet`() =
        (flervalg(`Årsak til kun deltid`) er Flervalg("faktum.kun-deltid-aarsak.svar.skift-turnus")).sannsynliggjøresAv(
            dokument(
                `Dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klass eller barn med spesielle behov under 18`
            )
        ).godkjentAv(
            boolsk(
                `Godkjenning av dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klass eller barn med spesielle behov under 18`
            )
        )

    private fun Søknad.`Annen situasjon`() =
        (flervalg(`Årsak til kun deltid`) er Flervalg("faktum.kun-deltid-aarsak.svar.annen-situasjon")).sannsynliggjøresAv(
            dokument(`Dokumentasjon annen situasjon`)
        ).godkjentAv(boolsk(`Godkjenning av dokumentasjon annen situasjon`)) hvisOppfylt {
            tekst(`Skriv kort om situasjonen din`).utfylt()
        }

    private fun Søknad.`Ansvar for barn med spesielle behov`() =
        (flervalg(`Årsak til kun deltid`) er Flervalg("faktum.kun-deltid-aarsak.svar.eneansvar-barn")).sannsynliggjøresAv(
            dokument(`Dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov`)
        ).godkjentAv(
            boolsk(`Godkjenning av dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov`)
        )

    private fun Søknad.`Redusert helse`() =
        (flervalg(`Årsak til kun deltid`) er Flervalg("faktum.kun-deltid-aarsak.svar.redusert-helse")).sannsynliggjøresAv(
            dokument(`Dokumentasjon for redusert helse`)
        ).godkjentAv(boolsk(`Godkjenning av dokumentasjon for redusert helse`))

    private fun Søknad.`Søkers evne til å flytte for arbeid`() = "".minstEnAv(
        `Jobbe i hele Norge`(),
        `Ikke jobbe i hele Norge`()
    )

    private fun Søknad.`Jobbe i hele Norge`() =
        boolsk(`Kan du jobbe i hele Norge`) er true

    private fun Søknad.`Ikke jobbe i hele Norge`() =
        boolsk(`Kan du jobbe i hele Norge`) er false hvisOppfylt {
            `Årsak til at man ikke kan jobbe i hele Norge`()
        }

    private fun Søknad.`Årsak til at man ikke kan jobbe i hele Norge`() =
        flervalg(`Årsak kan ikke jobbe i hele Norge`).utfylt() hvisOppfylt {
            "Årsak til at man ikke kan jobbe i hele Norge".minstEnAv(
                flervalg(`Årsak kan ikke jobbe i hele Norge`) er Flervalg("faktum.ikke-jobbe-hele-norge.svar.redusert-helse"),
                flervalg(`Årsak kan ikke jobbe i hele Norge`) er Flervalg("faktum.ikke-jobbe-hele-norge.svar.omsorg-baby"),
                flervalg(`Årsak kan ikke jobbe i hele Norge`) er Flervalg("faktum.ikke-jobbe-hele-norge.svar.eneansvar-barn"),
                flervalg(`Årsak kan ikke jobbe i hele Norge`) er Flervalg("faktum.ikke-jobbe-hele-norge.svar.omsorg-barn-spesielle-behov"),
                flervalg(`Årsak kan ikke jobbe i hele Norge`) er Flervalg("faktum.ikke-jobbe-hele-norge.svar.skift-turnus"),
                flervalg(`Årsak kan ikke jobbe i hele Norge`) er Flervalg("faktum.ikke-jobbe-hele-norge.svar.har-fylt-60"),
                flervalg(`Årsak kan ikke jobbe i hele Norge`) er Flervalg("faktum.ikke-jobbe-hele-norge.svar.annen-situasjon") hvisOppfylt {
                    tekst(`Kort om hvorfor ikke jobbe hele norge`).utfylt()
                }
            )
        }

    private fun Søknad.`Søkers arbeidsmobilitet`() = "".alle(
        boolsk(`Kan ta alle typer arbeid`).utfylt(),
        boolsk(`Kan bytte yrke og eller gå ned i lønn`).utfylt()
    )

    override fun seksjon(søknad: Søknad) =
        listOf(søknad.seksjon("reell-arbeidssoker", Rolle.søker, *this.databaseIder()))
}

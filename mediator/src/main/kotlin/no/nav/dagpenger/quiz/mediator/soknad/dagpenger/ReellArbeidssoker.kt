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
import no.nav.dagpenger.model.regel.inneholder
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

    const val `kan jobbe heltid` = 1
    const val `årsak til kun deltid` = 2
    const val `skriv kort om situasjonen din` = 3
    const val `antall timer deltid du kan jobbe` = 4
    const val `kan du jobbe i hele Norge` = 5
    const val `årsak kan ikke jobbe i hele Norge` = 6
    const val `kort om hvorfor ikke jobbe hele norge` = 7
    const val `kan ta alle typer arbeid` = 8
    const val `kan bytte yrke eller gå ned i lønn` = 9

    const val `dokumentasjon for redusert helse` = 10
    const val `godkjenning av dokumentasjon for redusert helse` = 11
    const val `dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov` = 12
    const val `godkjenning av dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov` = 13
    const val `dokumentasjon annen situasjon` = 14
    const val `godkjenning av dokumentasjon annen situasjon` = 15
    const val `dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klass eller barn med spesielle behov under 18` =
        16
    const val `godkjenning av dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klass eller barn med spesielle behov under 18` =
        17
    const val `dokumentasjon kan ikke ta alle typer arbeid` = 18
    const val `godkjenning av dokumentasjon kan ikke ta alle typer arbeid` = 19

    override val fakta = listOf(
        boolsk faktum "faktum.jobbe-hel-deltid" id `kan jobbe heltid`,
        flervalg faktum "faktum.kun-deltid-aarsak"
            med "svar.redusert-helse"
            med "svar.omsorg-baby"
            med "svar.eneansvar-barn"
            med "svar.omsorg-barn-spesielle-behov"
            med "svar.skift-turnus"
            med "svar.har-fylt-60"
            med "svar.annen-situasjon" id `årsak til kun deltid` avhengerAv `kan jobbe heltid`,
        heltall faktum "faktum.kun-deltid-aarsak-antall-timer" id `antall timer deltid du kan jobbe` avhengerAv `årsak til kun deltid` og `skriv kort om situasjonen din`,
        tekst faktum "faktum.kort-om-hvorfor-kun-deltid" id `skriv kort om situasjonen din` avhengerAv `årsak til kun deltid`,

        boolsk faktum "faktum.jobbe-hele-norge" id `kan du jobbe i hele Norge`,
        flervalg faktum "faktum.ikke-jobbe-hele-norge"
            med "svar.redusert-helse"
            med "svar.omsorg-baby"
            med "svar.eneansvar-barn"
            med "svar.omsorg-barn-spesielle-behov"
            med "svar.skift-turnus"
            med "svar.har-fylt-60"
            med "svar.annen-situasjon" id `årsak kan ikke jobbe i hele Norge` avhengerAv `kan du jobbe i hele Norge`,
        tekst faktum "faktum.kort-om-hvorfor-ikke-jobbe-hele-norge" id `kort om hvorfor ikke jobbe hele norge` avhengerAv `årsak kan ikke jobbe i hele Norge`,

        boolsk faktum "faktum.alle-typer-arbeid" id `kan ta alle typer arbeid`,
        boolsk faktum "faktum.bytte-yrke-ned-i-lonn" id `kan bytte yrke eller gå ned i lønn`,

        dokument faktum "faktum.dokumentasjon-redusert-helse" id `dokumentasjon for redusert helse`,
        boolsk faktum "faktum.godkjenning-dokumentasjon-redusert-helse" id `godkjenning av dokumentasjon for redusert helse` avhengerAv `dokumentasjon for redusert helse`,

        dokument faktum "faktum.dokumentasjon-eneansvar-delt-ansvar-barn-under-18-spesielle-behov" id `dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov`,
        boolsk faktum "faktum.godkjenning-dokumentasjon-eneansvar-delt-ansvar-barn-under-18-spesielle-behov" id `godkjenning av dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov` avhengerAv `dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov`,

        dokument faktum "faktum.dokumentasjon-skift-turnus" id `dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klass eller barn med spesielle behov under 18`,
        boolsk faktum "faktum.godkjenning-dokumentasjon-skift-turnus" id `godkjenning av dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klass eller barn med spesielle behov under 18` avhengerAv `dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klass eller barn med spesielle behov under 18`,

        dokument faktum "faktum.dokumentasjon-annen-situasjon" id `dokumentasjon annen situasjon`,
        boolsk faktum "faktum.godkjenning-dokumentasjon-annen-situasjon" id `godkjenning av dokumentasjon annen situasjon` avhengerAv `dokumentasjon annen situasjon`,

        dokument faktum "faktum.dokumentasjon-kan-ikke-ta-alle-typer-arbeid" id `dokumentasjon kan ikke ta alle typer arbeid`,
        boolsk faktum "faktum.godkjenning-dokumentasjon-kan-ikke-ta-alle-typer-arbeid" id `godkjenning av dokumentasjon kan ikke ta alle typer arbeid` avhengerAv `dokumentasjon kan ikke ta alle typer arbeid`
    )

    // https://lovdata.no/lov/1997-02-28-19/§4-5
    override fun regeltre(søknad: Søknad): DeltreSubsumsjon = with(søknad) {
        "reell arbeidssøker".deltre {
            "§ 4-5.Reelle arbeidssøkere".alle(
                `søkers arbeidskapasitet`(),
                `søkers evne til å flytte for arbeid`(),
                `kan ta alle typer arbeid`(),
                `kan bytte yrke eller gå ned i lønn`()
            )
        }
    }

    private fun Søknad.`søkers arbeidskapasitet`() =
        "Kan jobbe fulltid eller ikke".minstEnAv(
            `jobbe fulltid`(),
            `jobbe deltid`()
        )

    private fun Søknad.`jobbe fulltid`() = boolsk(`kan jobbe heltid`) er true

    private fun Søknad.`jobbe deltid`() =
        boolsk(`kan jobbe heltid`) er false hvisOppfylt {
            `årsak til deltid`() hvisOppfylt {
                heltall(`antall timer deltid du kan jobbe`).utfylt()
            }
        }

    private fun Søknad.`årsak til deltid`() = "Årsak til deltid".deltre {
        flervalg(`årsak til kun deltid`).utfylt() hvisOppfylt {
            "Årsak til deltid".minstEnAv(
                `årsak bare deltid - redusert helse`(),
                `årsak bare deltid - omsorg for barn under ett år`(),
                `årsak bare deltid - ansvar for barn til og med syvende klasse`(),
                `årsak bare deltid - omsorg for barn med spesielle behov`(),
                `årsak bare deltid - ansvar for barn til og med syvende klasse eller med spesielle behov og annen forelder jobber skift turnus`(),
                `årsak bare deltid- over seksti år`(),
                `årsak bare deltid - annen situasjon`(),
            )
        }
    }

    private fun Søknad.`årsak bare deltid - redusert helse`() =
        (flervalg(`årsak til kun deltid`) inneholder Flervalg("faktum.kun-deltid-aarsak.svar.redusert-helse"))
            .sannsynliggjøresAv(dokument(`dokumentasjon for redusert helse`))
            .godkjentAv(boolsk(`godkjenning av dokumentasjon for redusert helse`))

    private fun Søknad.`årsak bare deltid - omsorg for barn under ett år`() =
        flervalg(`årsak til kun deltid`) inneholder Flervalg("faktum.kun-deltid-aarsak.svar.omsorg-baby")

    private fun Søknad.`årsak bare deltid - ansvar for barn til og med syvende klasse`() =
        flervalg(`årsak til kun deltid`) inneholder Flervalg("faktum.kun-deltid-aarsak.svar.eneansvar-barn")

    private fun Søknad.`årsak bare deltid - omsorg for barn med spesielle behov`() =
        (flervalg(`årsak til kun deltid`) inneholder Flervalg("faktum.kun-deltid-aarsak.svar.omsorg-barn-spesielle-behov"))
            .sannsynliggjøresAv(
                dokument(
                    `dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov`
                )
            )
            .godkjentAv(
                boolsk(
                    `godkjenning av dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov`
                )
            )

    private fun Søknad.`årsak bare deltid - ansvar for barn til og med syvende klasse eller med spesielle behov og annen forelder jobber skift turnus`() =
        (flervalg(`årsak til kun deltid`) inneholder Flervalg("faktum.kun-deltid-aarsak.svar.skift-turnus"))
            .sannsynliggjøresAv(
                dokument(
                    `dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klass eller barn med spesielle behov under 18`
                )
            )
            .godkjentAv(
                boolsk(
                    `godkjenning av dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klass eller barn med spesielle behov under 18`
                )
            )

    private fun Søknad.`årsak bare deltid- over seksti år`() =
        flervalg(`årsak til kun deltid`) inneholder Flervalg("faktum.kun-deltid-aarsak.svar.har-fylt-60")

    private fun Søknad.`årsak bare deltid - annen situasjon`() =
        (flervalg(`årsak til kun deltid`) inneholder Flervalg("faktum.kun-deltid-aarsak.svar.annen-situasjon"))
            .sannsynliggjøresAv(dokument(`dokumentasjon annen situasjon`))
            .godkjentAv(boolsk(`godkjenning av dokumentasjon annen situasjon`)) hvisOppfylt {
            tekst(`skriv kort om situasjonen din`).utfylt()
        }

    private fun Søknad.`søkers evne til å flytte for arbeid`() =
        "Kan ta jobber i hele Norge eller ikke".minstEnAv(
            `jobbe i hele Norge`(),
            `ikke jobbe i hele Norge`()
        )

    private fun Søknad.`jobbe i hele Norge`() =
        boolsk(`kan du jobbe i hele Norge`) er true

    private fun Søknad.`ikke jobbe i hele Norge`() =
        boolsk(`kan du jobbe i hele Norge`) er false hvisOppfylt {
            `årsak til at man ikke kan jobbe i hele Norge`()
        }

    private fun Søknad.`årsak til at man ikke kan jobbe i hele Norge`() =
        flervalg(`årsak kan ikke jobbe i hele Norge`).utfylt() hvisOppfylt {
            "Årsak til at man ikke kan jobbe i hele Norge".minstEnAv(
                `årsak ikke jobbe i hele Norge - redusert helse`(),
                `årsak ikke jobbe i hele Norge - omsorg for barn under ett år`(),
                `årsak ikke jobbe i hele Norge - ansvar for barn til og med syvende klasse`(),
                `årsak ikke jobbe i hele Norge - omsorg for barn med spesielle behov`(),
                `årsak ikke jobbe i hele Norge - ansvar for barn til og med syvende klasse eller med spesielle behov og annen forelder jobber skift turnus`(),
                `årsak ikke jobbe i hele Norge - over seksti år`(),
                `årsak ikke jobbe i hele Norge - annen situasjon`()
            )
        }

    private fun Søknad.`årsak ikke jobbe i hele Norge - redusert helse`() =
        (flervalg(`årsak kan ikke jobbe i hele Norge`) inneholder Flervalg("faktum.ikke-jobbe-hele-norge.svar.redusert-helse"))
            .sannsynliggjøresAv(dokument(`dokumentasjon for redusert helse`))
            .godkjentAv(boolsk(`godkjenning av dokumentasjon for redusert helse`))

    private fun Søknad.`årsak ikke jobbe i hele Norge - omsorg for barn under ett år`() =
        flervalg(`årsak kan ikke jobbe i hele Norge`) inneholder Flervalg("faktum.ikke-jobbe-hele-norge.svar.omsorg-baby")

    private fun Søknad.`årsak ikke jobbe i hele Norge - ansvar for barn til og med syvende klasse`() =
        flervalg(`årsak kan ikke jobbe i hele Norge`) inneholder Flervalg("faktum.ikke-jobbe-hele-norge.svar.eneansvar-barn")

    private fun Søknad.`årsak ikke jobbe i hele Norge - omsorg for barn med spesielle behov`() =
        (flervalg(`årsak kan ikke jobbe i hele Norge`) inneholder Flervalg("faktum.ikke-jobbe-hele-norge.svar.omsorg-barn-spesielle-behov"))
            .sannsynliggjøresAv(
                dokument(
                    `dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov`
                )
            )
            .godkjentAv(
                boolsk(
                    `godkjenning av dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov`
                )
            )

    private fun Søknad.`årsak ikke jobbe i hele Norge - ansvar for barn til og med syvende klasse eller med spesielle behov og annen forelder jobber skift turnus`() =
        (flervalg(`årsak kan ikke jobbe i hele Norge`) inneholder Flervalg("faktum.ikke-jobbe-hele-norge.svar.skift-turnus"))
            .sannsynliggjøresAv(
                dokument(
                    `dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klass eller barn med spesielle behov under 18`
                )
            )
            .godkjentAv(
                boolsk(
                    `godkjenning av dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klass eller barn med spesielle behov under 18`
                )
            )

    private fun Søknad.`årsak ikke jobbe i hele Norge - over seksti år`() =
        flervalg(`årsak kan ikke jobbe i hele Norge`) inneholder Flervalg("faktum.ikke-jobbe-hele-norge.svar.har-fylt-60")

    private fun Søknad.`årsak ikke jobbe i hele Norge - annen situasjon`() =
        (flervalg(`årsak kan ikke jobbe i hele Norge`) inneholder Flervalg("faktum.ikke-jobbe-hele-norge.svar.annen-situasjon"))
            .sannsynliggjøresAv(dokument(`dokumentasjon annen situasjon`))
            .godkjentAv(boolsk(`godkjenning av dokumentasjon annen situasjon`)) hvisOppfylt {
            tekst(`kort om hvorfor ikke jobbe hele norge`).utfylt()
        }

    private fun Søknad.`kan ta alle typer arbeid`() =
        "Kan ta alle typer arbeid eller ikke".minstEnAv(
            (boolsk(`kan ta alle typer arbeid`) er false)
                .sannsynliggjøresAv(dokument(`dokumentasjon kan ikke ta alle typer arbeid`))
                .godkjentAv(boolsk(`godkjenning av dokumentasjon kan ikke ta alle typer arbeid`)),
            boolsk(`kan ta alle typer arbeid`) er true
        )

    private fun Søknad.`kan bytte yrke eller gå ned i lønn`() =
        boolsk(`kan bytte yrke eller gå ned i lønn`).utfylt()

    override fun seksjon(søknad: Søknad) =
        listOf(søknad.seksjon("reell-arbeidssoker", Rolle.søker, *this.databaseIder()))
}

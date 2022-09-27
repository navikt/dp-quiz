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

    const val `dokumentasjon redusert helse - kun deltid` = 10
    const val `godkjenning av dokumentasjon redusert helse - kun deltid` = 11
    const val `dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov - kun deltid` = 12
    const val `godkjenning av dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov - kun deltid` = 13
    const val `dokumentasjon annen situasjon - kun deltid` = 14
    const val `godkjenning av dokumentasjon annen situasjon - kun deltid` = 15
    const val `dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klass eller barn med spesielle behov under 18 - kun deltid` =
        16
    const val `godkjenning av dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klasse eller barn med spesielle behov under 18 - kun deltid` =
        17
    const val `dokumentasjon kan ikke ta alle typer arbeid - kun deltid` = 18
    const val `godkjenning av dokumentasjon kan ikke ta alle typer arbeid - kun deltid` = 19

    const val `dokumentasjon redusert helse - ikke jobbe hele Norge` = 20
    const val `godkjenning av dokumentasjon redusert helse - ikke jobbe hele Norge` = 21
    const val `dokumentasjon eneansvar eller delt ansvar barn under 18 år med spesielle behov - ikke jobbe hele Norge` = 22
    const val `godkjenning dokumentasjon eneansvar eller delt ansvar barn under 18 år med spesielle behov - ikke jobbe hele Norge` = 23
    const val `dokumentasjon annen situasjon - ikke jobbe hele Norge` = 24
    const val `godkjenning dokumentasjon annen situasjon - ikke jobbe hele Norge` = 25
    const val `dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar barn tom 7 klass eller barn med spesielle behov under 18 - ikke jobbe hele Norge` =
        26
    const val `godkjenning dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar barn tom 7 klasse eller barn med spesielle behov under 18 - ikke jobbe hele Norge` =
        27
    const val `dokumentasjon kan ikke ta alle typer arbeid - ikke jobbe hele Norge` = 28
    const val `godkjenning dokumentasjon kan ikke ta alle typer arbeid - ikke jobbe hele Norge` = 29

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
        heltall faktum "faktum.kun-deltid-aarsak-antall-timer" id `antall timer deltid du kan jobbe` avhengerAv `kan jobbe heltid`,
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

        dokument faktum "faktum.dokumentasjon-redusert-helse-kun-deltid" id `dokumentasjon redusert helse - kun deltid`,
        boolsk faktum "faktum.godkjenning-dokumentasjon-redusert-helse-kun-deltid"
            id `godkjenning av dokumentasjon redusert helse - kun deltid`
            avhengerAv `dokumentasjon redusert helse - kun deltid`,

        dokument faktum "faktum.dokumentasjon-eneansvar-delt-ansvar-barn-under-18-spesielle-behov-kun-deltid" id `dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov - kun deltid`,
        boolsk faktum "faktum.godkjenning-dokumentasjon-eneansvar-delt-ansvar-barn-under-18-spesielle-behov-kun-deltid"
            id `godkjenning av dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov - kun deltid`
            avhengerAv `dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov - kun deltid`,

        dokument faktum "faktum.dokumentasjon-skift-turnus-kun-deltid" id `dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klass eller barn med spesielle behov under 18 - kun deltid`,
        boolsk faktum "faktum.godkjenning-dokumentasjon-skift-turnus-kun-deltid"
            id `godkjenning av dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klasse eller barn med spesielle behov under 18 - kun deltid`
            avhengerAv `dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klass eller barn med spesielle behov under 18 - kun deltid`,

        dokument faktum "faktum.dokumentasjon-annen-situasjon-kun-deltid" id `dokumentasjon annen situasjon - kun deltid`,
        boolsk faktum "faktum.godkjenning-dokumentasjon-annen-situasjon-kun-deltid"
            id `godkjenning av dokumentasjon annen situasjon - kun deltid`
            avhengerAv `dokumentasjon annen situasjon - kun deltid`,

        dokument faktum "faktum.dokumentasjon-kan-ikke-ta-alle-typer-arbeid-kun-deltid" id `dokumentasjon kan ikke ta alle typer arbeid - kun deltid`,
        boolsk faktum "faktum.godkjenning-dokumentasjon-kan-ikke-ta-alle-typer-arbeid-kun-deltid"
            id `godkjenning av dokumentasjon kan ikke ta alle typer arbeid - kun deltid`
            avhengerAv `dokumentasjon kan ikke ta alle typer arbeid - kun deltid`,

        dokument faktum "faktum.dokumentasjon-redusert-helse-ikke-jobbe-hele-norge" id `dokumentasjon redusert helse - ikke jobbe hele Norge`,
        boolsk faktum "faktum.godkjenning-dokumentasjon-redusert-helse-ikke-jobbe-hele-norge"
            id `godkjenning av dokumentasjon redusert helse - ikke jobbe hele Norge`
            avhengerAv `dokumentasjon redusert helse - ikke jobbe hele Norge`,

        dokument faktum "faktum.dokumentasjon-eneansvar-delt-ansvar-barn-under-18-spesielle-behov-ikke-jobbe-hele-norge" id `dokumentasjon eneansvar eller delt ansvar barn under 18 år med spesielle behov - ikke jobbe hele Norge`,
        boolsk faktum "faktum.godkjenning-dokumentasjon-eneansvar-delt-ansvar-barn-under-18-spesielle-behov-ikke-jobbe-hele-norge"
            id `godkjenning dokumentasjon eneansvar eller delt ansvar barn under 18 år med spesielle behov - ikke jobbe hele Norge`
            avhengerAv `dokumentasjon eneansvar eller delt ansvar barn under 18 år med spesielle behov - ikke jobbe hele Norge`,

        dokument faktum "faktum.dokumentasjon-skift-turnus-ikke-jobbe-hele-norge"
            id `dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar barn tom 7 klass eller barn med spesielle behov under 18 - ikke jobbe hele Norge`,
        boolsk faktum "faktum.godkjenning-dokumentasjon-skift-turnus-ikke-jobbe-hele-norge"
            id `godkjenning dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar barn tom 7 klasse eller barn med spesielle behov under 18 - ikke jobbe hele Norge`
            avhengerAv `dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar barn tom 7 klass eller barn med spesielle behov under 18 - ikke jobbe hele Norge`,

        dokument faktum "faktum.dokumentasjon-annen-situasjon-ikke-jobbe-hele-norge" id `dokumentasjon annen situasjon - ikke jobbe hele Norge`,
        boolsk faktum "faktum.godkjenning-dokumentasjon-annen-situasjon-ikke-jobbe-hele-norge"
            id `godkjenning dokumentasjon annen situasjon - ikke jobbe hele Norge`
            avhengerAv `dokumentasjon annen situasjon - ikke jobbe hele Norge`,

        dokument faktum "faktum.dokumentasjon-kan-ikke-ta-alle-typer-arbeid-ikke-jobbe-hele-norge" id `dokumentasjon kan ikke ta alle typer arbeid - ikke jobbe hele Norge`,
        boolsk faktum "faktum.godkjenning-dokumentasjon-kan-ikke-ta-alle-typer-arbeid-ikke-jobbe-hele-norge"
            id `godkjenning dokumentasjon kan ikke ta alle typer arbeid - ikke jobbe hele Norge`
            avhengerAv `dokumentasjon kan ikke ta alle typer arbeid - ikke jobbe hele Norge`
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
                `årsak bare deltid - over seksti år`(),
                `årsak bare deltid - annen situasjon`()
            )
        }
    }

    private fun Søknad.`årsak bare deltid - redusert helse`() =
        (flervalg(`årsak til kun deltid`) inneholder Flervalg("faktum.kun-deltid-aarsak.svar.redusert-helse"))
            .sannsynliggjøresAv(dokument(`dokumentasjon redusert helse - kun deltid`))
            .godkjentAv(boolsk(`godkjenning av dokumentasjon redusert helse - kun deltid`))

    private fun Søknad.`årsak bare deltid - omsorg for barn under ett år`() =
        flervalg(`årsak til kun deltid`) inneholder Flervalg("faktum.kun-deltid-aarsak.svar.omsorg-baby")

    private fun Søknad.`årsak bare deltid - ansvar for barn til og med syvende klasse`() =
        flervalg(`årsak til kun deltid`) inneholder Flervalg("faktum.kun-deltid-aarsak.svar.eneansvar-barn")

    private fun Søknad.`årsak bare deltid - omsorg for barn med spesielle behov`() =
        (flervalg(`årsak til kun deltid`) inneholder Flervalg("faktum.kun-deltid-aarsak.svar.omsorg-barn-spesielle-behov"))
            .sannsynliggjøresAv(
                dokument(
                    `dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov - kun deltid`
                )
            )
            .godkjentAv(
                boolsk(
                    `godkjenning av dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov - kun deltid`
                )
            )

    private fun Søknad.`årsak bare deltid - ansvar for barn til og med syvende klasse eller med spesielle behov og annen forelder jobber skift turnus`() =
        (flervalg(`årsak til kun deltid`) inneholder Flervalg("faktum.kun-deltid-aarsak.svar.skift-turnus"))
            .sannsynliggjøresAv(
                dokument(
                    `dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klass eller barn med spesielle behov under 18 - kun deltid`
                )
            )
            .godkjentAv(
                boolsk(
                    `godkjenning av dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klasse eller barn med spesielle behov under 18 - kun deltid`
                )
            )

    private fun Søknad.`årsak bare deltid - over seksti år`() =
        flervalg(`årsak til kun deltid`) inneholder Flervalg("faktum.kun-deltid-aarsak.svar.har-fylt-60")

    private fun Søknad.`årsak bare deltid - annen situasjon`() =
        (flervalg(`årsak til kun deltid`) inneholder Flervalg("faktum.kun-deltid-aarsak.svar.annen-situasjon"))
            .sannsynliggjøresAv(dokument(`dokumentasjon annen situasjon - kun deltid`))
            .godkjentAv(boolsk(`godkjenning av dokumentasjon annen situasjon - kun deltid`)) hvisOppfylt {
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
            .sannsynliggjøresAv(dokument(`dokumentasjon redusert helse - ikke jobbe hele Norge`))
            .godkjentAv(boolsk(`godkjenning av dokumentasjon redusert helse - ikke jobbe hele Norge`))

    private fun Søknad.`årsak ikke jobbe i hele Norge - omsorg for barn under ett år`() =
        flervalg(`årsak kan ikke jobbe i hele Norge`) inneholder Flervalg("faktum.ikke-jobbe-hele-norge.svar.omsorg-baby")

    private fun Søknad.`årsak ikke jobbe i hele Norge - ansvar for barn til og med syvende klasse`() =
        flervalg(`årsak kan ikke jobbe i hele Norge`) inneholder Flervalg("faktum.ikke-jobbe-hele-norge.svar.eneansvar-barn")

    private fun Søknad.`årsak ikke jobbe i hele Norge - omsorg for barn med spesielle behov`() =
        (flervalg(`årsak kan ikke jobbe i hele Norge`) inneholder Flervalg("faktum.ikke-jobbe-hele-norge.svar.omsorg-barn-spesielle-behov"))
            .sannsynliggjøresAv(
                dokument(
                    `dokumentasjon eneansvar eller delt ansvar barn under 18 år med spesielle behov - ikke jobbe hele Norge`
                )
            )
            .godkjentAv(
                boolsk(
                    `godkjenning dokumentasjon eneansvar eller delt ansvar barn under 18 år med spesielle behov - ikke jobbe hele Norge`
                )
            )

    private fun Søknad.`årsak ikke jobbe i hele Norge - ansvar for barn til og med syvende klasse eller med spesielle behov og annen forelder jobber skift turnus`() =
        (flervalg(`årsak kan ikke jobbe i hele Norge`) inneholder Flervalg("faktum.ikke-jobbe-hele-norge.svar.skift-turnus"))
            .sannsynliggjøresAv(
                dokument(
                    `dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar barn tom 7 klass eller barn med spesielle behov under 18 - ikke jobbe hele Norge`
                )
            )
            .godkjentAv(
                boolsk(
                    `godkjenning dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar barn tom 7 klasse eller barn med spesielle behov under 18 - ikke jobbe hele Norge`
                )
            )

    private fun Søknad.`årsak ikke jobbe i hele Norge - over seksti år`() =
        flervalg(`årsak kan ikke jobbe i hele Norge`) inneholder Flervalg("faktum.ikke-jobbe-hele-norge.svar.har-fylt-60")

    private fun Søknad.`årsak ikke jobbe i hele Norge - annen situasjon`() =
        (flervalg(`årsak kan ikke jobbe i hele Norge`) inneholder Flervalg("faktum.ikke-jobbe-hele-norge.svar.annen-situasjon"))
            .sannsynliggjøresAv(dokument(`dokumentasjon annen situasjon - ikke jobbe hele Norge`))
            .godkjentAv(boolsk(`godkjenning dokumentasjon annen situasjon - ikke jobbe hele Norge`)) hvisOppfylt {
            tekst(`kort om hvorfor ikke jobbe hele norge`).utfylt()
        }

    private fun Søknad.`kan ta alle typer arbeid`() =
        "Kan ta alle typer arbeid eller ikke".minstEnAv(
            (boolsk(`kan ta alle typer arbeid`) er false)
                .sannsynliggjøresAv(dokument(`dokumentasjon kan ikke ta alle typer arbeid - ikke jobbe hele Norge`))
                .godkjentAv(boolsk(`godkjenning dokumentasjon kan ikke ta alle typer arbeid - ikke jobbe hele Norge`)),
            boolsk(`kan ta alle typer arbeid`) er true
        )

    private fun Søknad.`kan bytte yrke eller gå ned i lønn`() =
        boolsk(`kan bytte yrke eller gå ned i lønn`).utfylt()

    override fun seksjon(søknad: Søknad) =
        listOf(søknad.seksjon("reell-arbeidssoker", Rolle.søker, *spørsmålsrekkefølge))

    private val spørsmålsrekkefølge = listOf(
        `kan jobbe heltid`,
        `årsak til kun deltid`,
        `skriv kort om situasjonen din`,
        `antall timer deltid du kan jobbe`,
        `kan du jobbe i hele Norge`,
        `årsak kan ikke jobbe i hele Norge`,
        `kort om hvorfor ikke jobbe hele norge`,
        `kan ta alle typer arbeid`,
        `kan bytte yrke eller gå ned i lønn`,

        `dokumentasjon redusert helse - kun deltid`,
        `godkjenning av dokumentasjon redusert helse - kun deltid`,
        `dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov - kun deltid`,
        `godkjenning av dokumentasjon eneansvar eller delt ansvar for barn under 18 år med spesielle behov - kun deltid`,
        `dokumentasjon annen situasjon - kun deltid`,
        `godkjenning av dokumentasjon annen situasjon - kun deltid`,
        `dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klass eller barn med spesielle behov under 18 - kun deltid`,
        `godkjenning av dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar for barn tom 7 klasse eller barn med spesielle behov under 18 - kun deltid`,
        `dokumentasjon kan ikke ta alle typer arbeid - kun deltid`,
        `godkjenning av dokumentasjon kan ikke ta alle typer arbeid - kun deltid`,

        `dokumentasjon redusert helse - ikke jobbe hele Norge`,
        `godkjenning av dokumentasjon redusert helse - ikke jobbe hele Norge`,
        `dokumentasjon eneansvar eller delt ansvar barn under 18 år med spesielle behov - ikke jobbe hele Norge`,
        `godkjenning dokumentasjon eneansvar eller delt ansvar barn under 18 år med spesielle behov - ikke jobbe hele Norge`,
        `dokumentasjon annen situasjon - ikke jobbe hele Norge`,
        `godkjenning dokumentasjon annen situasjon - ikke jobbe hele Norge`,
        `dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar barn tom 7 klass eller barn med spesielle behov under 18 - ikke jobbe hele Norge`,
        `godkjenning dokumentasjon annen forelder jobber skift turnus utenfor nærområdet og ansvar barn tom 7 klasse eller barn med spesielle behov under 18 - ikke jobbe hele Norge`,
        `dokumentasjon kan ikke ta alle typer arbeid - ikke jobbe hele Norge`,
        `godkjenning dokumentasjon kan ikke ta alle typer arbeid - ikke jobbe hele Norge`
    ).toIntArray()
}

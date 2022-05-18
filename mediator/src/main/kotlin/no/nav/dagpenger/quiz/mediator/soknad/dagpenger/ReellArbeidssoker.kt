package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.flervalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object ReellArbeidssoker : DslFaktaseksjon {

    const val `Kan jobbe heltid` = 1
    const val `Årsak til kun deltid` = 2
    const val `Skriv kort om situasjonen din` = 3
    const val `Antall timer deltid du kan jobbe` = 4
    const val `Kan du jobbe i hele Norge` = 5
    const val `Kan ikke jobbe i hele Norge` = 6
    const val `Kort om hvorfor ikke jobbe hele norge` = 7
    const val `Kan ta alle typer arbeid` = 8
    const val `Kan bytte yrke og eller gå ned i lønn` = 9

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
        heltall faktum "faktum.kun-deltid-aarsak-antall-timer" id `Antall timer deltid du kan jobbe` avhengerAv `Årsak til kun deltid`,
        tekst faktum "faktum.kort-om-hvorfor-kun-deltid" id `Skriv kort om situasjonen din`,
        boolsk faktum "faktum.jobbe-hele-norge" id `Kan du jobbe i hele Norge`,
        flervalg faktum "faktum.ikke-jobbe-hele-norge"
            med "svar.redusert-helse"
            med "svar.omsorg-baby"
            med "svar.eneansvar-barn"
            med "svar.omsorg-barn-spesielle-behov"
            med "svar.skift-turnus"
            med "svar.har-fylt-60"
            med "svar.annen-situasjon" id `Kan ikke jobbe i hele Norge` avhengerAv `Kan du jobbe i hele Norge`,
        tekst faktum "faktum.kort-om-hvorfor-ikke-jobbe-hele-norge" id `Kort om hvorfor ikke jobbe hele norge` avhengerAv `Kan du jobbe i hele Norge`,
        boolsk faktum "faktum.alle-typer-arbeid" id `Kan ta alle typer arbeid` avhengerAv `Kan du jobbe i hele Norge` og `Kort om hvorfor ikke jobbe hele norge`, // TODO: Avhenger dette faktumet av 2 fakta?
        boolsk faktum "faktum.bytte-yrke-ned-i-lonn" id `Kan bytte yrke og eller gå ned i lønn` avhengerAv `Kan ta alle typer arbeid`
    )

    fun regeltre(søknad: Søknad): Subsumsjon = with(søknad) {
        "Er reel arbeidssøker".minstEnAv(
            `Jobbe fulltid`(),
            `Jobbe deltid`()
        ) hvisOppfylt {
            `Kan flytte for arbeid`() hvisOppfylt {
                `Kan jobbe med hva som helst`() hvisOppfylt {
                    `Kan bytte yrke og eller gå ned i lønn`()
                }
            }
        }
    }

    private fun Søknad.`Kan bytte yrke og eller gå ned i lønn`() =
        "Bytte yrke og/eller gå ned i lønn".minstEnAv(
            boolsk(`Kan bytte yrke og eller gå ned i lønn`) er true,
            boolsk(`Kan bytte yrke og eller gå ned i lønn`) er false
        )

    private fun Søknad.`Kan jobbe med hva som helst`() = "Jobbe med hva som helst".minstEnAv(
        `Alle typer jobb`(),
        `Ikke alle typer jobb`()
    )

    private fun Søknad.`Kan flytte for arbeid`() = "Kan flytte for arbeid".minstEnAv(
        `Jobbe i hele Norge`(),
        `Ikke jobbe i hele Norge`()
    )

    private fun Søknad.`Ikke alle typer jobb`() = boolsk(`Kan ta alle typer arbeid`) er false

    private fun Søknad.`Alle typer jobb`() = boolsk(`Kan ta alle typer arbeid`) er true

    private fun Søknad.`Jobbe fulltid`() = boolsk(`Kan jobbe heltid`) er true

    private fun Søknad.`Jobbe deltid`() =
        boolsk(`Kan jobbe heltid`) er false hvisOppfylt {
            `Årsak til deltid`() hvisOppfylt {
                heltall(`Antall timer deltid du kan jobbe`).utfylt()
            }
        }

    private fun Søknad.`Ikke jobbe i hele Norge`() =
        boolsk(`Kan du jobbe i hele Norge`) er false hvisOppfylt {
            `Årsak kan ikke jobbe i hele Norge`()
        }

    private fun Søknad.`Jobbe i hele Norge`() =
        boolsk(`Kan du jobbe i hele Norge`) er true

    private fun Søknad.`Årsak kan ikke jobbe i hele Norge`() =
        flervalg(`Kan ikke jobbe i hele Norge`).utfylt() hvisOppfylt {
            "Årsak til at man ikke kan jobbe i hele Norge".minstEnAv(
                flervalg(`Kan ikke jobbe i hele Norge`) er Flervalg("faktum.ikke-jobbe-hele-norge.svar.redusert-helse"),
                flervalg(`Kan ikke jobbe i hele Norge`) er Flervalg("faktum.ikke-jobbe-hele-norge.svar.omsorg-baby"),
                flervalg(`Kan ikke jobbe i hele Norge`) er Flervalg("faktum.ikke-jobbe-hele-norge.svar.eneansvar-barn"),
                flervalg(`Kan ikke jobbe i hele Norge`) er Flervalg("faktum.ikke-jobbe-hele-norge.svar.omsorg-barn-spesielle-behov"),
                flervalg(`Kan ikke jobbe i hele Norge`) er Flervalg("faktum.ikke-jobbe-hele-norge.svar.skift-turnus"),
                flervalg(`Kan ikke jobbe i hele Norge`) er Flervalg("faktum.ikke-jobbe-hele-norge.svar.har-fylt-60"),
                flervalg(`Kan ikke jobbe i hele Norge`) er Flervalg("faktum.ikke-jobbe-hele-norge.svar.annen-situasjon") hvisOppfylt {
                    tekst(`Kort om hvorfor ikke jobbe hele norge`).utfylt()
                }
            )
        }

    private fun Søknad.`Årsak til deltid`() =
        flervalg(`Årsak til kun deltid`).utfylt() hvisOppfylt {
            "Årsak til deltid".minstEnAv(
                `Redusert helse`(),
                `Omsorg for baby`(),
                `Eneansvar for barn`(),
                `Omsorg for barn med spesielle behov`(),
                `Den andre foreldren jobber utenfor nærområdet, eller turnus skift`(),
                `Over 60 år`(),
                `Annen situasjon som ikke omfattes av unntak`() hvisOppfylt {
                    tekst(`Skriv kort om situasjonen din`).utfylt()
                },
            )
        }

    private fun Søknad.`Over 60 år`() =
        flervalg(`Årsak til kun deltid`) er Flervalg("faktum.kun-deltid-aarsak.svar.har-fylt-60")

    private fun Søknad.`Den andre foreldren jobber utenfor nærområdet, eller turnus skift`() =
        flervalg(`Årsak til kun deltid`) er Flervalg("faktum.kun-deltid-aarsak.svar.skift-turnus")

    private fun Søknad.`Omsorg for barn med spesielle behov`() =
        flervalg(`Årsak til kun deltid`) er Flervalg("faktum.kun-deltid-aarsak.svar.omsorg-barn-spesielle-behov")

    private fun Søknad.`Eneansvar for barn`() =
        flervalg(`Årsak til kun deltid`) er Flervalg("faktum.kun-deltid-aarsak.svar.eneansvar-barn")

    private fun Søknad.`Omsorg for baby`() =
        flervalg(`Årsak til kun deltid`) er Flervalg("faktum.kun-deltid-aarsak.svar.omsorg-baby")

    private fun Søknad.`Redusert helse`() =
        flervalg(`Årsak til kun deltid`) er Flervalg("faktum.kun-deltid-aarsak.svar.redusert-helse")

    private fun Søknad.`Annen situasjon som ikke omfattes av unntak`() =
        flervalg(`Årsak til kun deltid`) er Flervalg("faktum.kun-deltid-aarsak.svar.annen-situasjon")

    override fun seksjon(søknad: Søknad) =
        listOf(søknad.seksjon("reell-arbeidssoker", Rolle.søker, *this.databaseIder()))
}

package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.flervalg
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
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

    const val `Kan jobbe hel og deltid` = 1
    const val `Årsak til kun deltid` = 2
    const val `kort om hvorfor kun deltid` = 3
    const val `kun deltid aarsak antall timer` = 4
    const val `jobbe hele norge` = 5
    const val `ikke jobbe hele norge` = 6
    const val `kort om hvorfor ikke jobbe hele norge` = 7
    const val `alle typer arbeid` = 8
    const val `bytte yrke ned i lonn` = 9

    // TODO: Konstanter med flervalgsvar?
    override val fakta = listOf(
        boolsk faktum "faktum.jobbe-hel-deltid" id `Kan jobbe hel og deltid`,
        flervalg faktum "faktum.kun-deltid-aarsak"
            med "svar.redusert-helse"
            med "svar.omsorg-baby"
            med "svar.eneansvar-barn"
            med "svar.omsorg-barn-spesielle-behov"
            med "svar.skift-turnus"
            med "svar.har-fylt-60"
            med "svar.annen-situasjon" id `Årsak til kun deltid`,
        tekst faktum "faktum.kort-om-hvorfor-kun-deltid" id `kort om hvorfor kun deltid`,
        heltall faktum "faktum.kun-deltid-aarsak-antall-timer" id `kun deltid aarsak antall timer`,
        boolsk faktum "faktum.jobbe-hele-norge" id `jobbe hele norge`,
        flervalg faktum "faktum.ikke-jobbe-hele-norge"
            med "svar.redusert-helse"
            med "svar.omsorg-baby"
            med "svar.eneansvar-barn"
            med "svar.omsorg-barn-spesielle-behov"
            med "svar.skift-turnus"
            med "svar.har-fylt-60"
            med "svar.annen-situasjon" id `ikke jobbe hele norge`,
        tekst faktum "faktum.kort-om-hvorfor-ikke-jobbe-hele-norge" id `kort om hvorfor ikke jobbe hele norge`,
        boolsk faktum "faktum.alle-typer-arbeid" id `alle typer arbeid`,
        boolsk faktum "faktum.bytte-yrke-ned-i-lonn" id `bytte yrke ned i lonn`
    )

    fun regeltre(søknad: Søknad): Subsumsjon = with(søknad) {
        "Er reel arbeidssøker".minstEnAv(
            boolsk(`Kan jobbe hel og deltid`) er true,
            boolsk(`Kan jobbe hel og deltid`) er false hvisOppfylt {
                flervalg(`Årsak til kun deltid`).utfylt()
            }
        )
    }

    override fun seksjon(søknad: Søknad) = listOf(søknad.seksjon("reell-arbeidssoker", Rolle.søker, *this.databaseIder()))
}

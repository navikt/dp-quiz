package no.nav.dagpenger.quiz.mediator.soknad

import mu.KotlinLogging
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.regel.dokumenteresAv
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfylt
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger.Subsumsjoner.regeltre

internal object Dagpenger {

    private val logger = KotlinLogging.logger { }

    val VERSJON_ID = Prosessversjon(Prosess.Dagpenger, 101)

    const val `Har du hatt dagpenger i løpet av de siste 52 ukene` = 1
    const val `Villig til å ta hel og deltidsjobb` = 2
    const val `Villig til å ta arbeid i hele Norge` = 3
    const val `Villig til å ta alle typer arbeid` = 4
    const val `Villig til å ta ethvert arbeid` = 5
    const val `Avtjent militærtjeneste minst 3 av siste 6 mnd` = 6
    const val `Redusert helse, fysisk eller psykisk` = 7
    const val `Bekreftelse fra relevant fagpersonell` = 8

    fun registrer(registrer: (søknad: Søknad) -> Unit) {
        registrer(søknad)
    }

    internal val søknad: Søknad
        get() = Søknad(
            VERSJON_ID,
            boolsk faktum "Har du hatt dagpenger siste 52 uker" id `Har du hatt dagpenger i løpet av de siste 52 ukene`,
            boolsk faktum "Som hovedregel må du være villig til å ta både hel- og deltidsjobb for å ha rett til dagpenger" id `Villig til å ta hel og deltidsjobb`,
            boolsk faktum "Som hovedregel må du være villig til å ta arbeid i hele Norge for å ha rett til dagpenger" id `Villig til å ta arbeid i hele Norge`,
            boolsk faktum "Som hovedregel må du kunne ta alle typer arbeid for å ha rett til dagpenger" id `Villig til å ta alle typer arbeid`,
            boolsk faktum "Som hovedregel må du være villig til å ta ethvert arbeid du er kvalifisert for. Dette gjelder også innenfor yrker du ikke er utdannet til eller har arbeidserfaring fra. Du må også være villig til å gå ned i lønn." id `Villig til å ta ethvert arbeid`,
            boolsk faktum "Du kan ha rett til dagpenger etter særlige regler hvis du har avtjent militærtjeneste eller obligatorisk sivilforsvarstjeneste i minst tre av de siste tolv månedene" id `Avtjent militærtjeneste minst 3 av siste 6 mnd`,
            boolsk faktum "Redusert helse, fysisk eller psykisk" id `Redusert helse, fysisk eller psykisk` avhengerAv `Villig til å ta hel og deltidsjobb`,
            dokument faktum "Bekreftelse fra relevant fagpersonell" id `Bekreftelse fra relevant fagpersonell` avhengerAv `Redusert helse, fysisk eller psykisk`
        )

    private object Seksjoner {
        val gjenopptak = with(søknad) {
            Seksjon("Gjenopptak", Rolle.søker, dato(`Har du hatt dagpenger i løpet av de siste 52 ukene`))
        }
        val reellArbeidsøker = søknad.seksjon(
            "Er reell arbeidssøker",
            Rolle.søker,
            `Villig til å ta hel og deltidsjobb`,
            `Villig til å ta arbeid i hele Norge`,
            `Villig til å ta alle typer arbeid`,
            `Villig til å ta ethvert arbeid`,
        )

        val unntakReellArbeidsøker = søknad.seksjon(

            "Reell arbeidssøker unntak",
            Rolle.søker,
            `Redusert helse, fysisk eller psykisk`,
            `Bekreftelse fra relevant fagpersonell`,
        )

        val verneplikt = søknad
            .seksjon(
                "Har avtjent verneplikt",
                Rolle.søker,
                `Avtjent militærtjeneste minst 3 av siste 6 mnd`
            )
    }

    internal val søknadsprosess: Søknadprosess =
        Søknadprosess(
            Seksjoner.gjenopptak,
            Seksjoner.reellArbeidsøker,
            Seksjoner.unntakReellArbeidsøker,
            Seksjoner.verneplikt,
        )

    object Subsumsjoner {
        val villigTilHeltidOgDeltid = with(søknad) {
            (boolsk(`Villig til å ta hel og deltidsjobb`) er true).hvisIkkeOppfylt {
                "svare på".minstEnAv(
                    (boolsk(`Redusert helse, fysisk eller psykisk`) er true).hvisOppfylt {
                        boolsk(`Redusert helse, fysisk eller psykisk`).dokumenteresAv(
                            dokument(
                                `Bekreftelse fra relevant fagpersonell`
                            )
                        )
                    }
                )
            }
        }

        val reellArbeidsøker = with(søknad) {
            "er reell arbeidssøker hvis".alle(
                villigTilHeltidOgDeltid,
                boolsk(`Villig til å ta arbeid i hele Norge`) er true,
                boolsk(`Villig til å ta alle typer arbeid`) er true,
                boolsk(`Villig til å ta ethvert arbeid`) er true
            )
        }

        val regeltre = with(søknad) {
            reellArbeidsøker.hvisOppfylt {
                boolsk(`Avtjent militærtjeneste minst 3 av siste 6 mnd`) er true
            }
        }
    }

    private val versjon = Versjon.Bygger(
        prototypeSøknad = søknad,
        prototypeSubsumsjon = regeltre,
        prototypeUserInterfaces = mapOf(
            Versjon.UserInterfaceType.Web to søknadsprosess
        ),
        faktumNavBehov = null
    ).registrer().also {
        logger.info { "\n\n\nREGISTRERT versjon id $VERSJON_ID \n\n\n\n" }
    }
}

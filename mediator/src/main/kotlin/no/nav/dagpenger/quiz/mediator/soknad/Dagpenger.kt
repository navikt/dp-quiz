package no.nav.dagpenger.quiz.mediator.soknad

import mu.KotlinLogging
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon

internal object Dagpenger {

    private val logger = KotlinLogging.logger { }

    const val VERSJON_ID = 101

    const val `Har du hatt dagpenger i løpet av de siste 52 ukene` = 1
    const val `Villig til å ta hel og deltidsjobb` = 2
    const val `Villig til å ta arbeid i hele Norge` = 3
    const val `Villig til å ta alle typer arbeid` = 4
    const val `Villig til å ta ethvert arbeid` = 5
    const val `Avtjent militærtjeneste minst 3 av siste 6 mnd` = 6

    fun registrer(registrer: (søknad: Søknad, versjonId: Int) -> Unit) {
        registrer(søknad, VERSJON_ID)
    }

    internal val søknad: Søknad
        get() = Søknad(
            VERSJON_ID,
            boolsk faktum "Har du hatt dagpenger siste 52 uker" id `Har du hatt dagpenger i løpet av de siste 52 ukene`,
            boolsk faktum "Som hovedregel må du være villig til å ta både hel- og deltidsjobb for å ha rett til dagpenger" id `Villig til å ta hel og deltidsjobb`,
            boolsk faktum "Som hovedregel må du være villig til å ta arbeid i hele Norge for å ha rett til dagpenger" id `Villig til å ta arbeid i hele Norge`,
            boolsk faktum "Som hovedregel må du kunne ta alle typer arbeid for å ha rett til dagpenger" id `Villig til å ta alle typer arbeid`,
            boolsk faktum "Som hovedregel må du være villig til å ta ethvert arbeid du er kvalifisert for. Dette gjelder også innenfor yrker du ikke er utdannet til eller har arbeidserfaring fra. Du må også være villig til å gå ned i lønn." id `Villig til å ta ethvert arbeid`,
            boolsk faktum "Du kan ha rett til dagpenger etter særlige regler hvis du har avtjent militærtjeneste eller obligatorisk sivilforsvarstjeneste i minst tre av de siste tolv månedene" id `Avtjent militærtjeneste minst 3 av siste 6 mnd`
        )

    private object Seksjoner {
        val dagpenger = with(søknad) {
            Seksjon("gjenopptak", Rolle.søker, dato(`Har du hatt dagpenger i løpet av de siste 52 ukene`))
            Seksjon(
                "er reel arbeidssøker",
                Rolle.søker,
                boolsk(`Villig til å ta hel og deltidsjobb`),
                boolsk(`Villig til å ta arbeid i hele Norge`),
                boolsk(`Villig til å ta alle typer arbeid`),
                boolsk(`Villig til å ta ethvert arbeid`)
            )
            Seksjon("Verneplikt", Rolle.søker, boolsk(`Avtjent militærtjeneste minst 3 av siste 6 mnd`))
        }
    }

    internal val søknadsprosess: Søknadprosess =
        Søknadprosess(
            Seksjoner.dagpenger
        )

    val regeltre = with(søknad) {
        boolsk(`Har du hatt dagpenger i løpet av de siste 52 ukene`) er true
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

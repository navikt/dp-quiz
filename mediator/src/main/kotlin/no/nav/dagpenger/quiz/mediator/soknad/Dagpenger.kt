package no.nav.dagpenger.quiz.mediator.soknad

import mu.KotlinLogging
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.periode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger.Subsumsjoner.regeltre

internal object Dagpenger {

    private val logger = KotlinLogging.logger { }

    val VERSJON_ID = Prosessversjon(Prosess.Dagpenger, 201)

    const val `Har du hatt dagpenger i løpet av de siste 52 ukene` = 1
    const val `Villig til å ta hel og deltidsjobb` = 2
    const val `Villig til å ta arbeid i hele Norge` = 3
    const val `Villig til å ta alle typer arbeid` = 4
    const val `Villig til å ta ethvert arbeid` = 5
    const val `Avtjent militærtjeneste minst 3 av siste 6 mnd` = 6
    const val `Redusert helse, fysisk eller psykisk` = 7
    const val `Bekreftelse fra relevant fagpersonell` = 8
    const val arbeidsforhold = 9
    const val `arbeidsforhold fra og med` = 10
    const val `arbeidsforhold til og med` = 11
    const val `personalia alder` = 12
    const val `personalia navn` = 13
    const val `en eller annen period` = 14

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
            dokument faktum "Bekreftelse fra relevant fagpersonell" id `Bekreftelse fra relevant fagpersonell` avhengerAv `Redusert helse, fysisk eller psykisk`,
            heltall faktum "faktum.arbeidsforhold" id arbeidsforhold genererer `arbeidsforhold fra og med` og `arbeidsforhold til og med`,
            dato faktum "faktum.arbeidsforhold.fom" id `arbeidsforhold fra og med`,
            dato faktum "faktum.arbeidsforhold.tom" id `arbeidsforhold til og med`,
            heltall faktum "faktum.personalia.alder" id `personalia alder`,
            tekst faktum "faktum.person.navn" id `personalia navn`,
            periode faktum "faktum.test.periode" id `en eller annen period`

        )

    private object Seksjoner {

        val søkerSeksjon = søknad.seksjon(
            "søkerseksjon",
            Rolle.søker,
            `Villig til å ta hel og deltidsjobb`,
            `Villig til å ta arbeid i hele Norge`,
            `Villig til å ta alle typer arbeid`,
            `Villig til å ta ethvert arbeid`,
            `Redusert helse, fysisk eller psykisk`,
            `Bekreftelse fra relevant fagpersonell`,
            `Avtjent militærtjeneste minst 3 av siste 6 mnd`
        )

        val navSeksjon = søknad.seksjon(
            "navseksjon",
            Rolle.nav,
            arbeidsforhold,
            `arbeidsforhold fra og med`,
            `arbeidsforhold til og med`,
            `personalia alder`,
            `personalia navn`,
            `en eller annen period`,
        )
    }

    internal val søknadsprosess: Søknadprosess =
        Søknadprosess(
            Seksjoner.søkerSeksjon,
            Seksjoner.navSeksjon
        )

    object Subsumsjoner {

        val regeltre = with(søknad) {
            heltall(arbeidsforhold) minst (0)
            heltall(`personalia alder`) minst (0)
        }
    }

    private val faktumNavBehov =
        FaktumNavBehov(
            mapOf(
                arbeidsforhold to "Arbeidsforhold",
                `personalia alder` to "PersonaliaAlder",
                `personalia navn` to "PersonaliaNavn",
                `en eller annen period` to "enEllerAnnenPeriod"
            )
        )

    private val versjon = Versjon.Bygger(
        prototypeSøknad = søknad,
        prototypeSubsumsjon = regeltre,
        prototypeUserInterfaces = mapOf(
            Versjon.UserInterfaceType.Web to søknadsprosess
        ),
        faktumNavBehov = faktumNavBehov
    ).registrer().also {
        logger.info { "\n\n\nREGISTRERT versjon id $VERSJON_ID \n\n\n\n" }
    }
}

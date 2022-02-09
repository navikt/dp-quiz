package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import mu.KotlinLogging
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Dagpenger.Subsumsjoner.regeltre

internal object Dagpenger {

    private val logger = KotlinLogging.logger { }

    val VERSJON_ID = Prosessversjon(Prosess.Dagpenger, 208)

    fun registrer(registrer: (søknad: Søknad) -> Unit) {
        registrer(søknad)
    }

    internal val søknad: Søknad
        get() = Søknad(
            VERSJON_ID,
            *AndreYtelser.fakta(),
            *Arbeidsforhold.fakta(),
            *Barnetillegg.fakta(),
            *Bostedsland.fakta(),
            *EgenNæring.fakta(),
            *EøsArbeidsforhold.fakta(),
            *KoronaFortsattRett.fakta(),
            *ReellArbeidssoker.fakta(),
            *Tilleggsopplysninger.fakta(),
            *Utdanning.fakta(),
            *Verneplikt.fakta()
        )

    private object Seksjoner {

        val søkerSeksjon = søknad.seksjon(
            "søkerseksjon",
            Rolle.søker,
            *AndreYtelser.databaseIder(),
            *Arbeidsforhold.databaseIder(),
            *Barnetillegg.databaseIder(),
            *Bostedsland.databaseIder(),
            *EgenNæring.databaseIder(),
            *EøsArbeidsforhold.databaseIder(),
            *KoronaFortsattRett.databaseIder(),
            *ReellArbeidssoker.databaseIder(),
            *Tilleggsopplysninger.databaseIder(),
            *Utdanning.databaseIder(),
            *Verneplikt.databaseIder()
        )

        val navSeksjon = søknad.seksjon(
            "navseksjon",
            Rolle.nav,
            *Barnetillegg.databaseIder(),
        )
    }

    internal val søknadsprosess: Søknadprosess =
        Søknadprosess(
            Seksjoner.søkerSeksjon,
            Seksjoner.navSeksjon
        )

    object Subsumsjoner {
        val regeltre: Subsumsjon = with(søknad) {
            heltall(Barnetillegg.`barn liste`) minst (0)
        }
    }

    private val faktumNavBehov =
        FaktumNavBehov(
            mapOf(
                Barnetillegg.`barn liste` to "Barn",
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

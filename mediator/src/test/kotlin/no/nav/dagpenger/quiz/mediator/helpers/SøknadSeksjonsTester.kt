package no.nav.dagpenger.quiz.mediator.helpers

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
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.DummySeksjon

internal object SøknadSeksjonsTester {

    private val logger = KotlinLogging.logger { }

    val VERSJON_ID = Prosessversjon(Prosess.Dagpenger, -1)

    fun registrer(registrer: (søknad: Søknad) -> Unit) {
        registrer(søknad)
    }

    internal val søknad: Søknad
        get() = Søknad(
            VERSJON_ID,
            *DummySeksjon.fakta(),
        )

    private object Seksjoner {
        val søkerSeksjon = søknad.seksjon(
            "søkerseksjon",
            Rolle.søker,
            *DummySeksjon.variabler(),
        )

        val navSeksjon = søknad.seksjon(
            "navseksjon",
            Rolle.nav,
        )
    }

    private val søknadsprosess: Søknadprosess =
        Søknadprosess(
            Seksjoner.søkerSeksjon,
            Seksjoner.navSeksjon
        )

    object Subsumsjoner {
        val regeltre: Subsumsjon =
            with(søknad) {
                "alle".minstEnAv(
                    heltall(DummySeksjon.`dummy int`) minst (0)
                )
            }
    }

    private val faktumNavBehov = FaktumNavBehov(emptyMap())

    private val versjon = Versjon.Bygger(
        prototypeSøknad = søknad,
        prototypeSubsumsjon = Subsumsjoner.regeltre,
        prototypeUserInterfaces = mapOf(
            Versjon.UserInterfaceType.Web to søknadsprosess
        ),
        faktumNavBehov = faktumNavBehov
    ).registrer().also {
        logger.info { "\n\n\nREGISTRERT versjon id $VERSJON_ID \n\n\n\n" }
    }
}

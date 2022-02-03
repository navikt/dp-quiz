package no.nav.dagpenger.quiz.mediator.soknad

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
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger.Subsumsjoner.regeltre
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.DummySeksjon

internal object Dagpenger {

    private val logger = KotlinLogging.logger { }

    val VERSJON_ID = Prosessversjon(Prosess.Dagpenger, 205)

    fun registrer(registrer: (søknad: Søknad) -> Unit) {
        registrer(søknad)
    }

    internal val søknad: Søknad
        get() {
            val søknad1 = Søknad(
                VERSJON_ID,
                *(DummySeksjon.faktaMap.toTypedArray()),
                *(DummySeksjon.faktaMapGenerator.toTypedArray()),
            )
            return søknad1
        }

    private object Seksjoner {

        val søkerSeksjon = søknad.seksjon(
            "søkerseksjon",
            Rolle.søker,
            *(DummySeksjon.alleVariablerISeksjonen.toTypedArray()).toIntArray(),
            *(DummySeksjon.alleGeneratorVariabler.toTypedArray()).toIntArray()
        )

        val navSeksjon = søknad.seksjon(
            "navseksjon",
            Rolle.nav,
        )
    }

    internal val søknadsprosess: Søknadprosess =
        Søknadprosess(
            Seksjoner.søkerSeksjon,
            Seksjoner.navSeksjon
        )

    object Subsumsjoner {

        val regeltre: Subsumsjon = with(søknad) {
            "alle".minstEnAv(
                heltall(DummySeksjon.`dummy-int`) minst (0)
            )
        }
    }

    private val faktumNavBehov =
        FaktumNavBehov(
            mapOf(
                DummySeksjon.`dummy-int` to "trengerEtHeltall",
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

package no.nav.dagpenger.quiz.mediator.soknad

import mu.KotlinLogging
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon

internal object Gjenopptak {

    private val logger = KotlinLogging.logger { }

    const val VERSJON_ID = 100

    const val `Har du hatt dagpenger i løpet av de siste 52 ukene` = 1

    fun registrer(registrer: (søknad: Søknad, versjonId: Int) -> Unit) {
        registrer(søknad, VERSJON_ID)
    }

    internal val søknad: Søknad
        get() = Søknad(
            VERSJON_ID,
            boolsk faktum "Har du hatt dagpenger siste 52 uker" id `Har du hatt dagpenger i løpet av de siste 52 ukene`,
        )

    private object Seksjoner {
        val gjenopptak = with(søknad) {
            Seksjon("gjenopptak", Rolle.søker, dato(`Har du hatt dagpenger i løpet av de siste 52 ukene`))
        }
    }

    internal val søknadsprosess: Søknadprosess =
        Søknadprosess(
            Seksjoner.gjenopptak
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

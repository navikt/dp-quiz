package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import mu.KotlinLogging
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Dagpenger.Subsumsjoner.regeltre

internal object Dagpenger {

    private val logger = KotlinLogging.logger { }

    val VERSJON_ID = Prosessversjon(Prosess.Dagpenger, 215)

    fun registrer(registrer: (søknad: Søknad) -> Unit) {
        registrer(søknad)
    }

    private val faktaseksjoner = listOf(
        AndreYtelser,
        Arbeidsforhold,
        Barnetillegg,
        Bosted,
        EgenNæring,
        EøsArbeidsforhold,
        ReellArbeidssoker,
        Tilleggsopplysninger,
        Utdanning,
        Verneplikt,
        DokumentasjonsKrav
    )

    private val alleFakta = flatMapAlleFakta()
    private val alleSeksjoner = flatMapAlleSeksjoner()

    private val søknad: Søknad
        get() = Søknad(
            VERSJON_ID,
            *alleFakta
        )

    internal val søknadsprosess: Søknadprosess = Søknadprosess(*alleSeksjoner)

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

    @Suppress("unused")
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

    private fun flatMapAlleFakta() = faktaseksjoner.flatMap { seksjon ->
        seksjon.fakta().toList()
    }.toTypedArray()

    private fun flatMapAlleSeksjoner() = faktaseksjoner.map { faktaSeksjon ->
        faktaSeksjon.seksjon(søknad)
    }.flatten().toTypedArray()
}

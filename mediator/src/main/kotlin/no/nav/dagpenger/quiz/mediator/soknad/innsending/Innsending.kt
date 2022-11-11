package no.nav.dagpenger.quiz.mediator.soknad.innsending

import mu.KotlinLogging
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.innsending.Innsending.Subsumsjoner.regeltre

internal object Innsending {
    private val logger = KotlinLogging.logger { }
    val VERSJON_ID = Prosessversjon(Prosess.Innsending, 6)

    fun registrer(registrer: (prototype: Søknad) -> Unit) {
        registrer(prototypeSøknad)
    }

    private val faktaseksjoner = listOf(
        Hvorfor
    )
    private val alleFakta = flatMapAlleFakta()
    private val alleSeksjoner = flatMapAlleSeksjoner()
    private val prototypeSøknad: Søknad
        get() = Søknad(
            VERSJON_ID,
            *alleFakta
        )
    private val søknadsprosess: Søknadprosess = Søknadprosess(*alleSeksjoner)

    object Subsumsjoner {
        val regeltre: Subsumsjon = with(prototypeSøknad) {
            Hvorfor.regeltre(this)
        }
    }

    private val faktumNavBehov =
        FaktumNavBehov(
            emptyMap()
        )

    init {
        Versjon.Bygger(
            prototypeSøknad = prototypeSøknad,
            prototypeSubsumsjon = regeltre,
            prototypeUserInterfaces = mapOf(
                Versjon.UserInterfaceType.Web to søknadsprosess
            ),
            faktumNavBehov = faktumNavBehov
        ).registrer().also {
            logger.info { "\n\n\nREGISTRERT versjon id $VERSJON_ID \n\n\n\n" }
        }
    }

    private fun flatMapAlleFakta() = faktaseksjoner.flatMap { seksjon ->
        seksjon.fakta().toList()
    }.toTypedArray()

    private fun flatMapAlleSeksjoner() = faktaseksjoner.map { faktaSeksjon ->
        faktaSeksjon.seksjon(prototypeSøknad)
    }.flatten().toTypedArray()
}

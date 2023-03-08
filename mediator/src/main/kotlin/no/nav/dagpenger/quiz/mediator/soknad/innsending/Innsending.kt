package no.nav.dagpenger.quiz.mediator.soknad.innsending

import mu.KotlinLogging
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.seksjon.Henvendelser
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.quiz.mediator.soknad.Prosesser
import no.nav.dagpenger.quiz.mediator.soknad.Prosessfakta
import no.nav.dagpenger.quiz.mediator.soknad.innsending.Innsending.Subsumsjoner.regeltre

internal object Innsending {
    private val logger = KotlinLogging.logger { }
    val VERSJON_ID = Faktaversjon(Prosessfakta.Innsending, 6)

    fun registrer(registrer: (prototypeProsess: Prosess) -> Unit = {}) {
        registrer(søknadsprosess)
    }

    private val faktaseksjoner = listOf(
        GenerellInnsending,
    )
    private val alleFakta = flatMapAlleFakta()
    private val alleSeksjoner = flatMapAlleSeksjoner()
    private val prototypeFakta: Fakta
        get() = Fakta(
            VERSJON_ID,
            *alleFakta,
        )
    private val søknadsprosess: Prosess = Prosess(
        Prosesser.Innsending,
        *alleSeksjoner,
    )

    object Subsumsjoner {
        val regeltre: Subsumsjon = with(prototypeFakta) {
            GenerellInnsending.regeltre(this)
        }
    }

    private val faktumNavBehov =
        FaktumNavBehov(
            emptyMap(),
        )

    init {
        Henvendelser.FaktaBygger(
            prototypeFakta,
            faktumNavBehov,
        ).also {
            it.leggTilProsess(søknadsprosess, regeltre)
            it.registrer()
        }
        logger.info { "\n\n\nREGISTRERT versjon id $VERSJON_ID \n\n\n\n" }
    }

    private fun flatMapAlleFakta() = faktaseksjoner.flatMap { seksjon ->
        seksjon.fakta().toList()
    }.toTypedArray()

    private fun flatMapAlleSeksjoner() = faktaseksjoner.map { faktaSeksjon ->
        faktaSeksjon.seksjon(prototypeFakta)
    }.flatten().toTypedArray()
}

package no.nav.dagpenger.quiz.mediator.soknad.aldersvurdering

import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett

internal object AldersvurderingOppsett {

    val VERSJON_ID = Prosessversjon(Prosess.Aldersvurdering, 1)

    fun registrer(registrer: (prototype: Søknad) -> Unit) {
        registrer(prototypeSøknad)
    }

    private val faktaseksjoner = listOf<DslFaktaseksjon>()
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
            Aldersvurdering.regeltre(this).hvisOppfylt {

            }
        }
    }

    private val faktumNavBehov =
        FaktumNavBehov(
            mapOf(
                AvslagPåMinsteinntektOppsett.ønsketDato to "ØnskerDagpengerFraDato",
                AvslagPåMinsteinntektOppsett.virkningsdato to "Virkningstidspunkt",
                AvslagPåMinsteinntektOppsett.over67årFradato to "ForGammelGrensedato",
            )
        )

    private fun flatMapAlleFakta() = faktaseksjoner.flatMap { seksjon ->
        seksjon.fakta().toList()
    }.toTypedArray()

    private fun flatMapAlleSeksjoner() = faktaseksjoner.map { faktaSeksjon ->
        faktaSeksjon.seksjon(AldersvurderingOppsett.prototypeSøknad)
    }.flatten().toTypedArray()
}
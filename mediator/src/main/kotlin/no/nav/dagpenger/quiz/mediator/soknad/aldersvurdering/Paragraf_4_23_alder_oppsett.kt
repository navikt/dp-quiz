package no.nav.dagpenger.quiz.mediator.soknad.aldersvurdering

import mu.KotlinLogging
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon
import no.nav.dagpenger.quiz.mediator.soknad.Prosess

/**
 *  POC - dele opp vilkårsvurdering i kap 4
 *
 * Denne blir brukt av dp-behandling for til å løse "vilkårsvurderingsbehov"
 * Vi er pt. usikre på formen på det enda, så denne blir borte/endret
 */

internal object Paragraf_4_23_alder_oppsett {
    val VERSJON_ID = Prosessversjon(Prosess.Paragraf_4_23_alder, 1)
    private val logger = KotlinLogging.logger { }
    private val faktaseksjoner = listOf<DslFaktaseksjon>(Paragraf_4_23_alder_vilkår)
    private val alleFakta = flatMapAlleFakta()
    private val alleSeksjoner = flatMapAlleSeksjoner()
    private val søknadsprosess: Søknadprosess = Søknadprosess(*alleSeksjoner)
    private val faktumNavBehov = FaktumNavBehov(
        mapOf(
            Paragraf_4_23_alder_vilkår.ønskerDagpengerFraDato to "ØnskerDagpengerFraDato",
            Paragraf_4_23_alder_vilkår.søknadInnsendtDato to "Søknadstidspunkt",
            Paragraf_4_23_alder_vilkår.fødselsdato to "Fødselsdato",
            Paragraf_4_23_alder_vilkår.innsendtSøknadId to "InnsendtSøknadsId"
        )
    )

    internal val prototypeSøknad: Søknad
        get() = Søknad(
            VERSJON_ID,
            *alleFakta
        )

    fun registrer(registrer: (prototype: Søknad) -> Unit) {
        registrer(prototypeSøknad)
    }

    object Subsumsjoner {
        val regeltre: Subsumsjon = with(prototypeSøknad) {
            Paragraf_4_23_alder_vilkår.regeltre(this)
        }
    }

    private fun flatMapAlleFakta() = faktaseksjoner.flatMap { seksjon ->
        seksjon.fakta().toList()
    }.toTypedArray()

    private fun flatMapAlleSeksjoner() = faktaseksjoner.map { faktaSeksjon ->
        faktaSeksjon.seksjon(prototypeSøknad)
    }.flatten().toTypedArray()

    init {
        Versjon.Bygger(
            prototypeSøknad = prototypeSøknad,
            prototypeSubsumsjon = Subsumsjoner.regeltre,
            prototypeUserInterfaces = mapOf(
                Versjon.UserInterfaceType.Web to søknadsprosess
            ),
            faktumNavBehov = faktumNavBehov
        ).registrer().also {
            logger.info { "\n\n\nREGISTRERT versjon id $VERSJON_ID} \n\n\n\n" }
        }
    }
}

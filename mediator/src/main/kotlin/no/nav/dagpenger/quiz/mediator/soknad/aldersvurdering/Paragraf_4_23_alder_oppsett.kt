package no.nav.dagpenger.quiz.mediator.soknad.aldersvurdering

import mu.KotlinLogging
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.grensedato67år
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Fakta.Companion.seksjon
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.seksjon.FaktaVersjonDingseboms
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Prosessversjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.quiz.mediator.soknad.Prosesser
import no.nav.dagpenger.quiz.mediator.soknad.Prosessfakta

/**
 *  POC - dele opp vilkårsvurdering i kap 4
 *
 * Denne blir brukt av dp-behandling for til å løse "vilkårsvurderingsbehov"
 * Vi er pt. usikre på formen på det enda, så denne blir borte/endret
 */
internal object Paragraf_4_23_alder_oppsett {
    val VERSJON_ID = Faktaversjon(Prosessfakta.Paragraf_4_23_alder, 2)
    const val virkningsdato = 1
    const val fødselsdato = 2
    const val grensedato = 3
    const val ønskerDagpengerFraDato = 4
    const val søknadInnsendtDato = 5
    const val innsendtSøknadId = 6
    internal val prototypeFakta: Fakta
        get() = Fakta(
            VERSJON_ID,
            dokument faktum "innsendtSøknadId" id innsendtSøknadId,
            dato faktum "ønskerDagpengerFra" id ønskerDagpengerFraDato avhengerAv innsendtSøknadId,
            dato faktum "søknadInnsendtDato" id søknadInnsendtDato avhengerAv innsendtSøknadId,
            maks dato "virkningsdato" av ønskerDagpengerFraDato og søknadInnsendtDato id virkningsdato,
            dato faktum "fødselsdato" id fødselsdato,
            grensedato67år dato "grensedato" av fødselsdato id grensedato,
        )
    private val logger = KotlinLogging.logger { }
    private val faktumNavBehov = FaktumNavBehov(
        mapOf(
            ønskerDagpengerFraDato to "ØnskerDagpengerFraDato",
            søknadInnsendtDato to "Søknadstidspunkt",
            fødselsdato to "Fødselsdato",
            innsendtSøknadId to "InnsendtSøknadsId",
        ),
    )

    fun registrer(registrer: (prototype: Fakta) -> Unit) {
        registrer(prototypeFakta)
    }

    object Subsumsjoner {
        val regeltre: Subsumsjon = with(prototypeFakta) {
            "søkeren må være under aldersgrense ved virkningstidspunkt".deltre {
                "under aldersgrense" deltre {
                    dato(virkningsdato) før dato(grensedato)
                }
            }
        }
    }

    internal val seksjon = with(prototypeFakta) {
        this.seksjon(
            "alder",
            Rolle.nav,
            innsendtSøknadId,
            ønskerDagpengerFraDato,
            søknadInnsendtDato,
            virkningsdato,
            fødselsdato,
            grensedato,
        )
    }
    internal val prosess: Prosess = Prosess(
        Prosesser.Paragraf_4_23_alder,
        seksjon,
    )

    init {
        FaktaVersjonDingseboms.Bygger(
            prototypeFakta,
            faktumNavBehov
        ).registrer()
        Prosessversjon.Bygger(
            Prosessfakta.Paragraf_4_23_alder,
            prototypeSubsumsjon = Subsumsjoner.regeltre,
            prosess = prosess,
        ).registrer().also {
            logger.info { "\n\n\nREGISTRERT versjon id $VERSJON_ID} \n\n\n\n" }
        }
    }
}

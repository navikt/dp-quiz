package no.nav.dagpenger.quiz.mediator.soknad.aldersvurdering

import mu.KotlinLogging
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.grensedato67år
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.quiz.mediator.soknad.Prosess

/**
 *  POC - dele opp vilkårsvurdering i kap 4
 *
 * Denne blir brukt av dp-behandling for til å løse "vilkårsvurderingsbehov"
 * Vi er pt. usikre på formen på det enda, så denne blir borte/endret
 */

internal object Paragraf_4_23_alder_oppsett {

    val VERSJON_ID = Prosessversjon(Prosess.Paragraf_4_23_alder, 2)

    const val virkningsdato = 1
    const val fødselsdato = 2
    const val grensedato = 3
    const val ønskerDagpengerFraDato = 4
    const val søknadInnsendtDato = 5
    const val innsendtSøknadId = 6
    internal val prototypeSøknad: Søknad
        get() = Søknad(
            VERSJON_ID,
            dokument faktum "innsendtSøknadId" id innsendtSøknadId,
            dato faktum "ønskerDagpengerFra" id ønskerDagpengerFraDato avhengerAv innsendtSøknadId,
            dato faktum "søknadInnsendtDato" id søknadInnsendtDato avhengerAv innsendtSøknadId,
            maks dato "virkningsdato" av ønskerDagpengerFraDato og søknadInnsendtDato id virkningsdato,
            dato faktum "fødselsdato" id fødselsdato,
            grensedato67år dato "grensedato" av fødselsdato id grensedato
        )

    private val logger = KotlinLogging.logger { }

    private val faktumNavBehov = FaktumNavBehov(
        mapOf(
            ønskerDagpengerFraDato to "ØnskerDagpengerFraDato",
            søknadInnsendtDato to "Søknadstidspunkt",
            fødselsdato to "Fødselsdato",
            innsendtSøknadId to "InnsendtSøknadsId"
        )
    )

    fun registrer(registrer: (prototype: Søknad) -> Unit) {
        registrer(prototypeSøknad)
    }

    object Subsumsjoner {
        val regeltre: Subsumsjon = with(prototypeSøknad) {
            "søkeren må være under aldersgrense ved virkningstidspunkt".deltre {
                "under aldersgrense" deltre {
                    dato(virkningsdato) før dato(grensedato)
                }
            }
        }
    }
    internal val seksjon = with(prototypeSøknad) {
        this.seksjon(
            "alder",
            Rolle.nav,
            innsendtSøknadId,
            ønskerDagpengerFraDato,
            søknadInnsendtDato,
            virkningsdato,
            fødselsdato,
            grensedato
        )
    }

    internal val søknadprosess: Søknadprosess = Søknadprosess(seksjon)

    init {
        Versjon.Bygger(
            prototypeSøknad = prototypeSøknad,
            prototypeSubsumsjon = Subsumsjoner.regeltre,
            prototypeUserInterfaces = mapOf(
                Versjon.UserInterfaceType.Web to søknadprosess
            ),
            faktumNavBehov = faktumNavBehov
        ).registrer().also {
            logger.info { "\n\n\nREGISTRERT versjon id $VERSJON_ID} \n\n\n\n" }
        }
    }
}
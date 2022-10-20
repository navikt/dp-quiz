package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import no.nav.dagpenger.quiz.mediator.behovløsere.MetadataStrategi
import no.nav.dagpenger.quiz.mediator.behovløsere.MetadataStrategi.Metadata
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.DagpengerMetadataStrategi
import no.nav.dagpenger.quiz.mediator.soknad.innsending.InnsendingMetadataStrategi
import java.util.UUID

internal class ProsessMetadataStrategi : MetadataStrategi {
    override fun metadata(søknadprosess: Søknadprosess): Metadata {
        return SkjemastrategiVelger(søknadprosess).skjemakodeStrategi()
    }

    private class SkjemastrategiVelger(private val søknadprosess: Søknadprosess) : SøknadprosessVisitor {
        private lateinit var metadata: Metadata

        init {
            søknadprosess.accept(this)
        }

        override fun preVisit(søknad: Søknad, prosessVersjon: Prosessversjon, uuid: UUID) {
            metadata = when (prosessVersjon.prosessnavn) {
                Prosess.Dagpenger -> DagpengerMetadataStrategi().metadata(søknadprosess)
                Prosess.Innsending -> InnsendingMetadataStrategi().metadata(søknadprosess)
                else -> throw IllegalArgumentException("Har ikke laget skjemakodestrategi for ${prosessVersjon.prosessnavn}")
            }
        }

        fun skjemakodeStrategi(): Metadata = metadata
    }
}

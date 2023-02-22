package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.visitor.ProsessVisitor
import no.nav.dagpenger.quiz.mediator.behovløsere.MetadataStrategi
import no.nav.dagpenger.quiz.mediator.behovløsere.MetadataStrategi.Metadata
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.DagpengerMetadataStrategi
import no.nav.dagpenger.quiz.mediator.soknad.innsending.InnsendingMetadataStrategi
import java.util.UUID

internal class ProsessMetadataStrategi : MetadataStrategi {
    override fun metadata(prosess: Prosess): Metadata {
        return SkjemastrategiVelger(prosess).skjemakodeStrategi()
    }

    private class SkjemastrategiVelger(private val prosess: Prosess) : ProsessVisitor {
        private lateinit var metadata: Metadata

        init {
            prosess.accept(this)
        }

        override fun preVisit(fakta: Fakta, faktaversjon: Faktaversjon, uuid: UUID) {
            metadata = when (faktaversjon.faktatype) {
                Prosessfakta.Dagpenger -> DagpengerMetadataStrategi().metadata(prosess)
                Prosessfakta.Innsending -> InnsendingMetadataStrategi().metadata(prosess)
                else -> throw IllegalArgumentException("Har ikke laget skjemakodestrategi for ${faktaversjon.faktatype}")
            }
        }

        fun skjemakodeStrategi(): Metadata = metadata
    }
}

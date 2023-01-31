package no.nav.dagpenger.quiz.mediator.soknad

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.HenvendelsesType
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import no.nav.dagpenger.quiz.mediator.behovløsere.MetadataStrategi
import no.nav.dagpenger.quiz.mediator.behovløsere.MetadataStrategi.Metadata
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.DagpengerMetadataStrategi
import no.nav.dagpenger.quiz.mediator.soknad.innsending.InnsendingMetadataStrategi
import java.util.UUID

internal class ProsessMetadataStrategi : MetadataStrategi {
    override fun metadata(utredningsprosess: Utredningsprosess): Metadata {
        return SkjemastrategiVelger(utredningsprosess).skjemakodeStrategi()
    }

    private class SkjemastrategiVelger(private val utredningsprosess: Utredningsprosess) : SøknadprosessVisitor {
        private lateinit var metadata: Metadata

        init {
            utredningsprosess.accept(this)
        }

        override fun preVisit(fakta: Fakta, prosessVersjon: HenvendelsesType, uuid: UUID) {
            metadata = when (prosessVersjon.prosessnavn) {
                Prosess.Dagpenger -> DagpengerMetadataStrategi().metadata(utredningsprosess)
                Prosess.Innsending -> InnsendingMetadataStrategi().metadata(utredningsprosess)
                else -> throw IllegalArgumentException("Har ikke laget skjemakodestrategi for ${prosessVersjon.prosessnavn}")
            }
        }

        fun skjemakodeStrategi(): Metadata = metadata
    }
}

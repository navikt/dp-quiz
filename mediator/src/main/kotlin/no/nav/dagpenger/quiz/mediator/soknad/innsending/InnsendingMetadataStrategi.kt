package no.nav.dagpenger.quiz.mediator.soknad.innsending

import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.quiz.mediator.behovløsere.MetadataStrategi
import no.nav.dagpenger.quiz.mediator.behovløsere.MetadataStrategi.Metadata

class InnsendingMetadataStrategi : MetadataStrategi {
    override fun metadata(søknadprosess: Søknadprosess) =
        Metadata("GENERELL_INNSENDING", "En generell henvendelse om Dagpenger")
}

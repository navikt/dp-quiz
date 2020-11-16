package no.nav.dagpenger.quiz.mediator.meldinger

import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.quiz.mediator.meldinger.model.BehovMelding
import no.nav.helse.rapids_rivers.RapidsConnection
import java.util.UUID

internal class BehovMediator(private val rapidsConnection: RapidsConnection) {

    internal fun håndter(seksjon: Seksjon, fnr: String, søknadId: UUID) {
        val melding = BehovMelding(fnr = fnr, seksjon = seksjon, søknadId)
        rapidsConnection.publish(melding.toJson())
    }
}

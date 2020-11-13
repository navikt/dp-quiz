package no.nav.dagpenger.quiz.mediator.meldinger.model

import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.quiz.mediator.meldinger.HendelseMediator
import no.nav.helse.rapids_rivers.JsonMessage

// Forstår en JSON message som representerer
internal class ØnskerRettighetsavklaringMelding(packet: JsonMessage) : HendelseMelding(packet) {
    override val fnr = packet["fnr"].asText()
    val faktagrupperType = Versjon.FaktagrupperType.valueOf(packet["faktagrupperType"].asText())

    override fun behandle(mediator: HendelseMediator) {
        mediator.behandle(fnr, faktagrupperType)
    }
}

package no.nav.dagpenger.quiz.mediator.meldinger

import no.nav.dagpenger.quiz.mediator.behovløsere.DokumentkravSvarService
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

class VilkårsvurderingLøser(rapidsConnection: RapidsConnection) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "vilkårsvurdering") }
            validate { it.demandAll("@vilkår", listOf("øvreAldersgrense")) }
            validate { it.forbid("@løsning") }
        }.register(this)
    }
    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        TODO("Not yet implemented")
    }
}

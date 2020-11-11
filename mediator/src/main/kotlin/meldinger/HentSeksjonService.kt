package meldinger

import com.fasterxml.jackson.databind.JsonNode
import db.SøknadRecord
import no.nav.dagpenger.model.marshalling.SaksbehandlerJsonBuilder
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.util.UUID

internal class HentSeksjonService(rapidsConnection: RapidsConnection) :
    River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { packet ->
                packet.demandValue("@event_name", "hent_seksjon")
                packet.require("@id") { UUID.fromString(it.asText()) }
                packet.require("soknad_uuid") { UUID.fromString(it.asText()) }
                packet.require("seksjon_navn", JsonNode::asText)
                packet.require("indeks", JsonNode::asInt)
                packet.forbid("fakta")
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        val søknadUuid = UUID.fromString(packet["soknad_uuid"].asText())
        val seksjonNavn = packet["seksjon_navn"].asText()
        val indeks = packet["indeks"].asInt()
        val fakta = SøknadRecord().hent(søknadUuid)

        val json = SaksbehandlerJsonBuilder(fakta, seksjonNavn, indeks).resultat()
        json.put("@event_name", packet["@event_name"].asText())
        json.put("@id", packet["@id"].asText())
        context.send(json.toString())
    }
}

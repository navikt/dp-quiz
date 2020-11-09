package meldinger

import com.fasterxml.jackson.databind.JsonNode
import db.SøknadPersistence
import no.nav.dagpenger.model.marshalling.SeksjonJsonBuilder
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.util.UUID

internal class HentSeksjonRiver(rapidsConnection: RapidsConnection, private val db: SøknadPersistence) : River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate { packet ->
                packet.demandValue("@event_name", "hent_seksjon")
                packet.require("soknad_uuid") { UUID.fromString(it.asText()) }
                packet.require("seksjon_navn", JsonNode::asText)
            }
        }
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        val søknadUuid = UUID.fromString(packet["soknad_uuid"].asText())
        val seksjonNavn = packet["seksjon_navn"].asText()

        val seksjon = db.hent(søknadUuid).let { faktagrupper ->
            faktagrupper.firstOrNull { it.navn == seksjonNavn }
        } ?: throw IllegalArgumentException("Søknad UUID $søknadUuid finnes ikke")

        SeksjonJsonBuilder(seksjon).resultat()
    }
}

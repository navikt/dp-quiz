package no.nav.dagpenger.quiz.mediator.meldinger

import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.SøknadRecord
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

class VilkårsvurderingLøser(private val prosessPersistence: SøknadRecord, rapidsConnection: RapidsConnection) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "vilkårsvurdering") }
            validate { it.demandAll("@vilkår", listOf("Paragraf_4_23_alder")) }
            validate { it.requireKey("prosessnavn", "ident") }
            validate { it.forbid("@løsning") }
        }.register(this)
    }
    override fun onPacket(packet: JsonMessage, context: MessageContext) {

        val vilkår = packet["@vilkår"].map { it.asText() }.first()
        val prosessversjon = when (vilkår) {
            "Paragraf_4_23_alder" -> Versjon.siste(Prosess.Paragraf_4_23_alder)
            else -> throw Error("Mangler prosess for $vilkår")
        }

        val identer = Identer.Builder()
            .folkeregisterIdent(packet["ident"].asText())
            .build()

        val Paragraf_4_23_alder_prosess = prosessPersistence.ny(identer, Versjon.UserInterfaceType.Web, prosessversjon)
        prosessPersistence.lagre(Paragraf_4_23_alder_prosess.søknad)

        packet["@løsning"] = mapOf("Paragraf_4_23_alder" to Paragraf_4_23_alder_prosess.søknad.uuid.toString())
        Paragraf_4_23_alder_prosess.sendNesteSeksjon(context)

        context.publish(packet.toJson())
    }
}

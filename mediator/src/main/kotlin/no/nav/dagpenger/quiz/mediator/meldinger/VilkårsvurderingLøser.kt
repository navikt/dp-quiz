package no.nav.dagpenger.quiz.mediator.meldinger

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

class VilkårsvurderingLøser(private val prosessPersistence: SøknadPersistence, rapidsConnection: RapidsConnection) :
    River.PacketListener {
    val behov = "Paragraf_4_23_alder"

    companion object {
        val logger = KotlinLogging.logger { }
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "vilkårsvurdering") }
            validate { it.demandAll("@behov", listOf(behov)) }
            validate { it.requireKey("ident", "behandlingId") }
            validate { it.forbid("@løsning") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {

        withLoggingContext(
            "behandlingId" to packet["behandlingId"].asText(),
        ) {
            val vilkår = packet["@behov"].map { it.asText() }.first()
            logger.info { "Mottok behov om vurdering av $vilkår" }
            val prosessversjon = when (vilkår) {
                behov -> Versjon.siste(Prosess.Paragraf_4_23_alder)
                else -> {
                    logger.error { "Det er ikke støtte for vurdering av $vilkår enda." }
                    null
                }
            } ?: return

            val identer = Identer.Builder()
                .folkeregisterIdent(packet["ident"].asText())
                .build()

            val Paragraf_4_23_alder_prosess =
                prosessPersistence.ny(identer, Versjon.UserInterfaceType.Web, prosessversjon)
            prosessPersistence.lagre(Paragraf_4_23_alder_prosess.søknad)

            val prosessUuid = Paragraf_4_23_alder_prosess.søknad.uuid.toString()
            packet["@løsning"] = mapOf(behov to prosessUuid)
            Paragraf_4_23_alder_prosess.sendNesteSeksjon(context)

            context.publish(packet.toJson())
            logger.info { "Løste $vilkår med prosessId $prosessUuid" }
        }
    }
}

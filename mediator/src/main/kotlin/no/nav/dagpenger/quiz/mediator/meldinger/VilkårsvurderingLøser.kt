package no.nav.dagpenger.quiz.mediator.meldinger

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.aldersvurdering.Paragraf_4_23_alder_vilkår
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.time.LocalDateTime
import java.util.UUID

internal class VilkårsvurderingLøser(
    rapidsConnection: RapidsConnection,
    private val prosessPersistence: SøknadPersistence
) :
    River.PacketListener {
    val behov = "Paragraf_4_23_alder"

    companion object {
        val logger = KotlinLogging.logger { }
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "behov") }
            validate { it.demandAll("@behov", listOf(behov)) }
            validate { it.requireKey("ident", "behandlingId", "vilkårsvurderingId", "søknad_uuid") }
            validate { it.forbid("@løsning") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {

        val vilkårsvurderingId = packet["vilkårsvurderingId"].asText().let { UUID.fromString(it) }
        val søknadUuid = packet["søknad_uuid"].asText().let { UUID.fromString(it) }
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

            val identer = Identer.Builder().folkeregisterIdent(packet["ident"].asText()).build()

            val paragraf_4_23_alder_prosess =
                prosessPersistence.ny(
                    identer = identer,
                    type = Versjon.UserInterfaceType.Web,
                    prosessVersjon = prosessversjon,
                    uuid = vilkårsvurderingId
                )

            paragraf_4_23_alder_prosess.dokument(Paragraf_4_23_alder_vilkår.innsendtSøknadId)
                .besvar(Dokument(LocalDateTime.now(), "urn:soknadid:$søknadUuid"))

            prosessPersistence.lagre(paragraf_4_23_alder_prosess.søknad)

            val prosessUuid = paragraf_4_23_alder_prosess.søknad.uuid.toString()
            packet["@løsning"] = mapOf(behov to prosessUuid)
            paragraf_4_23_alder_prosess.sendNesteSeksjon(context)

            context.publish(packet.toJson())
            logger.info { "Løste $vilkår med prosessId $prosessUuid" }
        }
    }
}

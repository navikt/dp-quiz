package no.nav.dagpenger.quiz.mediator.meldinger

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.FaktaPersistence
import no.nav.dagpenger.quiz.mediator.soknad.Prosessfakta
import no.nav.dagpenger.quiz.mediator.soknad.aldersvurdering.Paragraf_4_23_alder_oppsett
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.time.LocalDateTime
import java.util.UUID

internal class VilkårsvurderingLøser(
    rapidsConnection: RapidsConnection,
    private val prosessPersistence: FaktaPersistence,
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
            try {
                val vilkår = packet["@behov"].map { it.asText() }.first()
                logger.info { "Mottok behov om vurdering av $vilkår" }
                val prosessversjon = when (vilkår) {
                    behov -> Versjon.siste(Prosessfakta.Paragraf_4_23_alder)
                    else -> {
                        logger.error { "Det er ikke støtte for vurdering av $vilkår enda." }
                        null
                    }
                } ?: return

                val identer = Identer.Builder().folkeregisterIdent(packet["ident"].asText()).build()

                val paragraf_4_23_alder_prosess =
                    prosessPersistence.ny(
                        identer = identer,
                        prosessVersjon = prosessversjon,
                        uuid = vilkårsvurderingId,
                    )

                paragraf_4_23_alder_prosess.dokument(Paragraf_4_23_alder_oppsett.innsendtSøknadId)
                    .besvar(Dokument(LocalDateTime.now(), "urn:soknadid:$søknadUuid"))

                prosessPersistence.lagre(paragraf_4_23_alder_prosess.fakta)

                val prosessUuid = paragraf_4_23_alder_prosess.fakta.uuid.toString()
                packet["@løsning"] = mapOf(behov to prosessUuid)
                paragraf_4_23_alder_prosess.sendNesteSeksjon(context)

                context.publish(packet.toJson())
                logger.info { "Løste $vilkår med prosessId $prosessUuid" }
            } catch (e: Exception) {
                logger.error(e) { "Klarte ikke å håndtere $behov" }
            }
        }
    }
}

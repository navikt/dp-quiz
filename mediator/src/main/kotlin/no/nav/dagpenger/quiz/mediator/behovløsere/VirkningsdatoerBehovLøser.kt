package no.nav.dagpenger.quiz.mediator.behovløsere

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.quiz.mediator.db.ProsessRepository
import no.nav.dagpenger.quiz.mediator.soknad.Prosesser
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.DinSituasjon.`dagpenger søknadsdato`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.DinSituasjon.`gjenopptak søknadsdato`
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.DinSituasjon.`mottatt dagpenger siste 12 mnd`
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.util.UUID

internal class VirkningsdatoerBehovLøser(
    rapidsConnection: RapidsConnection,
    private val utredningsRepository: ProsessRepository,
) : River.PacketListener {
    private companion object {
        val logger = KotlinLogging.logger { }
        const val behov = "Virkningsdatoer"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "behov") }
            validate { it.demandAll("@behov", listOf(behov)) }
            validate { it.requireKey("Virkningsdatoer.søknad_uuid") }
            validate { it.forbid("@løsning") }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val søknadId = packet["Virkningsdatoer.søknad_uuid"].asText().let { UUID.fromString(it) }

        withLoggingContext("søknadId" to søknadId.toString()) {
            val prosess: Prosess = utredningsRepository.hent(søknadId)
            if (prosess.type != Prosesser.Søknad) {
                logger.error { "Prøvde å løse behov $behov for prosess som ikke var av type Søknad" }
                return
            }
            val løsning = mapOf(
                "ønsketdato" to ønsketdato(prosess),
            )
            packet["@løsning"] = mapOf(
                behov to løsning,
            )

            context.publish(packet.toJson())
            logger.info { "Løser $behov med $løsning" }
        }
    }

    private fun ønsketdato(prosess: Prosess) =
        when (prosess.fakta.envalg(`mottatt dagpenger siste 12 mnd`).svar()) {
            Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.ja") -> prosess.fakta.dato(`gjenopptak søknadsdato`).svar()
            Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.nei"),
            Envalg("faktum.mottatt-dagpenger-siste-12-mnd.svar.vet-ikke"),
            -> prosess.fakta.dato(`dagpenger søknadsdato`).svar()

            else -> throw IllegalArgumentException("Fant ikke riktig svar-alternativ")
        }
}

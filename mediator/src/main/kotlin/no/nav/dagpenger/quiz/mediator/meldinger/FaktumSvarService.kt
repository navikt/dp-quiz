package no.nav.dagpenger.quiz.mediator.meldinger

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.marshalling.ResultatJsonBuilder
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.util.UUID

private val log = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

internal class FaktumSvarService(
    private val søknadPersistence: SøknadPersistence,
    rapidsConnection: RapidsConnection
) : River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate { message ->
                message.demandValue("@event_name", "faktum_svar")
                message.requireKey(
                    "søknad_uuid",
                    "fakta"
                )
                message.require("@opprettet", JsonNode::asLocalDateTime)
                message.require("@id") { UUID.fromString(it.asText()) }
                message.requireArray("fakta") {
                    requireKey("id")
                    requireKey("clazz")
                }
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        val fakta = packet["fakta"].filter { faktumNode -> faktumNode.has("svar") }
        if (fakta.isEmpty()) return
        val søknadUuid = UUID.fromString(packet["søknad_uuid"].asText())

        withLoggingContext(
            "id" to UUID.fromString(packet["@id"].asText()).toString(),
            "søknadUuid" to søknadUuid.toString()
        ) {
            log.info { "Mottok ny(e) fakta (${fakta.joinToString(",") { it["id"].asText() }}) for $søknadUuid" }
            sikkerlogg.info { packet.toJson() }

            try {
                val søknadprosess = søknadPersistence.hent(søknadUuid, Versjon.UserInterfaceType.Web)
                besvarFakta(fakta, søknadprosess)
                sendNesteSeksjon(søknadprosess, context)
            } catch (e: Exception) {
                log.error(e) { "feil ved svar for faktum: ${e.message}" }
            }
        }
    }

    private fun besvarFakta(fakta: List<JsonNode>, søknadprosess: Søknadprosess) {
        fakta.forEach { faktumNode ->
            val faktumId = faktumNode["id"].asText()
            val svar = faktumNode["svar"]
            val clazz = faktumNode["clazz"].asText()
            val besvartAv = faktumNode["besvartAv"]?.asText()

            besvar(søknadprosess, faktumId, svar, clazz, besvartAv)
        }
        søknadPersistence.lagre(søknadprosess.søknad)
    }

    private fun sendNesteSeksjon(søknadprosess: Søknadprosess, context: RapidsConnection.MessageContext) {
        søknadprosess.nesteSeksjoner()
            .onEach { seksjon ->
                val json = seksjon.somSpørsmål()
                context.send(json)
                sikkerlogg.info { "Send ut seksjon: $json" }
                log.info { "Send seksjon ${seksjon.navn} for søknad ${søknadprosess.søknad.uuid}" }
            }.also { seksjon ->
                if (Søknadprosess.erFerdig(seksjon)) {
                    ResultatJsonBuilder(søknadprosess).resultat().also { json ->
                        context.send(json.toString())
                        sikkerlogg.info { "Send ut resultat: $json" }
                    }
                    log.info { "Ferdig med søknad ${søknadprosess.søknad.uuid}. Resultatet er: ${søknadprosess.resultat()}" }
                }
            }
    }

    override fun onError(problems: MessageProblems, context: RapidsConnection.MessageContext) {
        log.error { problems.toString() }
        sikkerlogg.error { problems.toExtendedReport() }
    }

    private fun besvar(søknadprosess: Søknadprosess, faktumId: String, svar: JsonNode, clazz: String, besvartAv: String?) {
        when (clazz) {
            "boolean" -> søknadprosess.boolsk(faktumId).besvar(svar.asBoolean(), besvartAv)
            "int" -> søknadprosess.heltall(faktumId).besvar(svar.asInt(), besvartAv)
            "localdate" -> søknadprosess.dato(faktumId).besvar(svar.asLocalDate(), besvartAv)
            "inntekt" -> søknadprosess.inntekt(faktumId).besvar(svar.asDouble().årlig, besvartAv)
            "dokument" ->
                søknadprosess.dokument(faktumId)
                    .besvar(Dokument(svar["lastOppTidsstempel"].asLocalDateTime(), svar["url"].asText()), besvartAv)
            "generator" -> {
                val svarene = svar as ArrayNode
                søknadprosess.generator(faktumId).besvar(svarene.size(), besvartAv)
                svarene.forEachIndexed { index, genererteSvar ->
                    genererteSvar.forEach {
                        besvar(
                            søknadprosess,
                            "${it["id"].asText()}.${index + 1}}",
                            it["svar"],
                            it["clazz"].asText(),
                            it["besvartAv"]?.asText()
                        )
                    }
                }
            }
            else -> throw IllegalArgumentException("Ukjent svar-type: $clazz")
        }
    }
}

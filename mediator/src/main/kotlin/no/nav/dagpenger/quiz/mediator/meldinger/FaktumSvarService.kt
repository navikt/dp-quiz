package no.nav.dagpenger.quiz.mediator.meldinger

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import mu.KotlinLogging
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.marshalling.ResultatJsonBuilder
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.util.UUID

private val log = KotlinLogging.logger {}
private val sikkerlogg = KotlinLogging.logger("tjenestekall")

internal class FaktumSvarService(
    private val søknadPersistence: SøknadPersistence,
    rapidsConnection: RapidsConnection
) :
    HendelseService(rapidsConnection) {
    override val eventName = "faktum_svar"
    override val riverName = "Faktum svar"

    override fun validate(packet: JsonMessage) {
        packet.requireKey(
            "søknad_uuid",
            "fakta",
        )
        packet.requireArray("fakta") {
            requireKey("id")
            requireKey("clazz")
        }
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        val fakta = packet["fakta"].filter { faktumNode -> faktumNode.has("svar") }
        if (fakta.isEmpty()) return
        val søknadUuid = UUID.fromString(packet["søknad_uuid"].asText())
        log.info { "Mottok ny svar for $søknadUuid" }
        sikkerlogg.info { packet.toJson() }

        try {
            søknadPersistence.hent(søknadUuid, Versjon.UserInterfaceType.Web).also { søknadprosess ->
                sikkerlogg.info {
                    """Hentet lagrede fakta ${
                    søknadprosess.søknad.filter { it.erBesvart() }.map { "${it.id}: ${it.svar()}" }
                    } """
                }
                fakta.forEach { faktumNode ->
                    val faktumId = faktumNode["id"].asText()
                    val svar = faktumNode["svar"]
                    val clazz = faktumNode["clazz"].asText()

                    besvar(søknadprosess, faktumId, svar, clazz)
                }
                søknadPersistence.lagre(søknadprosess.søknad)
                søknadprosess.nesteSeksjoner()
                    .onEach { seksjon ->
                        val json = seksjon.somSpørsmål()
                        context.send(json)
                        sikkerlogg.info { "Send ut seksjon: $json" }
                        log.info { "Send seksjon ${seksjon.navn} for søknad ${søknadprosess.søknad.uuid}" }
                    }.also {
                        if (Søknadprosess.erFerdig(it)) {
                            ResultatJsonBuilder(søknadprosess).resultat().also { json ->
                                context.send(json.toString())
                                sikkerlogg.info { "Send ut resultat: $json" }
                            }
                            log.info { "Ferdig med søknad ${søknadprosess.søknad.uuid}. Resultatet er: ${søknadprosess.resultat()}" }
                        }
                    }
            }
        } catch (e: Exception) {
            log.error(e) {
                "feil ved faktum svar: ${e.message}"
            }
        }
    }

    private fun besvar(søknadprosess: Søknadprosess, faktumId: String, svar: JsonNode, clazz: String) {
        when (clazz) {
            "boolean" -> søknadprosess.ja(faktumId).besvar(svar.asBoolean())
            "int" -> søknadprosess.heltall(faktumId).besvar(svar.asInt())
            "localdate" -> søknadprosess.dato(faktumId).besvar(svar.asLocalDate())
            "inntekt" -> søknadprosess.inntekt(faktumId).besvar(svar.asDouble().årlig)
            "dokument" ->
                søknadprosess.dokument(faktumId)
                    .besvar(Dokument(svar["lastOppTidsstempel"].asLocalDateTime(), svar["url"].asText()))
            "generator" -> {
                val svarene = svar as ArrayNode
                søknadprosess.generator(faktumId).besvar(svarene.size())
                svarene.forEachIndexed { index, genererteSvar ->
                    genererteSvar.forEach {
                        besvar(søknadprosess, "${it["id"].asText()}.${index + 1}}", it["svar"], it["clazz"].asText())
                    }
                }
            }
            else -> throw IllegalArgumentException("Ukjent svar-type: $clazz")
        }
    }
}

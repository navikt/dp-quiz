package no.nav.dagpenger.quiz.mediator.meldinger

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import mu.KotlinLogging
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktatype
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.marshalling.ResultatJsonBuilder
import no.nav.dagpenger.model.marshalling.SøkerJsonBuilder
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.visitor.UtredningsprosessVisitor
import no.nav.dagpenger.quiz.mediator.db.FaktaRepository
import no.nav.dagpenger.quiz.mediator.db.ResultatPersistence
import no.nav.dagpenger.quiz.mediator.soknad.Prosessfakta
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime
import no.nav.helse.rapids_rivers.withMDC
import java.util.UUID

internal class FaktumSvarService(
    private val faktaRepository: FaktaRepository,
    private val resultatPersistence: ResultatPersistence,
    rapidsConnection: RapidsConnection,
) : River.PacketListener {
    private companion object {
        private val log = KotlinLogging.logger {}
        private val sikkerlogg = KotlinLogging.logger("tjenestekall")
        private val ignorerSøknadUUID =
            setOf("e08c32fd-3941-4d10-b81d-425f67c23598").map {
                UUID.fromString(it)
            }
    }

    init {
        River(rapidsConnection).apply {
            validate { message ->
                message.demandValue("@event_name", "faktum_svar")
                message.requireKey(
                    "søknad_uuid",
                    "fakta",
                )
                message.require("@opprettet", JsonNode::asLocalDateTime)
                message.require("@id") { UUID.fromString(it.asText()) }
                message.requireArray("fakta") {
                    requireKey("id")
                    requireKey("type")
                }
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val søknadUuid = UUID.fromString(packet["søknad_uuid"].asText())
        if ((ignorerSøknadUUID.contains(søknadUuid)) &&
            System.getenv()["NAIS_CLUSTER_NAME"] == "dev-gcp"
        ) {
            log.info { "Skipper $søknadUuid, poison pill." }
            return
        }
        val fakta = packet["fakta"].filter(harSvar())
        if (fakta.isEmpty()) return

        try {
            withMDC(
                mapOf(
                    "behovId" to UUID.fromString(packet["@id"].asText()).toString(),
                    "søknad_uuid" to søknadUuid.toString(),
                ),
            ) {
                log.info { "Mottok ny(e) fakta (${fakta.joinToString(",") { it["id"].asText() }}) for $søknadUuid" }
                sikkerlogg.info { "Mottok ny(e) fakta: ${packet.toJson()}" }

                val søknadprosess = faktaRepository.hent(søknadUuid)
                besvarFakta(fakta, søknadprosess)

                val prosessnavn = ProsessVersjonVisitor(søknadprosess).faktatype
                if (søknadprosess.erFerdig()) {
                    // TODO: Lag en bedre måte å håndtere disse prosessene
                    if (prosessnavn == Prosessfakta.Dagpenger || prosessnavn == Prosessfakta.Innsending) {
                        SøkerJsonBuilder(søknadprosess).resultat().also { json ->
                            val message = json.toString().let { JsonMessage(it, MessageProblems(it)) }
                            context.publish(message.toJson())
                        }
                        log.info { "Ferdig med søknad ${søknadprosess.fakta.uuid}. Resultatet er: ${søknadprosess.resultat()}" }
                    } else {
                        sendResultat(søknadprosess, context)
                    }
                } else {
                    søknadprosess.sendNesteSeksjon(context)
                }
            }
        } catch (e: Exception) {
            log.error(e) { "Kunne ikke lagre faktum for søknad $søknadUuid" }
            // throw e
        }
    }

    private fun besvarFakta(fakta: List<JsonNode>, utredningsprosess: Utredningsprosess) {
        fakta.forEach { faktumNode ->
            val faktumId = faktumNode["id"].asText()
            val svar = faktumNode["svar"]
            val type = faktumNode["type"].asText()
            val besvartAv = faktumNode["besvartAv"]?.asText()

            besvar(utredningsprosess, faktumId, svar, type, besvartAv)
        }
        faktaRepository.lagre(utredningsprosess.fakta)
    }

    private fun sendResultat(utredningsprosess: Utredningsprosess, context: MessageContext) {
        ResultatJsonBuilder(utredningsprosess).resultat().also { json ->
            resultatPersistence.lagreResultat(utredningsprosess.resultat()!!, utredningsprosess.fakta.uuid, json)
            context.publish(json.toString())
            sikkerlogg.info { "Send ut resultat: $json" }
        }
        log.info { "Ferdig med søknad ${utredningsprosess.fakta.uuid}. Resultatet er: ${utredningsprosess.resultat()}" }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.error { problems.toString() }
        sikkerlogg.error { problems.toExtendedReport() }
    }

    private fun besvar(
        utredningsprosess: Utredningsprosess,
        faktumId: String,
        svar: JsonNode,
        type: String,
        besvartAv: String?,
    ) {
        if (svar.isNull) return utredningsprosess.id(faktumId).tilUbesvart()
        when (type) {
            "land" -> utredningsprosess.land(faktumId).besvar(svar.asLand(), besvartAv)
            "boolean" -> utredningsprosess.boolsk(faktumId).besvar(svar.asBoolean(), besvartAv)
            "int" -> utredningsprosess.heltall(faktumId).besvar(svar.asInt(), besvartAv) // todo: remove?
            "integer" -> utredningsprosess.heltall(faktumId).besvar(svar.asInt(), besvartAv)
            "double" -> utredningsprosess.desimaltall(faktumId).besvar(svar.asDouble(), besvartAv)
            "localdate" -> utredningsprosess.dato(faktumId).besvar(svar.asLocalDate(), besvartAv)
            "inntekt" -> utredningsprosess.inntekt(faktumId).besvar(svar.asDouble().årlig, besvartAv)
            "envalg" -> utredningsprosess.envalg(faktumId).besvar(svar.asEnvalg(), besvartAv)
            "flervalg" -> utredningsprosess.flervalg(faktumId).besvar(svar.asFlervalg(), besvartAv)
            "tekst" -> utredningsprosess.tekst(faktumId).besvar(svar.asTekst(), besvartAv)
            "periode" -> utredningsprosess.periode(faktumId).besvar(svar.asPeriode(), besvartAv)
            "dokument" -> utredningsprosess.dokument(faktumId).besvar(svar.asDokument(), besvartAv)
            "generator" -> {
                val svarene = svar as ArrayNode
                utredningsprosess.generator(faktumId).besvar(svarene.size(), besvartAv)
                svarene.forEachIndexed { index, genererteSvar ->
                    genererteSvar.filter(harSvar()).forEach {
                        besvar(
                            utredningsprosess,
                            "${it["id"].asText()}.${index + 1}}",
                            it["svar"],
                            it["type"].asText(),
                            it["besvartAv"]?.asText(),
                        )
                    }
                }
            }
            else -> throw IllegalArgumentException("Ukjent svar-type: $type")
        }
    }

    private fun harSvar() = { faktumNode: JsonNode -> faktumNode.has("svar") }

    private class ProsessVersjonVisitor(utredningsprosess: Utredningsprosess) : UtredningsprosessVisitor {
        lateinit var faktatype: Faktatype

        init {
            utredningsprosess.accept(this)
        }

        override fun preVisit(fakta: Fakta, faktaversjon: Faktaversjon, uuid: UUID) {
            faktatype = faktaversjon.faktatype
        }
    }
}

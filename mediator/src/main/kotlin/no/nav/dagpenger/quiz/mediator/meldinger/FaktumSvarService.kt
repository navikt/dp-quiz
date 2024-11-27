package no.nav.dagpenger.quiz.mediator.meldinger

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDateTime
import com.github.navikt.tbd_libs.rapids_and_rivers.withMDC
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageMetadata
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.MeterRegistry
import jsonNodeToMap
import mu.KotlinLogging
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktatype
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.marshalling.FaktumNavBehov
import no.nav.dagpenger.model.marshalling.ResultatJsonBuilder
import no.nav.dagpenger.model.marshalling.SøkerJsonBuilder
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.visitor.ProsessVisitor
import no.nav.dagpenger.quiz.mediator.db.ProsessRepository
import no.nav.dagpenger.quiz.mediator.db.ResultatPersistence
import no.nav.dagpenger.quiz.mediator.soknad.Prosessfakta
import java.util.UUID

internal class FaktumSvarService(
    private val prosessRepository: ProsessRepository,
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
            precondition { it.requireValue("@event_name", "faktum_svar") }
            validate {
                it.requireKey(
                    "søknad_uuid",
                    "fakta",
                )
                it.require("@opprettet", JsonNode::asLocalDateTime)
                it.require("@id") { UUID.fromString(it.asText()) }
                it.requireArray("fakta") {
                    requireKey("id")
                    requireKey("type")
                }
            }
        }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
        metadata: MessageMetadata,
        meterRegistry: MeterRegistry,
    ) {
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

                val søknadprosess = prosessRepository.hent(søknadUuid)
                besvarFakta(fakta, søknadprosess)

                val prosessnavn = ProsessVersjonVisitor(søknadprosess).faktatype
                if (søknadprosess.erFerdig()) {
                    // TODO: Lag en bedre måte å håndtere disse prosessene
                    if (prosessnavn == Prosessfakta.Dagpenger || prosessnavn == Prosessfakta.Innsending) {
                        SøkerJsonBuilder(søknadprosess).resultat().also { json: ObjectNode ->
                            val message = JsonMessage.newMessage(jsonNodeToMap(json))
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

    private fun besvarFakta(
        fakta: List<JsonNode>,
        prosess: Prosess,
    ) {
        fakta.forEach { faktumNode ->
            val faktumId = faktumNode["id"].asText()
            val svar = faktumNode["svar"]
            val type = faktumNode["type"].asText()
            val besvartAv = faktumNode["besvartAv"]?.asText()

            besvar(prosess, faktumId, svar, type, besvartAv)
        }
        prosessRepository.lagre(prosess)
    }

    private fun sendResultat(
        prosess: Prosess,
        context: MessageContext,
    ) {
        ResultatJsonBuilder(prosess).resultat().also { json ->
            resultatPersistence.lagreResultat(prosess.resultat()!!, prosess.fakta.uuid, json)
            context.publish(json.toString())
            sikkerlogg.info { "Send ut resultat: $json" }
        }
        log.info { "Ferdig med søknad ${prosess.fakta.uuid}. Resultatet er: ${prosess.resultat()}" }
    }

    override fun onError(
        problems: MessageProblems,
        context: MessageContext,
        metadata: MessageMetadata,
    ) {
        log.error { problems.toString() }
        sikkerlogg.error { problems.toExtendedReport() }
    }

    private fun besvar(
        prosess: Prosess,
        faktumId: String,
        svar: JsonNode,
        type: String,
        besvartAv: String?,
    ) {
        if (svar.isNull) return prosess.id(faktumId).tilUbesvart()
        when (type) {
            "land" -> prosess.land(faktumId).besvar(svar.asLand(), besvartAv)
            "boolean" -> prosess.boolsk(faktumId).besvar(svar.asBoolean(), besvartAv)
            "int" -> prosess.heltall(faktumId).besvar(svar.asInt(), besvartAv) // todo: remove?
            "integer" -> prosess.heltall(faktumId).besvar(svar.asInt(), besvartAv)
            "double" -> prosess.desimaltall(faktumId).besvar(svar.asDouble(), besvartAv)
            "localdate" -> prosess.dato(faktumId).besvar(svar.asLocalDate(), besvartAv)
            "inntekt" -> prosess.inntekt(faktumId).besvar(svar.asDouble().årlig, besvartAv)
            "envalg" -> prosess.envalg(faktumId).besvar(svar.asEnvalg(), besvartAv)
            "flervalg" -> prosess.flervalg(faktumId).besvar(svar.asFlervalg(), besvartAv)
            "tekst" -> prosess.tekst(faktumId).besvar(svar.asTekst(), besvartAv)
            "periode" -> prosess.periode(faktumId).besvar(svar.asPeriode(), besvartAv)
            "dokument" -> prosess.dokument(faktumId).besvar(svar.asDokument(), besvartAv)
            "generator" -> {
                val svarene = svar as ArrayNode
                prosess.generator(faktumId).besvar(svarene.size(), besvartAv)
                svarene.forEachIndexed { index, genererteSvar ->
                    genererteSvar.filter(harSvar()).forEach {
                        besvar(
                            prosess,
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

    private class ProsessVersjonVisitor(prosess: Prosess) : ProsessVisitor {
        lateinit var faktatype: Faktatype

        init {
            prosess.accept(this)
        }

        override fun preVisit(
            fakta: Fakta,
            faktaversjon: Faktaversjon,
            uuid: UUID,
            navBehov: FaktumNavBehov,
        ) {
            faktatype = faktaversjon.faktatype
        }
    }
}

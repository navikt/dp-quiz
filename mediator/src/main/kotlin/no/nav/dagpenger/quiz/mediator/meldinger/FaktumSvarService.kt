package no.nav.dagpenger.quiz.mediator.meldinger

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import mu.KotlinLogging
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.Prosessnavn
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.ResultatJsonBuilder
import no.nav.dagpenger.model.marshalling.SøkerJsonBuilder
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import no.nav.dagpenger.quiz.mediator.db.ResultatPersistence
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
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
    private val søknadPersistence: SøknadPersistence,
    private val resultatPersistence: ResultatPersistence,
    rapidsConnection: RapidsConnection
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
                    "fakta"
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
                    "søknad_uuid" to søknadUuid.toString()
                )
            ) {
                log.info { "Mottok ny(e) fakta (${fakta.joinToString(",") { it["id"].asText() }}) for $søknadUuid" }
                sikkerlogg.info { "Mottok ny(e) fakta: ${packet.toJson()}" }

                val søknadprosess = søknadPersistence.hent(søknadUuid, Versjon.UserInterfaceType.Web)
                besvarFakta(fakta, søknadprosess)

                val prosessnavn = ProsessVersjonVisitor(søknadprosess).prosessnavn
                if (søknadprosess.erFerdig()) {
                    if (prosessnavn == Prosess.Dagpenger) {
                        SøkerJsonBuilder(søknadprosess).resultat().also { json ->
                            val message = json.toString().let { JsonMessage(it, MessageProblems(it)) }
                            context.publish(message.toJson())
                        }
                        log.info { "Ferdig med søknad ${søknadprosess.søknad.uuid}. Resultatet er: ${søknadprosess.resultat()}" }
                    } else {
                        sendResultat(søknadprosess, context)
                    }
                } else {
                    søknadprosess.sendNesteSeksjon(context)
                }
            }
        } catch (e: Exception) {
            log.error(e) { "Kunne ikke lagre faktum for søknad $søknadUuid" }
            throw e
        }
    }

    private fun besvarFakta(fakta: List<JsonNode>, søknadprosess: Søknadprosess) {
        fakta.forEach { faktumNode ->
            val faktumId = faktumNode["id"].asText()
            val svar = faktumNode["svar"]
            val type = faktumNode["type"].asText()
            val besvartAv = faktumNode["besvartAv"]?.asText()

            besvar(søknadprosess, faktumId, svar, type, besvartAv)
        }
        søknadPersistence.lagre(søknadprosess.søknad)
    }

    private fun sendResultat(søknadprosess: Søknadprosess, context: MessageContext) {
        ResultatJsonBuilder(søknadprosess).resultat().also { json ->
            resultatPersistence.lagreResultat(søknadprosess.resultat()!!, søknadprosess.søknad.uuid, json)
            context.publish(json.toString())
            sikkerlogg.info { "Send ut resultat: $json" }
        }
        log.info { "Ferdig med søknad ${søknadprosess.søknad.uuid}. Resultatet er: ${søknadprosess.resultat()}" }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        log.error { problems.toString() }
        sikkerlogg.error { problems.toExtendedReport() }
    }

    private fun besvar(
        søknadprosess: Søknadprosess,
        faktumId: String,
        svar: JsonNode,
        type: String,
        besvartAv: String?
    ) {
        when (type) {
            "land" -> søknadprosess.land(faktumId).besvar(svar.asLand(), besvartAv)
            "boolean" -> søknadprosess.boolsk(faktumId).besvar(svar.asBoolean(), besvartAv)
            "int" -> søknadprosess.heltall(faktumId).besvar(svar.asInt(), besvartAv) // todo: remove?
            "integer" -> søknadprosess.heltall(faktumId).besvar(svar.asInt(), besvartAv)
            "double" -> søknadprosess.desimaltall(faktumId).besvar(svar.asDouble(), besvartAv)
            "localdate" -> søknadprosess.dato(faktumId).besvar(svar.asLocalDate(), besvartAv)
            "inntekt" -> søknadprosess.inntekt(faktumId).besvar(svar.asDouble().årlig, besvartAv)
            "envalg" -> søknadprosess.envalg(faktumId).besvar(svar.asEnvalg(), besvartAv)
            "flervalg" -> søknadprosess.flervalg(faktumId).besvar(svar.asFlervalg(), besvartAv)
            "tekst" -> søknadprosess.tekst(faktumId).besvar(svar.asTekst(), besvartAv)
            "periode" -> søknadprosess.periode(faktumId).besvar(svar.asPeriode(), besvartAv)
            "dokument" -> søknadprosess.dokument(faktumId).besvar(svar.asDokument(), besvartAv)
            "generator" -> {
                val svarene = svar as ArrayNode
                søknadprosess.generator(faktumId).besvar(svarene.size(), besvartAv)
                svarene.forEachIndexed { index, genererteSvar ->
                    genererteSvar.filter(harSvar()).forEach {
                        besvar(
                            søknadprosess,
                            "${it["id"].asText()}.${index + 1}}",
                            it["svar"],
                            it["type"].asText(),
                            it["besvartAv"]?.asText()
                        )
                    }
                }
            }
            else -> throw IllegalArgumentException("Ukjent svar-type: $type")
        }
    }

    private fun harSvar() = { faktumNode: JsonNode -> faktumNode.has("svar") }

    private class ProsessVersjonVisitor(søknadprosess: Søknadprosess) : SøknadprosessVisitor {

        lateinit var prosessnavn: Prosessnavn

        init {
            søknadprosess.accept(this)
        }

        override fun preVisit(søknad: Søknad, prosessVersjon: Prosessversjon, uuid: UUID) {
            prosessnavn = prosessVersjon.prosessnavn
        }
    }
}

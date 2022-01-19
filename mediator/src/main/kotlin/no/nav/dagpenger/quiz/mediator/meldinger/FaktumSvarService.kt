package no.nav.dagpenger.quiz.mediator.meldinger

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import mu.KotlinLogging
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.Prosessnavn
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.FaktaJsonBuilder
import no.nav.dagpenger.model.marshalling.ResultatJsonBuilder
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import no.nav.dagpenger.quiz.mediator.db.ResultatPersistence
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.dagpenger.quiz.mediator.soknad.Dagpenger.søknadsprosess
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
                    requireKey("clazz")
                }
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val søknadUuid = UUID.fromString(packet["søknad_uuid"].asText())
        val fakta = packet["fakta"].filter { faktumNode -> faktumNode.has("svar") }
        if (fakta.isEmpty()) return

        try {
            withMDC(
                mapOf(
                    "behovId" to UUID.fromString(packet["@id"].asText()).toString(),
                    "soknadUuid" to søknadUuid.toString()
                )
            ) {
                log.info { "Mottok ny(e) fakta (${fakta.joinToString(",") { it["id"].asText() }}) for $søknadUuid" }
                sikkerlogg.info { packet.toJson() }

                val søknadprosess = søknadPersistence.hent(søknadUuid, Versjon.UserInterfaceType.Web)
                besvarFakta(fakta, søknadprosess)

                when (ProsessVersjonVisitor(søknadprosess).prosessnavn) {
                    Prosess.Dagpenger -> {
                        context.publish(
                            FaktaJsonBuilder(søknadsprosess).resultat().toString().also {
                                sikkerlogg.info { "Fakta sendt: $it" }
                                log.info { "Fakta sendt: $it" }
                            }
                        )
                    }
                    else -> {
                        if (søknadprosess.erFerdig()) {
                            sendResultat(søknadprosess, context)
                        } else {
                            sendNesteSeksjon(søknadprosess, context)
                        }
                    }
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
            val clazz = faktumNode["clazz"].asText()
            val besvartAv = faktumNode["besvartAv"]?.asText()

            besvar(søknadprosess, faktumId, svar, clazz, besvartAv)
        }
        søknadPersistence.lagre(søknadprosess.søknad)
    }

    private fun sendNesteSeksjon(søknadprosess: Søknadprosess, context: MessageContext) {
        søknadprosess.nesteSeksjoner()
            .onEach { seksjon ->
                val json = seksjon.somSpørsmål()
                context.publish(json)
                sikkerlogg.info { "Send ut seksjon: $json" }
                log.info { "Send seksjon ${seksjon.navn} for søknad ${søknadprosess.søknad.uuid}" }
            }
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
        clazz: String,
        besvartAv: String?
    ) {
        when (clazz) {
            "boolean" -> søknadprosess.boolsk(faktumId).besvar(svar.asBoolean(), besvartAv)
            "int" -> søknadprosess.heltall(faktumId).besvar(svar.asInt(), besvartAv)
            "double" -> søknadprosess.desimaltall(faktumId).besvar(svar.asDouble(), besvartAv)
            "localdate" -> søknadprosess.dato(faktumId).besvar(svar.asLocalDate(), besvartAv)
            "inntekt" -> søknadprosess.inntekt(faktumId).besvar(svar.asDouble().årlig, besvartAv)
            "envalg" -> søknadprosess.envalg(faktumId).besvar(svar.asEnvalg(), besvartAv)
            "flervalg" -> søknadprosess.flervalg(faktumId).besvar(svar.asFlervalg(), besvartAv)
            "tekst" -> søknadprosess.tekst(faktumId).besvar(svar.asTekst(), besvartAv)
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

    private class ProsessVersjonVisitor(private val søknadprosess: Søknadprosess) : SøknadprosessVisitor {

        lateinit var prosessnavn: Prosessnavn
        init {
            søknadprosess.accept(this)
        }

        override fun preVisit(søknad: Søknad, prosessVersjon: Prosessversjon, uuid: UUID) {
            prosessnavn = prosessVersjon.prosessnavn
        }
    }
}

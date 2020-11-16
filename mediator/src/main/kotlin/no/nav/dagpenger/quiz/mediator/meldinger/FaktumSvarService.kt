package no.nav.dagpenger.quiz.mediator.meldinger

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.util.UUID

internal class FaktumSvarService(
    private val søknadPersistence: SøknadPersistence,
    private val behovMediator: BehovMediator,
    rapidsConnection: RapidsConnection
) :
    HendelseService(rapidsConnection) {

    override val eventName = "faktum_svar"
    override val riverName = "Faktum svar"

    override fun validate(packet: JsonMessage) {
        packet.requireKey(
            "fnr",
            "opprettet",
            "søknadUuid",
            "fakta"
        )
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        val fnr = packet["fnr"].asText()
        val søknadUuid = UUID.fromString(packet["søknadUuid"].asText())
        val fakta = packet["fakta"]

        søknadPersistence.hent(søknadUuid, Versjon.UserInterfaceType.Web).also { søknadprosess ->
            fakta.filter { faktumNode -> faktumNode.has("svar") }
                .forEach { faktumNode ->
                    val faktumId = faktumNode["faktumId"].asInt()
                    val svar = faktumNode["svar"]
                    val clazz = faktumNode["clazz"].asText()

                    besvar(søknadprosess, faktumId, svar, clazz)
                }
            søknadPersistence.lagre(søknadprosess.søknad)
            søknadprosess.nesteSeksjoner()
                .onEach { seksjon ->
                    behovMediator.håndter(seksjon, fnr, søknadprosess.søknad.uuid)
                }
            // .also { if (it.isEmpty()) behandleFerdigResultat() }
        }
    }

    private fun besvar(søknadprosess: Søknadprosess, faktumId: Int, svar: JsonNode, clazz: String) {
        when (clazz) {
            "boolean" -> søknadprosess.ja(faktumId).besvar(svar.asBoolean())
            "heltall" -> søknadprosess.heltall(faktumId).besvar(svar.asInt())
            "dato" -> søknadprosess.dato(faktumId).besvar(svar.asLocalDate())
            "inntekt" -> søknadprosess.inntekt(faktumId).besvar(svar.asDouble().årlig)
            "dokument" ->
                søknadprosess.dokument(faktumId)
                    .besvar(Dokument(svar["lastOppTidsstempel"].asLocalDateTime(), svar["url"].asText()))
            else -> throw IllegalArgumentException("Ukjent svar-type: ${svar::class.java}")
        }
    }
}

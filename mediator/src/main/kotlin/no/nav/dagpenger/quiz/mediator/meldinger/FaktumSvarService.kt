package no.nav.dagpenger.quiz.mediator.meldinger

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
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

        søknadPersistence.hent(søknadUuid, Versjon.FaktagrupperType.Web).also { faktagrupper ->
            fakta.filter { faktumNode -> faktumNode.has("svar") }
                .forEach { faktumNode ->
                    val faktumId = faktumNode["faktumId"].asInt()
                    val svar = faktumNode["svar"]
                    val clazz = faktumNode["clazz"].asText()

                    besvar(faktagrupper, faktumId, svar, clazz)
                }
            søknadPersistence.lagre(faktagrupper.søknad)
            faktagrupper.nesteSeksjoner()
                .onEach { seksjon ->
                    behovMediator.håndter(seksjon, fnr, faktagrupper.søknad.uuid)
                }
            // .also { if (it.isEmpty()) behandleFerdigResultat() }
        }
    }

    private fun besvar(faktagrupper: Faktagrupper, faktumId: Int, svar: JsonNode, clazz: String) {
        when (clazz) {
            "boolean" -> faktagrupper.ja(faktumId).besvar(svar.asBoolean())
            "heltall" -> faktagrupper.heltall(faktumId).besvar(svar.asInt())
            "dato" -> faktagrupper.dato(faktumId).besvar(svar.asLocalDate())
            "inntekt" -> faktagrupper.inntekt(faktumId).besvar(svar.asDouble().årlig)
            "dokument" ->
                faktagrupper.dokument(faktumId)
                    .besvar(Dokument(svar["lastOppTidsstempel"].asLocalDateTime(), svar["url"].asText()))
            else -> throw IllegalArgumentException("Ukjent svar-type: ${svar::class.java}")
        }
    }
}

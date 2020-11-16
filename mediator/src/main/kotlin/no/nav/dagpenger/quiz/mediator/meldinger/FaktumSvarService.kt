package no.nav.dagpenger.quiz.mediator.meldinger

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.time.LocalDate
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
            "faktumId",
            "svar",
            "søknadUuid",
            "clazz",
        )
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        val fnr = packet["fnr"].asText()
        val søknadUuid = UUID.fromString(packet["søknadUuid"].asText())
        val faktumId = packet["faktumId"].asInt()
        val typedSvar = typedSvar(packet["svar"], packet["clazz"].asText())

        søknadPersistence.hent(søknadUuid, Versjon.UserInterfaceType.Web).also { søknadprosess ->
            besvar(søknadprosess, faktumId, typedSvar)
            søknadPersistence.lagre(søknadprosess.søknad)
            søknadprosess.nesteSeksjoner()
                .onEach { seksjon ->
                    behovMediator.håndter(seksjon, fnr, søknadprosess.søknad.uuid)
                }
            // .also { if (it.isEmpty()) behandleFerdigResultat() }
        }
    }

    private fun typedSvar(svar: JsonNode, clazz: String) = when (clazz) {
        "boolean" -> svar.asBoolean()
        "heltall" -> svar.asInt()
        "dato" -> svar.asLocalDate()
        "inntekt" -> svar.asDouble().årlig
        "dokument" -> Dokument(svar["lastOppTidsstempel"].asLocalDateTime(), svar["url"].asText())
        else -> throw IllegalArgumentException("Ukjent faktum type: $clazz")
    }

    private fun besvar(søknadprosess: Søknadprosess, faktumId: Int, svar: Any) {
        when (svar) {
            is Boolean -> søknadprosess.ja(faktumId).besvar(svar)
            is Int -> søknadprosess.heltall(faktumId).besvar(svar)
            is LocalDate -> søknadprosess.dato(faktumId).besvar(svar)
            is Inntekt -> søknadprosess.inntekt(faktumId).besvar(svar)
            is Dokument -> søknadprosess.dokument(faktumId).besvar(svar)
            else -> throw IllegalArgumentException("Ukjent svar-type: ${svar::class.java}")
        }
    }
}

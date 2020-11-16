package no.nav.dagpenger.quiz.mediator.meldinger

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
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

        søknadPersistence.hent(søknadUuid, Versjon.FaktagrupperType.Web).also { faktagrupper ->
            besvar(faktagrupper, faktumId, typedSvar)
            søknadPersistence.lagre(faktagrupper.søknad)
            faktagrupper.nesteSeksjoner()
                .onEach { seksjon ->
                    behovMediator.håndter(seksjon, fnr, faktagrupper.søknad.uuid)
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

    private fun besvar(faktagrupper: Faktagrupper, faktumId: Int, svar: Any) {
        when (svar) {
            is Boolean -> faktagrupper.ja(faktumId).besvar(svar)
            is Int -> faktagrupper.heltall(faktumId).besvar(svar)
            is LocalDate -> faktagrupper.dato(faktumId).besvar(svar)
            is Inntekt -> faktagrupper.inntekt(faktumId).besvar(svar)
            is Dokument -> faktagrupper.dokument(faktumId).besvar(svar)
            else -> throw IllegalArgumentException("Ukjent svar-type: ${svar::class.java}")
        }
    }
}

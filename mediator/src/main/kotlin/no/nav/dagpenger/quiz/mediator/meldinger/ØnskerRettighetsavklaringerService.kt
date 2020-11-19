package no.nav.dagpenger.quiz.mediator.meldinger

import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection

internal class ØnskerRettighetsavklaringerService(
    private val søknadPersistence: SøknadPersistence,
    rapidsConnection: RapidsConnection
) :
    HendelseService(rapidsConnection) {

    override val eventName = "ønsker_rettighetsavklaring"
    override val riverName = "Ønsker rettighetsavklaring"

    override fun validate(packet: JsonMessage) {
        packet.requireKey("fnr", "opprettet", "faktagrupperType")
    }

    override fun onPacket(packet: JsonMessage, context: RapidsConnection.MessageContext) {
        val fnr = packet["fnr"].asText()
        val faktagrupperType = Versjon.UserInterfaceType.valueOf(packet["faktagrupperType"].asText())
        søknadPersistence.ny(fnr, faktagrupperType, Versjon.siste)
            .also { søknadprosess ->
                søknadprosess.nesteSeksjoner()
                    .forEach { seksjon ->
                        context.send(seksjon.somSpørsmål())
                    }
            }
    }
}

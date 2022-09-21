package no.nav.dagpenger.quiz.mediator

import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.marshalling.SøknadsmalJsonBuilder
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.behovløsere.BehandlingsdatoService
import no.nav.dagpenger.quiz.mediator.behovløsere.SenesteMuligeVirkningsdatoService
import no.nav.dagpenger.quiz.mediator.behovløsere.TerskelFaktorService
import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.quiz.mediator.db.ResultatRecord
import no.nav.dagpenger.quiz.mediator.db.SøknadRecord
import no.nav.dagpenger.quiz.mediator.meldinger.AvslagPåMinsteinntektService
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.meldinger.ManuellBehandlingSink
import no.nav.dagpenger.quiz.mediator.meldinger.NyProsessBehovLøser
import no.nav.dagpenger.quiz.mediator.meldinger.SøknadSlettetService
import no.nav.dagpenger.quiz.mediator.soknad.Prosess
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Dagpenger
import no.nav.dagpenger.quiz.mediator.soknad.innsending.Innsending
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

// Understands how to build our application server
internal class ApplicationBuilder : RapidsConnection.StatusListener {
    private val rapidsConnection = RapidApplication.Builder(
        RapidApplication.RapidApplicationConfig.fromEnv(Configuration.config)
    ).build()

    init {
        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()
    fun stop() = rapidsConnection.stop()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        runMigration()
            .also {
                val søknadRecord = SøknadRecord()
                val resultatRecord = ResultatRecord()
                AvslagPåMinsteinntektOppsett.registrer { prototypeSøknad -> FaktumTable(prototypeSøknad) }
                AvslagPåMinsteinntektService(søknadRecord, rapidsConnection)

                Dagpenger.registrer { prototypeSøknad ->
                    FaktumTable(prototypeSøknad)
                    val sisteDagpengerVersjon = Versjon.siste(Prosess.Dagpenger)
                    Versjon.id(sisteDagpengerVersjon).also { versjon ->
                        val søknadsprosess = versjon.søknadprosess(prototypeSøknad, Versjon.UserInterfaceType.Web)
                        val malJson = SøknadsmalJsonBuilder(søknadsprosess).resultat().toString()
                        rapidsConnection.publish(JsonMessage(malJson, MessageProblems(malJson)).toJson())
                    }
                }

                Innsending.registrer { prototype: Søknad ->
                    FaktumTable(prototype)
                }

                NyProsessBehovLøser(søknadRecord, rapidsConnection)
                FaktumSvarService(søknadRecord, resultatRecord, rapidsConnection)
                BehandlingsdatoService(rapidsConnection)
                SenesteMuligeVirkningsdatoService(rapidsConnection)
                TerskelFaktorService(rapidsConnection)
                ManuellBehandlingSink(rapidsConnection, resultatRecord)
                SøknadSlettetService(rapidsConnection, søknadRecord)
            }
    }
}

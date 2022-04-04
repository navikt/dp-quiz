package no.nav.dagpenger.quiz.mediator

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
import no.nav.dagpenger.quiz.mediator.meldinger.NySøknadBehovLøser
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Dagpenger
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
                AvslagPåMinsteinntektOppsett.registrer { søknad -> FaktumTable(søknad) }
                AvslagPåMinsteinntektService(søknadRecord, rapidsConnection)
                Dagpenger.registrer { søknad -> FaktumTable(søknad) }
                NySøknadBehovLøser(søknadRecord, rapidsConnection)
                FaktumSvarService(søknadRecord, resultatRecord, rapidsConnection)
                BehandlingsdatoService(rapidsConnection)
                SenesteMuligeVirkningsdatoService(rapidsConnection)
                TerskelFaktorService(rapidsConnection)
                ManuellBehandlingSink(rapidsConnection, resultatRecord)
            }
    }
}

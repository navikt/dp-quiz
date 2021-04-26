package no.nav.dagpenger.quiz.mediator

import PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.quiz.mediator.behovløsere.BehandlingsdatoService
import no.nav.dagpenger.quiz.mediator.behovløsere.SenesteMuligeVirkningstidspunktService
import no.nav.dagpenger.quiz.mediator.behovløsere.TerskelFaktorService
import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.SøknadRecord
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.meldinger.NySøknadService
import no.nav.dagpenger.quiz.mediator.soknad.AvslagPåMinsteinntektOppsett
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

// Understands how to build our application server
internal class ApplicationBuilder() : RapidsConnection.StatusListener {

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
                AvslagPåMinsteinntektOppsett.registrer { søknad, versjonId -> FaktumTable(søknad, versjonId) }
                NySøknadService(søknadRecord, rapidsConnection)
                FaktumSvarService(søknadRecord, rapidsConnection)
                BehandlingsdatoService(rapidsConnection)
                SenesteMuligeVirkningstidspunktService(rapidsConnection)
                TerskelFaktorService(rapidsConnection)
            }
    }
}

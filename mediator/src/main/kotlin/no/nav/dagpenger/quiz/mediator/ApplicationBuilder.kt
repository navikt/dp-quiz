package no.nav.dagpenger.quiz.mediator

import mu.KotlinLogging
import no.nav.dagpenger.model.marshalling.SøknadsmalJsonBuilder
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.quiz.mediator.behovløsere.BehandlingsdatoService
import no.nav.dagpenger.quiz.mediator.behovløsere.DokumentkravSvarService
import no.nav.dagpenger.quiz.mediator.behovløsere.MetadataService
import no.nav.dagpenger.quiz.mediator.behovløsere.MigrerProsessService
import no.nav.dagpenger.quiz.mediator.behovløsere.SenesteMuligeVirkningsdatoService
import no.nav.dagpenger.quiz.mediator.behovløsere.TerskelFaktorService
import no.nav.dagpenger.quiz.mediator.db.FaktaRecord
import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.quiz.mediator.db.ProsessRepositoryPostgres
import no.nav.dagpenger.quiz.mediator.db.ResultatRecord
import no.nav.dagpenger.quiz.mediator.meldinger.FaktaSlettetService
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.meldinger.ManuellBehandlingSink
import no.nav.dagpenger.quiz.mediator.meldinger.NyProsessBehovLøser
import no.nav.dagpenger.quiz.mediator.soknad.ProsessMetadataStrategi
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Dagpenger
import no.nav.dagpenger.quiz.mediator.soknad.innsending.Innsending
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.v248.Dagpenger as Dagpenger248

// Understands how to build our application server
internal class ApplicationBuilder : RapidsConnection.StatusListener {
    private val rapidsConnection = RapidApplication.Builder(
        RapidApplication.RapidApplicationConfig.fromEnv(Configuration.config),
    ).build()

    private companion object {
        val logger = KotlinLogging.logger {}
    }

    init {
        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()
    fun stop() = rapidsConnection.stop()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        runMigration()
            .also {
                val faktaRecord = FaktaRecord()
                val prosessRepository = ProsessRepositoryPostgres()
                val resultatRecord = ResultatRecord()

                Dagpenger248.registrer {
                    logger.info("Sørger for å støtte gamle versjoner, registrerer dagpenger versjon 248")
                }

                Dagpenger.registrer { prototype: Prosess ->
                    FaktumTable(prototype.fakta)
                    val malJson = SøknadsmalJsonBuilder(prototype).resultat().toString()
                    rapidsConnection.publish(JsonMessage(malJson, MessageProblems(malJson)).toJson())
                }

                Innsending.registrer { prototype: Prosess ->
                    FaktumTable(prototype.fakta)
                    val malJson = SøknadsmalJsonBuilder(prototype).resultat().toString()
                    rapidsConnection.publish(JsonMessage(malJson, MessageProblems(malJson)).toJson())
                }

                NyProsessBehovLøser(prosessRepository, rapidsConnection)
                FaktumSvarService(prosessRepository, resultatRecord, rapidsConnection)
                BehandlingsdatoService(rapidsConnection)
                SenesteMuligeVirkningsdatoService(rapidsConnection)
                TerskelFaktorService(rapidsConnection)
                ManuellBehandlingSink(rapidsConnection, resultatRecord)
                FaktaSlettetService(rapidsConnection, prosessRepository)
                MetadataService(
                    rapidsConnection,
                    prosessRepository,
                    ProsessMetadataStrategi(),
                )
                DokumentkravSvarService(rapidsConnection, prosessRepository)
                MigrerProsessService(rapidsConnection, faktaRecord, prosessRepository)
            }
    }
}

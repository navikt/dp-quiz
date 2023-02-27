package no.nav.dagpenger.quiz.mediator

import mu.KotlinLogging
import no.nav.dagpenger.quiz.mediator.behovløsere.BehandlingsdatoService
import no.nav.dagpenger.quiz.mediator.behovløsere.DokumentkravSvarService
import no.nav.dagpenger.quiz.mediator.behovløsere.MetadataService
import no.nav.dagpenger.quiz.mediator.behovløsere.MigrerProsessService
import no.nav.dagpenger.quiz.mediator.behovløsere.SenesteMuligeVirkningsdatoService
import no.nav.dagpenger.quiz.mediator.behovløsere.TerskelFaktorService
import no.nav.dagpenger.quiz.mediator.db.FaktaRecord
import no.nav.dagpenger.quiz.mediator.db.FaktumTable
import no.nav.dagpenger.quiz.mediator.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.quiz.mediator.db.ProsessRepositoryImpl
import no.nav.dagpenger.quiz.mediator.db.ResultatRecord
import no.nav.dagpenger.quiz.mediator.meldinger.AvslagPåMinsteinntektService
import no.nav.dagpenger.quiz.mediator.meldinger.FaktumSvarService
import no.nav.dagpenger.quiz.mediator.meldinger.ManuellBehandlingSink
import no.nav.dagpenger.quiz.mediator.meldinger.NyProsessBehovLøser
import no.nav.dagpenger.quiz.mediator.meldinger.SøknadSlettetService
import no.nav.dagpenger.quiz.mediator.meldinger.VilkårsvurderingLøser
import no.nav.dagpenger.quiz.mediator.soknad.ProsessMetadataStrategi
import no.nav.dagpenger.quiz.mediator.soknad.aldersvurdering.Paragraf_4_23_alder_oppsett
import no.nav.dagpenger.quiz.mediator.soknad.avslagminsteinntekt.AvslagPåMinsteinntektOppsett
import no.nav.dagpenger.quiz.mediator.soknad.dagpenger.Dagpenger
import no.nav.dagpenger.quiz.mediator.soknad.innsending.Innsending
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
                val utredningsprosessRepository = ProsessRepositoryImpl()
                val resultatRecord = ResultatRecord()
                AvslagPåMinsteinntektOppsett.registrer { prototypeSøknad -> FaktumTable(prototypeSøknad) }
                AvslagPåMinsteinntektService(utredningsprosessRepository, rapidsConnection)

                Dagpenger248.registrer {
                    logger.info("Sørger for å støtte gamle versjoner, registrerer dagpenger versjon 248")
                }
                Dagpenger.registrer { prototype ->
                    FaktumTable(prototype)

                    TODO("Se om denne trengs")
                    /*Versjon.id(Versjon.siste(Prosessfakta.Dagpenger)).also { versjon ->
                        val søknadsprosess = versjon.utredningsprosess(prototype)
                        val malJson = SøknadsmalJsonBuilder(søknadsprosess).resultat().toString()
                        rapidsConnection.publish(JsonMessage(malJson, MessageProblems(malJson)).toJson())
                    }*/
                }

                Innsending.registrer { prototype ->
                    FaktumTable(prototype)

                    /*Versjon.id(Versjon.siste(Prosessfakta.Innsending)).also { versjon ->
                        val søknadsprosess = versjon.utredningsprosess(prototype)
                        val malJson = SøknadsmalJsonBuilder(søknadsprosess).resultat().toString()
                        rapidsConnection.publish(JsonMessage(malJson, MessageProblems(malJson)).toJson())
                    }*/
                }

                if (Cluster.DEV_GCP == Cluster.current) {
                    Paragraf_4_23_alder_oppsett.registrer { prototype ->
                        FaktumTable(prototype)
                    }
                    VilkårsvurderingLøser(rapidsConnection, utredningsprosessRepository)
                }

                NyProsessBehovLøser(utredningsprosessRepository, rapidsConnection)
                FaktumSvarService(utredningsprosessRepository, resultatRecord, rapidsConnection)
                BehandlingsdatoService(rapidsConnection)
                SenesteMuligeVirkningsdatoService(rapidsConnection)
                TerskelFaktorService(rapidsConnection)
                ManuellBehandlingSink(rapidsConnection, resultatRecord)
                SøknadSlettetService(rapidsConnection, faktaRecord)
                MetadataService(
                    rapidsConnection,
                    utredningsprosessRepository,
                    ProsessMetadataStrategi(),
                )
                DokumentkravSvarService(rapidsConnection, utredningsprosessRepository)
                MigrerProsessService(rapidsConnection, faktaRecord, utredningsprosessRepository)
            }
    }
}

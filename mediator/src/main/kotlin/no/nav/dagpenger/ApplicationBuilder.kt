package no.nav.dagpenger

import com.natpryce.konfig.Configuration
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

class ApplicationBuilder(config: Configuration) : RapidsConnection.StatusListener {
    /*private val dataSourceBuilder = DataSourceBuilder(Subset(config, "database"))
    private val dataSource = dataSourceBuilder.getDataSource()*/

    private val rapidsConnection = RapidApplication.create(config.asMap())

    init {
        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()
    fun stop() = rapidsConnection.stop()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        // dataSourceBuilder.migrate()
    }
}

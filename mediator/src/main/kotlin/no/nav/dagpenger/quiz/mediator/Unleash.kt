package no.nav.dagpenger.quiz.mediator

import no.finn.unleash.DefaultUnleash
import no.finn.unleash.strategy.Strategy
import no.finn.unleash.util.UnleashConfig

const val FEATURE_MOTTA_SØKNAD = "dp-quiz.motta.soknad"

fun setupUnleash(unleashApiUrl: String): DefaultUnleash {
    val appName = "dp-regel-api"
    val unleashconfig = UnleashConfig.builder()
        .appName(appName)
        .instanceId(appName)
        .unleashAPI(unleashApiUrl)
        .build()

    return DefaultUnleash(unleashconfig, ByClusterStrategy(Cluster.current))
}

class ByClusterStrategy(private val currentCluster: Cluster) : Strategy {
    override fun getName(): String = "byCluster"

    override fun isEnabled(parameters: Map<String, String>?): Boolean {
        val clustersParameter = parameters?.get("cluster") ?: return false
        val alleClustere = clustersParameter.split(",").map { it.trim() }.map { it.toLowerCase() }.toList()
        return alleClustere.contains(currentCluster.asString())
    }
}

enum class Cluster {
    DEV_GCP, PROD_GCP, ANNET;

    companion object {
        val current: Cluster by lazy {
            when (System.getenv("NAIS_CLUSTER_NAME")) {
                "dev-gcp" -> DEV_GCP
                "prod-gcp" -> PROD_GCP
                else -> ANNET
            }
        }
    }

    fun asString(): String = name.toLowerCase().replace("_", "-")
}

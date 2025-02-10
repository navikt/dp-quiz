package no.nav.dagpenger.quiz.mediator

import io.getunleash.DefaultUnleash
import io.getunleash.UnleashContext
import io.getunleash.strategy.Strategy
import io.getunleash.util.UnleashConfig

const val FEATURE_MOTTA_SØKNAD = "dp-quiz-mediator.motta.soknad"

fun setupUnleash(unleashApiUrl: String): DefaultUnleash {
    val appName = "dp-quiz"
    val unleashconfig = UnleashConfig.builder()
        .appName(appName)
        .instanceId(appName)
        .unleashAPI(unleashApiUrl)
        .build()

    return DefaultUnleash(unleashconfig, ByClusterStrategy(Cluster.current))
}

class ByClusterStrategy(private val currentCluster: Cluster) : Strategy {
    override fun getName(): String = "byCluster"

    override fun isEnabled(parameters: MutableMap<String, String>, unleashContext: UnleashContext): Boolean {
        val clustersParameter = parameters["cluster"] ?: return false
        val alleClustere = clustersParameter.split(",").map { it.trim() }.map { it.lowercase() }.toList()
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

    fun asString(): String = name.lowercase().replace("_", "-")
}

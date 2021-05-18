package no.nav.dagpenger.quiz.mediator

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.overriding

internal object Configuration {
    private val defaultProperties = ConfigurationMap(
        mapOf(
            "KAFKA_CONSUMER_GROUP_ID" to "dp-quiz-mediator-v1",
            "KAFKA_RAPID_TOPIC" to "teamdagpenger.rapid.v1",
            "KAFKA_RESET_POLICY" to "latest",
            "unleash.url" to "https://unleash.nais.io/api/"
        )
    )
    private val properties = systemProperties() overriding EnvironmentVariables() overriding defaultProperties

    val config: Map<String, String> = properties.list().reversed().fold(emptyMap()) { map, pair ->
        map + pair.second
    }
}

package no.nav.dagpenger

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.getValue
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType

internal object Configuration {
    private val port by intType

    private val defaults = ConfigurationMap(
        port to "8080"
    )

    private val config = systemProperties() overriding EnvironmentVariables() overriding defaults

    val rapidApplication: Map<String, String> = mapOf(
        "RAPID_APP_NAME" to "dp-quiz",
        "KAFKA_BOOTSTRAP_SERVERS" to config[Key("kafka.bootstrap.servers", stringType)],
        "KAFKA_CONSUMER_GROUP_ID" to "dp-quiz-v1",
        "KAFKA_RAPID_TOPIC" to config[Key("kafka.topic", stringType)],
        // "KAFKA_EXTRA_TOPIC" to config()[Key("extra.kafka.topics", stringType)],
        "KAFKA_RESET_POLICY" to config[Key("kafka.reset.policy", stringType)],
        "NAV_TRUSTSTORE_PATH" to config[Key("nav.truststore.path", stringType)],
        "NAV_TRUSTSTORE_PASSWORD" to config[Key("nav.truststore.password", stringType)],
        "HTTP_PORT" to config[Key("port", stringType)]
    )
}

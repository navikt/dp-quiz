package no.nav.dagpenger

import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType

internal object Configuration {

    private val config = systemProperties() overriding EnvironmentVariables()

    val rapidApplication: Map<String, String> = mapOf(
        "RAPID_APP_NAME" to "dp-quiz",
        "KAFKA_BOOTSTRAP_SERVERS" to config[Key("kafka.bootstrap.servers", stringType)],
        "KAFKA_CONSUMER_GROUP_ID" to "dp-quiz-v1",
        "KAFKA_RAPID_TOPIC" to config.getOrElse(Key("kafka.topic", stringType), "privat-dagpenger-behov-v2"),
        "KAFKA_RESET_POLICY" to config.getOrElse(Key("kafka.reset.policy", stringType), "earliest"),
        "NAV_TRUSTSTORE_PATH" to config[Key("nav.truststore.path", stringType)],
        "NAV_TRUSTSTORE_PASSWORD" to config[Key("nav.truststore.password", stringType)],
        "HTTP_PORT" to config.getOrElse(Key("port", stringType), "8080")
    )
}

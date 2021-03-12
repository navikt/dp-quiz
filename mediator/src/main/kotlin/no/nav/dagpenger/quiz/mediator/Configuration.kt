package no.nav.dagpenger.quiz.mediator

import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.Key
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType

internal object Configuration {
    private val config = systemProperties() overriding EnvironmentVariables()

    val prodEnvironment =
        (System.getenv().getOrElse("NAIS_CLUSTER_NAME", { "tom" }).contains("prod"))

    val rapidApplication: Map<String, String> = mutableMapOf(
        "RAPID_APP_NAME" to "dp-quiz-mediator",
        "KAFKA_BROKERS" to config.getOrElse(Key("kafka.brokers", stringType), "localhost:9092"),
        "KAFKA_CONSUMER_GROUP_ID" to "dp-quiz-mediator-v1",
        "KAFKA_RAPID_TOPIC" to config.getOrElse(Key("kafka.topic", stringType), "teamdagpenger.rapid.v1"),
        "KAFKA_RESET_POLICY" to config.getOrElse(Key("kafka.reset.policy", stringType), "latest"),
        "KAFKA_CREDSTORE_PASSWORD" to config.getOrElse(Key("kafka.credstore.password", stringType), "/non/existing"),
        "HTTP_PORT" to config.getOrElse(Key("port", stringType), "8080")
    ).also {
        config.getOrNull(Key("kafka.truststore.path", stringType))?.let { truststore ->
            it["KAFKA_TRUSTSTORE_PATH"] = truststore
        }
        config.getOrNull(Key("kafka.keystore.path", stringType))?.let { keystore ->
            it["KAFKA_KEYSTORE_PATH"] = keystore
        }
    }
}

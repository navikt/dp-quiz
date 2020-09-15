package no.nav.dagpenger

import com.natpryce.konfig.Configuration
import com.natpryce.konfig.ConfigurationProperties.Companion.fromResource
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.PropertyGroup
import com.natpryce.konfig.getValue
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding
import com.natpryce.konfig.stringType

internal object application : PropertyGroup() {
    val name by stringType
    val port by intType
}

internal object kafka : PropertyGroup() {
    val rapid_topic by stringType
    val reset_policy by stringType
    val bootstrap_servers by stringType
    val consumer_group_id by stringType
}

internal object database : PropertyGroup() {
    val name by stringType
    val host by stringType
    val port by intType
    val username by stringType
    val password by stringType
    val jdbcUrl by stringType
}

internal object vault : PropertyGroup() {
    val mountpath by stringType
}

internal val nav_truststore_path by stringType
internal val nav_truststore_password by stringType

internal val config = systemProperties() overriding EnvironmentVariables overriding fromResource("defaults.properties")

internal fun Configuration.asMap(): Map<String, String> = (
    mapOf(
        "RAPID_APP_NAME" to this[application.name],
        "KAFKA_BOOTSTRAP_SERVERS" to this[kafka.bootstrap_servers],
        "KAFKA_CONSUMER_GROUP_ID" to this[kafka.consumer_group_id],
        "KAFKA_RAPID_TOPIC" to this[kafka.rapid_topic],
        "KAFKA_RESET_POLICY" to this[kafka.reset_policy],
        "NAV_TRUSTSTORE_PATH" to this[nav_truststore_path],
        "NAV_TRUSTSTORE_PASSWORD" to this[nav_truststore_password],
        "HTTP_PORT" to this[application.port].toString()
    ) + System.getenv().filter { it.key.startsWith("NAIS_") }
    )

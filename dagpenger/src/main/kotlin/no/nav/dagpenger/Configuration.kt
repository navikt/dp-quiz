package no.nav.dagpenger

import com.natpryce.konfig.ConfigurationMap
import com.natpryce.konfig.ConfigurationProperties.Companion.systemProperties
import com.natpryce.konfig.EnvironmentVariables
import com.natpryce.konfig.getValue
import com.natpryce.konfig.intType
import com.natpryce.konfig.overriding

val port by intType

val defaults = ConfigurationMap(
    port to "8080"
)

val config = systemProperties() overriding EnvironmentVariables() overriding defaults

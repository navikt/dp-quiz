package no.nav.dagpenger.model.helpers

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.readValue

private val objectMapper = jacksonMapperBuilder()
    .enable(SerializationFeature.INDENT_OUTPUT)
    .build()

internal fun String.toPrettyJson(): String? {
    val jsonNode = objectMapper.readValue<JsonNode>(this)
    return jsonNode.toPrettyJson()
}

internal fun JsonNode.toPrettyJson() = objectMapper.writeValueAsString(this)

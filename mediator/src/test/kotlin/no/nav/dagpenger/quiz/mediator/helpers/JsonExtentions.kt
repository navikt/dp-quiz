package no.nav.dagpenger.quiz.mediator.helpers

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import com.fasterxml.jackson.module.kotlin.readValue

object JsonExtentions {
    private val objectMapper = jacksonMapperBuilder()
        .enable(SerializationFeature.INDENT_OUTPUT)
        .build()

    fun String.toPrettyJson(): String? {
        val jsonNode = objectMapper.readValue<JsonNode>(this)
        return jsonNode.toPrettyJson()
    }

    fun JsonNode.toPrettyJson() = objectMapper.writeValueAsString(this)
}

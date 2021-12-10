package no.nav.dagpenger.quiz.mediator.helpers

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.dagpenger.model.factory.FaktaRegel
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.TemplateFaktum
import org.junit.jupiter.api.Assertions.assertEquals

@JsonIgnoreProperties("avhengigeFakta", "avhengerAvFakta")
private class GrunnlegendeFaktumMixin

@JsonIgnoreProperties("seksjoner")
private class TemplateFaktumMixin

@JsonIgnoreProperties("søknad")
private class GeneratorFaktumMixin

private class FaktaRegelSerializer : JsonSerializer<FaktaRegel<*>?>() {
    override fun serialize(
        regel: FaktaRegel<*>?,
        jsonGenerator: com.fasterxml.jackson.core.JsonGenerator,
        serializers: SerializerProvider?
    ) {
        regel?.let { jsonGenerator.writeString(it.navn) }
    }
}

private val module = SimpleModule().also { it.addSerializer(FaktaRegel::class.java, FaktaRegelSerializer()) }

private val objectMapper = jacksonObjectMapper()
    .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(module)
    .setMixIns(
        mutableMapOf(
            GrunnleggendeFaktum::class.java to GrunnlegendeFaktumMixin::class.java,
            TemplateFaktum::class.java to TemplateFaktumMixin::class.java,
            GeneratorFaktum::class.java to GeneratorFaktumMixin::class.java,
        ) as Map<Class<*>, Class<*>>?
    )
    .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
    .registerModule(JavaTimeModule())

internal fun assertJsonEquals(expected: Any, actual: Any) {
    val expectedJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(expected)
    val actualJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(actual)
    assertEquals(expectedJson, actualJson)
}

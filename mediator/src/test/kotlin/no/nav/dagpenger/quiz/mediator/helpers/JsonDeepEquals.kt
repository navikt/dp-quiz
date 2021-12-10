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
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.visitor.FaktumVisitor
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

private class GrunnleggendeFaktumSerializer : JsonSerializer<GrunnleggendeFaktum<*>>() {
    override fun serialize(
        faktum: GrunnleggendeFaktum<*>,
        jsonGenerator: com.fasterxml.jackson.core.JsonGenerator,
        serializers: SerializerProvider?
    ) {
        GrunnleggendeFaktumVisitor(faktum, jsonGenerator)
    }
}

private class GrunnleggendeFaktumVisitor(
    val faktum: GrunnleggendeFaktum<*>,
    private val jsonGenerator: com.fasterxml.jackson.core.JsonGenerator
) : FaktumVisitor {

    init {
        faktum.accept(this)
    }

    override fun <R : Comparable<R>> visit(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: Faktum.FaktumTilstand,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        godkjenner: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>
    ) {
        jsonGenerator.writeStartObject()
        skrivStandardFelt(id, tilstand, clazz, avhengigeFakta, avhengerAvFakta, godkjenner, roller)
        jsonGenerator.writeEndObject()
    }

    override fun <R : Comparable<R>> visit(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: Faktum.FaktumTilstand,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        godkjenner: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        svar: R,
        besvartAv: String?
    ) {
        jsonGenerator.writeStartObject()
        skrivStandardFelt(id, tilstand, clazz, avhengigeFakta, avhengerAvFakta, godkjenner, roller)

        jsonGenerator.writeStringField("gjeldendeSvar", svar.toString())
        jsonGenerator.writeEndObject()
    }

    private fun <R : Comparable<R>> skrivStandardFelt(
        id: String,
        tilstand: Faktum.FaktumTilstand,
        clazz: Class<R>,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        godkjenner: Set<Faktum<*>>,
        roller: Set<Rolle>
    ) {
        jsonGenerator.writeStringField("id", id)
        jsonGenerator.writeStringField("tilstand", tilstand.name)
        jsonGenerator.writeStringField("clazz", clazz.toString())
        jsonGenerator.writeArrayFieldStart("avhengigeFakta")
        avhengigeFakta.forEach { jsonGenerator.writeString(it.id) }
        jsonGenerator.writeEndArray()
        jsonGenerator.writeArrayFieldStart("avhengerAvFakta")
        avhengerAvFakta.forEach { jsonGenerator.writeString(it.id) }
        jsonGenerator.writeEndArray()
        jsonGenerator.writeArrayFieldStart("godkjenner")
        godkjenner.forEach { jsonGenerator.writeString(it.id) }
        jsonGenerator.writeEndArray()

        jsonGenerator.writeArrayFieldStart("roller")
        roller.forEach { jsonGenerator.writeString(it.typeNavn) }
        jsonGenerator.writeEndArray()
    }
}

private val module = SimpleModule()
    .also {
        it.addSerializer(
            FaktaRegel::class.java, FaktaRegelSerializer()
        )
    }
    .also {
        it.addSerializer(
            GrunnleggendeFaktum::class.java, GrunnleggendeFaktumSerializer()
        )
    }

private val objectMapper = jacksonObjectMapper()
    .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(module)
    .setMixIns(
        mutableMapOf(
            GrunnleggendeFaktum::class.java to GrunnlegendeFaktumMixin::class.java,
            TemplateFaktum::class.java to TemplateFaktumMixin::class.java,
            GeneratorFaktum::class.java to GeneratorFaktumMixin::class.java,
        ) as Map<Class<*>, Class<*>>
    )
    .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
    .registerModule(JavaTimeModule())

internal fun assertJsonEquals(expected: Any, actual: Any) {
    val expectedJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(expected)
    val actualJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(actual)
    assertEquals(expectedJson, actualJson)
}

package no.nav.dagpenger.quiz.mediator.helpers

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Valg
import no.nav.dagpenger.model.faktum.ValgFaktum
import no.nav.dagpenger.model.visitor.FaktumVisitor
import org.junit.jupiter.api.Assertions.assertEquals

private class FaktumSerializer : JsonSerializer<Faktum<*>>() {
    override fun serialize(
        faktum: Faktum<*>,
        jsonGenerator: JsonGenerator,
        serializers: SerializerProvider?
    ) {
        FaktumVisitor(faktum, jsonGenerator)
    }
}

private class FaktumVisitor(
    val faktum: Faktum<*>,
    private val jsonGenerator: JsonGenerator
) : FaktumVisitor {

    init {
        faktum.accept(this)
    }

    override fun <R : Comparable<R>> visit(
        faktum: ValgFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        godkjenner: Set<Faktum<*>>,
        gyldigeValg: Valg,
        roller: Set<Rolle>,
        clazz: Class<R>
    ) {
        jsonGenerator.writeStartObject()
        jsonGenerator.skrivStandardFelt(id, Faktum.FaktumTilstand.Ukjent, clazz, avhengigeFakta, avhengerAvFakta, godkjenner, roller)
        jsonGenerator.writeArrayFieldStart("gyldigeValg")
        gyldigeValg.forEach { jsonGenerator.writeString(it) }
        jsonGenerator.writeEndArray()
        jsonGenerator.writeEndObject()
    }

    override fun <R : Comparable<R>> visit(
        faktum: ValgFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        godkjenner: Set<Faktum<*>>,
        gyldigeValg: Valg,
        roller: Set<Rolle>,
        clazz: Class<R>,
        svar: Valg
    ) {
        jsonGenerator.writeStartObject()
        jsonGenerator.skrivStandardFelt(id, Faktum.FaktumTilstand.Kjent, clazz, avhengigeFakta, avhengerAvFakta, godkjenner, roller)
        jsonGenerator.writeArrayFieldStart("gyldigeValg")
        gyldigeValg.forEach { jsonGenerator.writeString(it) }
        jsonGenerator.writeEndArray()
        jsonGenerator.writeArrayFieldStart("gjeldendeSvar")
        svar.forEach { jsonGenerator.writeString(it) }
        jsonGenerator.writeEndArray()

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
        clazz: Class<R>
    ) {
        jsonGenerator.writeStartObject()
        jsonGenerator.skrivStandardFelt(id, tilstand, clazz, avhengigeFakta, avhengerAvFakta, godkjenner, roller)
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
        jsonGenerator.skrivStandardFelt(id, tilstand, clazz, avhengigeFakta, avhengerAvFakta, godkjenner, roller)
        jsonGenerator.writeStringField("gjeldendeSvar", svar.toString())
        jsonGenerator.writeEndObject()
    }

    private fun JsonGenerator.skrivStandardFelt(
        id: String,
        tilstand: Faktum.FaktumTilstand,
        clazz: Class<*>,
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
            Faktum::class.java,
            FaktumSerializer()
        )
    }

private val objectMapper = jacksonObjectMapper()
    .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    .registerModule(module)
    .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
    .registerModule(JavaTimeModule())

internal fun assertJsonEquals(expected: Any, actual: Any) {
    val expectedJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(expected)
    val actualJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(actual)
    assertEquals(expectedJson, actualJson)
}

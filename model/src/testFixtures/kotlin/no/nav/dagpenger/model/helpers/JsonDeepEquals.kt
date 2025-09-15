package no.nav.dagpenger.model.helpers

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
import no.nav.dagpenger.model.faktum.GyldigeValg
import no.nav.dagpenger.model.faktum.LandGrupper
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.visitor.FaktumVisitor
import org.junit.jupiter.api.Assertions.assertEquals

private class FaktumSerializer : JsonSerializer<Faktum<*>>() {
    override fun serialize(
        faktum: Faktum<*>,
        jsonGenerator: JsonGenerator,
        serializers: SerializerProvider?,
    ) {
        FaktumVisitor(faktum, jsonGenerator)
    }
}

private class FaktumVisitor(
    val faktum: Faktum<*>,
    private val jsonGenerator: JsonGenerator,
) : FaktumVisitor {
    init {
        faktum.accept(this)
    }

    override fun <R : Comparable<R>> visitUtenSvar(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: Faktum.FaktumTilstand,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        godkjenner: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        gyldigeValg: GyldigeValg?,
        landGrupper: LandGrupper?,
    ) {
        jsonGenerator.writeStartObject()
        jsonGenerator.skrivStandardFelt(id, tilstand, clazz, avhengigeFakta, avhengerAvFakta, godkjenner, roller)
        skrivGyldigeValg(gyldigeValg)
        jsonGenerator.writeEndObject()
    }

    override fun <R : Comparable<R>> visitMedSvar(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: Faktum.FaktumTilstand,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        godkjenner: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        svar: R,
        besvartAv: String?,
        gyldigeValg: GyldigeValg?,
        landGrupper: LandGrupper?,
    ) {
        jsonGenerator.writeStartObject()
        jsonGenerator.skrivStandardFelt(id, tilstand, clazz, avhengigeFakta, avhengerAvFakta, godkjenner, roller)
        skrivGyldigeValg(gyldigeValg)
        jsonGenerator.writeObjectField("gjeldendeSvar", svar)
        jsonGenerator.writeEndObject()
    }

    private fun skrivGyldigeValg(gyldigeValg: GyldigeValg?) {
        gyldigeValg?.let { valg ->
            jsonGenerator.writeArrayFieldStart("gyldigeValg")
            valg.forEach { jsonGenerator.writeString(it) }
            jsonGenerator.writeEndArray()
        }
    }

    private fun JsonGenerator.skrivStandardFelt(
        id: String,
        tilstand: Faktum.FaktumTilstand,
        type: Class<*>,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        godkjenner: Set<Faktum<*>>,
        roller: Set<Rolle>,
    ) {
        jsonGenerator.writeStringField("id", id)
        jsonGenerator.writeStringField("tilstand", tilstand.name)
        jsonGenerator.writeStringField("type", type.name)
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

private val module =
    SimpleModule()
        .also {
            it.addSerializer(
                Faktum::class.java,
                FaktumSerializer(),
            )
        }

private val objectMapper =
    jacksonObjectMapper()
        .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .registerModule(module)
        .setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
        .registerModule(JavaTimeModule())

fun assertJsonEquals(
    expected: Any,
    actual: Any,
) {
    val expectedJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(expected)
    val actualJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(actual)
    assertEquals(expectedJson, actualJson)
}

package no.nav.dagpenger.model.unit.marshalling

import com.fasterxml.jackson.databind.JsonNode
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

internal fun JsonNode.finnSeksjon(seksjon: String): JsonNode {
    val jsonNode = this["seksjoner"].find { it["beskrivendeId"].asText() == seksjon }
    return checkNotNull(jsonNode) { "Fant ikke seksjon med navn '$seksjon' i json \n ${this.toPrettyString()}" }
}

internal fun JsonNode.assertFaktaAsJson(
    expectedId: String,
    expectedType: String,
    expectedBeskrivendeId: String,
    expectedRoller: List<String>,
    assertSvar: ((JsonNode) -> Unit)? = null
) {
    assertEquals(expectedBeskrivendeId, this.get("beskrivendeId").asText())
    assertEquals(expectedType, this.get("type").asText())
    assertEquals(expectedId, this.get("id").asText())
    if (expectedRoller.isNotEmpty()) {
        val actual: List<String> = this.get("roller").toSet().map { it.asText() }
        assertEquals(
            expectedRoller.size,
            actual.size,
            "$expectedBeskrivendeId har $actual, forventet $expectedRoller "
        )
        Assertions.assertTrue(expectedRoller.containsAll<String>(actual)) { "$expectedBeskrivendeId har $actual, forventet $expectedRoller " }
    }
    assertSvar?.let { assert -> assert(this["svar"]) }
}

internal fun JsonNode.assertLandFaktum(
    expectedId: String,
    expectedType: String,
    expectedBeskrivendeId: String,
    expectedRoller: List<String>,
    assertSvar: ((JsonNode) -> Unit)? = null
) {
    this.assertFaktaAsJson(expectedId, expectedType, expectedBeskrivendeId, expectedRoller, assertSvar)
    assertTrue(this.has("gyldigeLand"), "Forventer at landfaktum har gyldige land")
    assertTrue(0 < this.get("gyldigeLand").size(), "Forventet at gyldige land ikke er tom")
}

internal fun JsonNode.assertGeneratorFaktaAsJson(
    expectedId: String,
    expectedType: String,
    expectedBeskrivendeId: String,
    expectedRoller: List<String>,
    assertTemplates: List<(JsonNode) -> Unit>,
    assertSvar: ((JsonNode) -> Unit)? = null
) {
    this.assertFaktaAsJson(expectedId, expectedType, expectedBeskrivendeId, expectedRoller, assertSvar)
    assertTemplates.forEachIndexed { index: Int, test: (JsonNode) -> Unit ->
        test(this.get("templates")[index])
    }
}

internal fun JsonNode.assertValgFaktaAsJson(
    expectedId: String,
    expectedClass: String,
    expectedNavn: String,
    expectedRoller: List<String>,
    expectedGyldigeValg: List<String>,
    assertSvar: ((JsonNode) -> Unit)? = null
) {
    this.assertFaktaAsJson(expectedId, expectedClass, expectedNavn, expectedRoller, assertSvar)
    val expectedGyldigeValgMedPrefix = expectedGyldigeValg.map { "$expectedNavn.$it" }
    val actual: List<String> = this.get("gyldigeValg").toSet().map { it.asText() }
    Assertions.assertTrue(expectedGyldigeValgMedPrefix.containsAll<String>(actual)) { "\nExpected: $expectedGyldigeValg\n  Actual: $actual" }
}

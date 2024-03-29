package no.nav.dagpenger.model.helpers

import com.fasterxml.jackson.databind.JsonNode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

fun JsonNode.finnSeksjon(seksjon: String): JsonNode {
    val jsonNode = this["seksjoner"].find { it["beskrivendeId"].asText() == seksjon }
    return checkNotNull(jsonNode) { "Fant ikke seksjon med navn '$seksjon' i json \n ${this.toPrettyString()}" }
}

fun JsonNode.assertFaktaAsJson(
    expectedId: String,
    expectedType: String,
    expectedBeskrivendeId: String,
    expectedRoller: List<String>,
    readOnly: Boolean = false,
    assertSvar: ((JsonNode) -> Unit)? = null
) {
    val beskrivendeId = this.get("beskrivendeId").asText()
    assertEquals(expectedBeskrivendeId, beskrivendeId)
    assertEquals(expectedType, this.get("type").asText())
    assertEquals(expectedId, this.get("id").asText())
    if (this.has("readOnly")) {
        val forventetReadOnly = this.get("readOnly").asBoolean()
        assertEquals(readOnly, forventetReadOnly, "Forventet at $beskrivendeId har readOnly $readOnly men har $forventetReadOnly")
    }
    if (expectedRoller.isNotEmpty()) {
        val actual: List<String> = this.get("roller").toSet().map { it.asText() }
        assertEquals(
            expectedRoller.size,
            actual.size,
            "$expectedBeskrivendeId har $actual, forventet $expectedRoller "
        )
        assertTrue(expectedRoller.containsAll<String>(actual)) { "$expectedBeskrivendeId har $actual, forventet $expectedRoller " }
    }
    assertSvar?.let { assert -> assert(this["svar"]) }
}

internal fun JsonNode.assertLandFaktum(
    expectedId: String,
    expectedType: String,
    expectedBeskrivendeId: String,
    expectedRoller: List<String>,
    expectedLandgruppeIder: Set<String>,
    readOnly: Boolean = false,
    assertSvar: ((JsonNode) -> Unit)? = null
) {
    this.assertFaktaAsJson(expectedId, expectedType, expectedBeskrivendeId, expectedRoller, readOnly, assertSvar)
    assertTrue(this.has("grupper"), "Landfaktum må ha grupper")
    assertEquals(expectedLandgruppeIder, this["grupper"].map { it["gruppeId"].asText() }.toSet())
    assertTrue(this.has("gyldigeLand"), "Forventer at landfaktum har gyldige land")
    assertTrue(0 < this.get("gyldigeLand").size(), "Forventet at gyldige land ikke er tom")
}

fun JsonNode.assertGeneratorFaktaAsJson(
    expectedId: String,
    expectedType: String,
    expectedBeskrivendeId: String,
    expectedRoller: List<String>,
    readOnly: Boolean = false,
    assertTemplates: List<(JsonNode) -> Unit>,
    assertSvar: ((JsonNode) -> Unit)? = null
) {
    this.assertFaktaAsJson(expectedId, expectedType, expectedBeskrivendeId, expectedRoller, readOnly, assertSvar)
    assertTemplates.forEachIndexed { index: Int, test: (JsonNode) -> Unit ->
        test(this.get("templates")[index])
    }
}

fun JsonNode.assertValgFaktaAsJson(
    expectedId: String,
    expectedClass: String,
    expectedNavn: String,
    expectedRoller: List<String>,
    expectedGyldigeValg: List<String>,
    readOnly: Boolean = false,
    assertSvar: ((JsonNode) -> Unit)? = null
) {
    this.assertFaktaAsJson(expectedId, expectedClass, expectedNavn, expectedRoller, readOnly, assertSvar)
    val expectedGyldigeValgMedPrefix = expectedGyldigeValg.map { "$expectedNavn.$it" }
    val actual: List<String> = this.get("gyldigeValg").toSet().map { it.asText() }
    assertTrue(expectedGyldigeValgMedPrefix.containsAll<String>(actual)) { "\nExpected: $expectedGyldigeValg\n  Actual: $actual" }
}

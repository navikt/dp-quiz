package no.nav.dagpenger.qamodel

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

internal class HandlingTest {
    val sisteDagMedLønn = Fakta("Siste dag du har lønn", DatoStrategi())
    val inntekt1_5G = Fakta("Inntekt er lik eller over 1.5G siste 12 måneder", JaNeiStrategi({},{}))
    val inntekt3G = Fakta("Inntekt er lik eller over 3G siste 3 år", JaNeiStrategi({}, {inntekt1_5G.spør()}))

    @Test
    fun `Utføre handlinger`(){
        assertThrows<IllegalStateException> {inntekt1_5G.besvar(true) }
        inntekt3G.spør().besvar(false)
        assertEquals(Ja(inntekt1_5G),inntekt1_5G.besvar(true))
    }
}

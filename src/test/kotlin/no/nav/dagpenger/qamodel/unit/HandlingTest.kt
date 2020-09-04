package no.nav.dagpenger.qamodel.unit

import no.nav.dagpenger.qamodel.fakta.*
import no.nav.dagpenger.qamodel.handling.Handling
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

internal class HandlingTest {

    var teller = 0

    val inntekt1_5G = Faktum(
        "Inntekt er lik eller over 1.5G siste 12 måneder",
        JaNeiStrategi(
            object : Handling() {},
            object : Handling() {}
        )
    )
    val inntekt3G = Faktum(
        "Inntekt er lik eller over 3G siste 3 år",
        JaNeiStrategi(
            object : Handling() {},
            object : Handling(inntekt1_5G) {
                override fun utfør() { teller++ }
            }
        )
    )

    val sisteDagMedLønn = Faktum(
            "Siste dag du har lønn",
            DatoStrategi(
                    object : Handling(inntekt1_5G, inntekt3G) {}
            )
    )

    @Test
    fun `Utføre handlinger`() {
        assertThrows<IllegalStateException> { inntekt1_5G.besvar(true) }
        inntekt3G.spør().besvar(false)
        assertEquals(Ja(inntekt1_5G), inntekt1_5G.besvar(true))
        assertEquals(1, teller)
    }

    @Test
    fun `Flere handlinger`(){
        sisteDagMedLønn.spør().besvar(LocalDate.now())
        assertEquals(Ja(inntekt1_5G), inntekt1_5G.besvar(true))
        assertEquals(Ja(inntekt3G), inntekt3G.besvar(true))
    }

    @Test
    fun `Visitor teller`(){
        TellerVisitor().apply{
            sisteDagMedLønn.accept(this)
            assertEquals(4, faktumTeller)
            assertEquals(7, handlingTeller)
            assertEquals(4, strategiTeller)
        }
    }
    private class TellerVisitor(): FaktumVisitor{
        var faktumTeller = 0
        var handlingTeller = 0
        var strategiTeller = 0

        override fun preVisit(faktum: Faktum<*>) {
            faktumTeller++
        }

        override fun preVisit(handling: Handling) {
            handlingTeller++
        }

        override fun preVisit(strategi: SpørsmålStrategi<*>) {
            strategiTeller++
        }
    }
}

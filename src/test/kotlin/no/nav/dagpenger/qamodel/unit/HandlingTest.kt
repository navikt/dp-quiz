package no.nav.dagpenger.qamodel.unit

import java.time.LocalDate
import no.nav.dagpenger.qamodel.fakta.DatoStrategi
import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.fakta.Ja
import no.nav.dagpenger.qamodel.fakta.JaNeiStrategi
import no.nav.dagpenger.qamodel.handling.Handling
import no.nav.dagpenger.qamodel.visitor.FaktumVisitor
import no.nav.dagpenger.qamodel.visitor.PrettyPrint
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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
    fun `Flere handlinger`() {
        sisteDagMedLønn.spør().besvar(LocalDate.now())
        assertEquals(Ja(inntekt1_5G), inntekt1_5G.besvar(true))
        assertEquals(Ja(inntekt3G), inntekt3G.besvar(true))
    }

    @Test
    fun `Visitor teller`() {
        TellerVisitor().apply {
            sisteDagMedLønn.accept(this)
            assertEquals(4, faktumTeller)
            assertEquals(7, handlingTeller)
        }
        println(PrettyPrint(sisteDagMedLønn).result())
    }

    private class TellerVisitor() : FaktumVisitor {
        var faktumTeller = 0
        var handlingTeller = 0

        override fun preVisitJaNei(faktum: Faktum<Boolean>) {
            faktumTeller++
        }

        override fun preVisitDato(faktum: Faktum<LocalDate>) {
            faktumTeller++
        }

        override fun preVisitJa(handling: Handling) {
            handlingTeller++
        }

        override fun preVisitNei(handling: Handling) {
            handlingTeller++
        }

        override fun preVisitDato(handling: Handling) {
            handlingTeller++
        }
    }
}

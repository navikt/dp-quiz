package no.nav.dagpenger.qamodel.unit

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

    val villigDeltid = Faktum(
        "Villig til å jobbe deltid",
        JaNeiStrategi(
            object : Handling() {},
            object : Handling() {}
        )
    )

    val inntekt3G = Faktum(
        "Inntekt er lik eller over 3G siste 3 år",
        JaNeiStrategi(
            object : Handling() {},
            object : Handling(villigDeltid) {
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
    fun `at vi utfører en handling etter besvart spørsmål`() {
        assertThrows<IllegalStateException> { villigDeltid.besvar(true) }
        inntekt3G.spør().besvar(false)
        assertEquals(Ja(villigDeltid), villigDeltid.besvar(true))
        assertEquals(1, teller)
    }

    @Test
    fun `at vi utfører flere handlinger etter besvart spørsmål`() {
        sisteDagMedLønn.spør().besvar(LocalDate.now())
        assertEquals(Ja(inntekt1_5G), inntekt1_5G.besvar(true))
        assertEquals(Ja(inntekt3G), inntekt3G.besvar(true))
        assertEquals(3, TellerVisitor(sisteDagMedLønn).besvarteSpørsmålTeller)
    }

    @Test
    fun `Visitor teller`() {
        TellerVisitor(sisteDagMedLønn).apply {
            assertEquals(4, faktumTeller)
            assertEquals(7, handlingTeller)
        }
        println(PrettyPrint(sisteDagMedLønn).result())
    }

    private class TellerVisitor(faktum: Faktum<*>) : FaktumVisitor {
        var faktumTeller = 0
        var handlingTeller = 0
        var besvarteSpørsmålTeller = 0
        val ubesvarteSpørsmålTeller get() = faktumTeller - besvarteSpørsmålTeller

        init {
            faktum.accept(this)
        }

        override fun preVisitJaNei(faktum: Faktum<Boolean>, tilstand: Faktum.FaktumTilstand) {
            faktumTeller++
            if (tilstand == Faktum.FaktumTilstand.Kjent) besvarteSpørsmålTeller++
        }

        override fun preVisitDato(faktum: Faktum<LocalDate>, tilstand: Faktum.FaktumTilstand) {
            faktumTeller++
            if (tilstand == Faktum.FaktumTilstand.Kjent) besvarteSpørsmålTeller++
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

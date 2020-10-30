package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktum.Fakta
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class GyldigeValgTest {

    private object choice {
        infix fun faktum(navn: String) = BaseFaktumFactory(Choice::class.java, navn)
    }

    companion object {
        private enum class Choice { A, B, C, D, E }
        private val prototypeFakta = Fakta(
            choice faktum "multiple choice" id 1,
            choice faktum "valid choices" id 2,
            dokument faktum "dokument" id 3,
            ja nei "approval" id 4,
        )
    }

    internal fun setup() {
//        TODO wite a test that actually tests multiple
//        f1Choice = fn1Choice.faktum(Choice::class.java)
//        f2AcceptableChoices = fn2AcceptableChoices.faktum(f1Choice, Choice.A, Choice.C, Choice.E)
//        f3Dokument = fn3Dokument.faktum(Dokument::class.java)
//        f4Approval = fn4Approval.faktum(Boolean::class.java)
        Choice.A < Choice.B
    }

    @Test
    fun a() {
        assertTrue(Choice.A < Choice.B)
    }
}

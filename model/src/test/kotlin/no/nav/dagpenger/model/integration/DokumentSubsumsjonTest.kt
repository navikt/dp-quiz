package no.nav.dagpenger.model.integration

import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.faktum

internal class DokumentSubsumsjonTest {
    /*
    * Scenario: Given a multiple choice question, we will need an uploaded
    * document for certain choices. That document will need approval before
    * final determination.
    *
    * Example: Reason for leaving a job
    * Example: Reason for not wanting to relocate
    * */

    private enum class Choice { A, B, C, D, E }

    companion object {
        private val fn1Choice = FaktumNavn(1, "multiple choice")
        private val fn2AcceptableChoices = FaktumNavn(2, "valid choices")
        private val fn3Dokument = FaktumNavn(3, "dokument")
        private val fn4Approval = FaktumNavn(4, "approval")

        private val p1Choice = fn1Choice.faktum(Choice::class.java)
//        private val p2AcceptableChoices = fn2AcceptableChoices.faktum(p1Choice, Choice.A, Choice.C, Choice.E)
        private val p3Dokument = fn3Dokument.faktum(Dokument::class.java)
        private val p4Approval = fn4Approval.faktum(Boolean::class.java)
    }

    private lateinit var f1Choice: Faktum<Choice>
    private lateinit var f2AcceptableChoices: Faktum<Boolean>
    private lateinit var f3Dokument: Faktum<Dokument>
    private lateinit var f4Approval: Faktum<Boolean>

    internal fun setup() {
        f1Choice = fn1Choice.faktum(Choice::class.java)
//        f2AcceptableChoices = fn2AcceptableChoices.faktum(f1Choice, Choice.A, Choice.C, Choice.E)
        f3Dokument = fn3Dokument.faktum(Dokument::class.java)
        f4Approval = fn4Approval.faktum(Boolean::class.java)
    }
}

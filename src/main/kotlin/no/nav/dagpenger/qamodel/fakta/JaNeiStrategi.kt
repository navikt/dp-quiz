package no.nav.dagpenger.qamodel.fakta

import no.nav.dagpenger.qamodel.handling.Handling
import no.nav.dagpenger.qamodel.visitor.FaktumVisitor

internal class JaNeiStrategi(
    private val jaHandling: Handling,
    private val neiHandling: Handling
) : SpørsmålStrategi<Boolean> {
    override fun besvar(svar: Boolean, faktum: Faktum<Boolean>): Svar = if (svar) Ja(faktum).also {
        jaHandling.apply {
            utfør()
            nesteSpørsmål()
        }
    } else Nei(faktum).also {
        neiHandling.apply {
            utfør()
            nesteSpørsmål()
        }
    }

    override fun accept(visitor: FaktumVisitor, faktum: Faktum<Boolean>) {
        visitor.preVisitJaNei(faktum)

        visitor.preVisitJa(jaHandling)
        jaHandling.accept(visitor)
        visitor.postVisitJa(jaHandling)

        visitor.preVisitNei(neiHandling)
        neiHandling.accept(visitor)
        visitor.postVisitNei(neiHandling)

        visitor.postVisitJaNei(faktum)
    }
}

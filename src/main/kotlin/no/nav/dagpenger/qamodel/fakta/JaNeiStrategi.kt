package no.nav.dagpenger.qamodel.fakta

import no.nav.dagpenger.qamodel.handling.Handling

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

    override fun accept(visitor: FaktumVisitor) {
        visitor.preVisit(this)
        jaHandling.accept(visitor)
        neiHandling.accept(visitor)
        visitor.postVisit(this)
    }
}

package no.nav.dagpenger.qamodel.fakta

import no.nav.dagpenger.qamodel.handling.Handling
import no.nav.dagpenger.qamodel.visitor.FaktumVisitor

internal class JaNeiStrategi(
    private val jaHandling: Handling<Boolean>,
    private val neiHandling: Handling<Boolean>
) : SpørsmålStrategi<Boolean> {
    override fun besvar(svar: Boolean, faktum: Faktum<Boolean>) = if (svar) jaHandling else neiHandling

    override fun accept(visitor: FaktumVisitor, faktum: Faktum<Boolean>, tilstand: Faktum.FaktumTilstand) {
        visitor.preVisitJaNei(faktum, tilstand)

        visitor.preVisitJa(jaHandling)
        jaHandling.accept(visitor)
        visitor.postVisitJa(jaHandling)

        visitor.preVisitNei(neiHandling)
        neiHandling.accept(visitor)
        visitor.postVisitNei(neiHandling)

        visitor.postVisitJaNei(faktum, tilstand)
    }
}

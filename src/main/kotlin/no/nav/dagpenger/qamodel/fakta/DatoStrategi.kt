package no.nav.dagpenger.qamodel.fakta

import no.nav.dagpenger.qamodel.handling.Handling
import no.nav.dagpenger.qamodel.visitor.FaktumVisitor
import java.time.LocalDate

internal class DatoStrategi(private val handling: Handling) : SpørsmålStrategi<LocalDate> {
    override fun besvar(dato: LocalDate, faktum: Faktum<LocalDate>) =
        DatoSvar(faktum, dato).also {
            handling.apply {
                utfør()
                nesteSpørsmål()
            }
        }

    override fun accept(visitor: FaktumVisitor, faktum: Faktum<LocalDate>, tilstand: Faktum.FaktumTilstand) {
        visitor.preVisitDato(faktum, tilstand)

        visitor.preVisitDato(handling)
        handling.accept(visitor)
        visitor.postVisitDato(handling)

        visitor.postVisitDato(faktum, tilstand)
    }
}

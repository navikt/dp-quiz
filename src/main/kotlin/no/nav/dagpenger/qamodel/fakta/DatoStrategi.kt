package no.nav.dagpenger.qamodel.fakta

import java.time.LocalDate
import no.nav.dagpenger.qamodel.handling.Handling

internal class DatoStrategi(private val handling: Handling) : SpørsmålStrategi<LocalDate> {
    override fun besvar(dato: LocalDate, faktum: Faktum<LocalDate>) =
            DatoSvar(faktum, dato).also {
                handling.apply {
                    utfør()
                    nesteSpørsmål()
                }
            }

    override fun accept(visitor: FaktumVisitor, faktum: Faktum<LocalDate>) {
        visitor.preVisitDato(faktum)

        visitor.preVisitDato(handling)
        handling.accept(visitor)
        visitor.postVisitDato(handling)

        visitor.postVisitDato(faktum)
    }
}

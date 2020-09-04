package no.nav.dagpenger.qamodel.fakta

import no.nav.dagpenger.qamodel.handling.Handling
import java.time.LocalDate

internal class DatoStrategi(private val handling: Handling) : SpørsmålStrategi<LocalDate> {
    override fun besvar(dato: LocalDate, faktum: Faktum<LocalDate>) =
            DatoSvar(faktum, dato).also {
                handling.apply {
                    utfør()
                    nesteSpørsmål()
                }
            }

    override fun accept(visitor: FaktumVisitor) {
        visitor.preVisit(this)
        handling.accept(visitor)
        visitor.postVisit(this)
    }
}

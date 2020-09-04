package no.nav.dagpenger.qamodel.fakta

import java.time.LocalDate

internal class DatoStrategi : SpørsmålStrategi<LocalDate> {
    override fun besvar(dato: LocalDate, faktum: Faktum<LocalDate>) = DatoSvar(faktum, dato)
}

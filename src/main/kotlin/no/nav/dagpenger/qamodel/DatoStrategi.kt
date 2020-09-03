package no.nav.dagpenger.qamodel

import java.time.LocalDate

internal class DatoStrategi(private val fakta: Fakta) : SpørsmålStrategi<LocalDate> {
    override fun besvar(dato: LocalDate) = DatoSvar(fakta, dato)
}

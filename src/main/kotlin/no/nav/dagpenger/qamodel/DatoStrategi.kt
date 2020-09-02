package no.nav.dagpenger.qamodel

import java.time.LocalDate

internal class DatoStrategi(private val fakta: Fakta) : SpørsmålStrategi {
    override fun besvar(dato: Any) = DatoSvar(fakta, dato as LocalDate)
}

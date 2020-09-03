package no.nav.dagpenger.qamodel

import java.time.LocalDate

internal class DatoStrategi : SpørsmålStrategi<LocalDate> {
    override fun besvar(dato: LocalDate, fakta: Fakta<LocalDate>) = DatoSvar(fakta, dato)
}

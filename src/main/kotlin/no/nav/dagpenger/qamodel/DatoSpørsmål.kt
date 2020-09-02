package no.nav.dagpenger.qamodel

import java.time.LocalDate

class DatoSpørsmål(private val fakta: Fakta) {
    fun svar(dato: LocalDate) = DatoSvar(fakta, dato)
}

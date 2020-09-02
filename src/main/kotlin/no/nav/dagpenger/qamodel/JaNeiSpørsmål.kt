package no.nav.dagpenger.qamodel

class JaNeiSpørsmål(private val fakta: Fakta) {
    fun svar(svar: Boolean): Svar = if (svar) Ja(fakta) else Nei(fakta)
}

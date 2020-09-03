package no.nav.dagpenger.qamodel

internal class JaNeiStrategi(private val fakta: Fakta) : SpørsmålStrategi<Boolean> {
    override fun besvar(svar: Boolean): Svar = if (svar) Ja(fakta) else Nei(fakta)
}

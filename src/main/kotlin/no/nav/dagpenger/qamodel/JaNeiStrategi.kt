package no.nav.dagpenger.qamodel

internal class JaNeiStrategi(private val fakta: Fakta) : SpørsmålStrategi {
    override fun besvar(svar: Any): Svar = if (svar as Boolean) Ja(fakta) else Nei(fakta)
}

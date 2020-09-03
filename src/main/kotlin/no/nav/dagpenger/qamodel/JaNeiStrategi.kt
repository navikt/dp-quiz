package no.nav.dagpenger.qamodel

internal class JaNeiStrategi : SpørsmålStrategi<Boolean> {
    override fun besvar(svar: Boolean, fakta: Fakta<Boolean>): Svar = if (svar) Ja(fakta) else Nei(fakta)
}

package no.nav.dagpenger.qamodel.fakta

import no.nav.dagpenger.qamodel.handling.Handling

internal class JaNeiStrategi(
    private val jaStrategi: Handling,
    private val neiStrategi: Handling
) : SpørsmålStrategi<Boolean> {
    override fun besvar(svar: Boolean, fakta: Fakta<Boolean>): Svar = if (svar) Ja(fakta).also {
        jaStrategi.nesteSpørsmål()
    } else Nei(fakta).also {
        neiStrategi.nesteSpørsmål()
    }
}

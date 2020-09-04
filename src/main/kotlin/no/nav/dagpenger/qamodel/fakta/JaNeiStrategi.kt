package no.nav.dagpenger.qamodel.fakta

import no.nav.dagpenger.qamodel.handling.Handling

internal class JaNeiStrategi(
    private val jaStrategi: Handling,
    private val neiStrategi: Handling
) : SpørsmålStrategi<Boolean> {
    override fun besvar(svar: Boolean, faktum: Faktum<Boolean>): Svar = if (svar) Ja(faktum).also {
        jaStrategi.nesteSpørsmål()
    } else Nei(faktum).also {
        neiStrategi.nesteSpørsmål()
    }
}

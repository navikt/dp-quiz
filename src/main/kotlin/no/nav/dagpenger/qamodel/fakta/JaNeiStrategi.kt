package no.nav.dagpenger.qamodel.fakta

import no.nav.dagpenger.qamodel.handling.Handling

internal class JaNeiStrategi(
    private val jaStrategi: Handling,
    private val neiStrategi: Handling
) : SpørsmålStrategi<Boolean> {
    override fun besvar(svar: Boolean, faktum: Faktum<Boolean>): Svar = if (svar) Ja(faktum).also {
        jaStrategi.apply {
            utfør()
            nesteSpørsmål()
        }
    } else Nei(faktum).also {
        neiStrategi.apply {
            utfør()
            nesteSpørsmål()
        }
    }
}

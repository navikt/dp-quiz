package no.nav.dagpenger.qamodel.fakta

internal class JaNeiStrategi(private val jaStrategi: SvarStrategi, private val neiStrategi: SvarStrategi) : SpørsmålStrategi<Boolean> {
    override fun besvar(svar: Boolean, fakta: Fakta<Boolean>): Svar = if (svar) Ja(fakta).also {
        jaStrategi()
    } else Nei(fakta).also {
        neiStrategi()
    }
}

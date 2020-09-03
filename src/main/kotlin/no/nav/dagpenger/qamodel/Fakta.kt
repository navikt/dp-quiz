package no.nav.dagpenger.qamodel

class Fakta <R> private constructor(navn: String, private val strategi: SpørsmålStrategi<R>) {
    fun spørsmål(): Spørsmål<R> = Spørsmål(this, strategi)

    companion object {
        val inntekt3G = Fakta("Inntekt er lik eller over 3G siste 3 år", JaNeiStrategi())
        val sisteDagMedLønn = Fakta("Siste dag du har lønn", DatoStrategi())
    }
}

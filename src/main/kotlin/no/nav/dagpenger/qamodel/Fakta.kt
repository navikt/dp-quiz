package no.nav.dagpenger.qamodel

class Fakta private constructor(navn: String) {
    fun jaNei(): JaNeiSpørsmål = JaNeiSpørsmål(this)
    fun dato(): DatoSpørsmål = DatoSpørsmål(this)

    companion object {
        val inntekt3G = Fakta("Inntekt er lik eller over 3G siste 3 år")
        val sisteDagMedLønn = Fakta("Siste dag du har lønn")
    }
}

package no.nav.dagpenger.model.faktum

data class ProsessVersjon(val navn: String, val versjon: Int) {
    companion object {
        val prototypeversjon = ProsessVersjon("Prototype", 1)
    }
}

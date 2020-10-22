package no.nav.dagpenger

internal class EnkelSøknad : SøknadBygger {
    private var prototype = Prototype()

    override fun søknad() = prototype.søknad("")
}

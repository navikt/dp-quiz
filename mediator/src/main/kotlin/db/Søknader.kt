package db

import no.nav.dagpenger.model.søknad.Søknad
import java.util.UUID

internal interface Søknader {
    fun nySøknad(søknad: Søknad)
}

internal class InMemorySøknader() : Søknader {
    private val søknader = mutableMapOf<UUID, Søknad>()
    override fun nySøknad(søknad: Søknad) {
        TODO("Not yet implemented")
    }
}

package no.nav.dagpenger

import no.nav.dagpenger.model.søknad.Søknad
import java.util.UUID

interface Søknader {

    fun søknad(id: UUID): Søknad
}

class InMemorySøknader(val søknadBuilder: () -> Søknad) : Søknader {
    private val søknader = mutableMapOf<UUID, Søknad>()

    override fun søknad(id: UUID) = getOrCreateSøknad(id)

    private fun getOrCreateSøknad(id: UUID) =
        søknader.getOrPut(id) {
            søknadBuilder()
        }
}

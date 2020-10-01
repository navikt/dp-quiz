package no.nav.dagpenger

import no.nav.dagpenger.model.søknad.Søknad
import java.util.UUID

interface Søknader {
    fun søknad(id: UUID): Søknad
}

fun interface SøknadBygger {
    fun søknad(): Søknad
}

class InMemorySøknader(val søknadBuilder: SøknadBygger) : Søknader {
    private val søknader = mutableMapOf<UUID, Søknad>()

    override fun søknad(id: UUID) = getOrCreateSøknad(id)

    private fun getOrCreateSøknad(id: UUID) =
        søknader.getOrPut(id) {
            søknadBuilder.søknad()
        }
}

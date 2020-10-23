package db

import no.nav.dagpenger.model.søknad.Søknad

internal interface Søknader {
    fun persister(søknad: Søknad)
}

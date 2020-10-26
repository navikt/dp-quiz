package db

import no.nav.dagpenger.model.søknad.Søknad

internal interface FaktaPersistance {
    fun persister(søknad: Søknad)
}

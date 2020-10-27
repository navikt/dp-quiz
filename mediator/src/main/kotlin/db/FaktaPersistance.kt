package db

import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.søknad.Versjon
import java.time.LocalDateTime
import java.util.UUID

internal interface FaktaPersistance {
    fun ny(fnr: String, søknadType: Versjon.Type): Søknad
    fun hent(uuid: UUID, søknadType: Versjon.Type): Søknad
    fun lagre(fakta: Fakta): Boolean
    fun opprettede(fnr: String): Map<UUID, LocalDateTime>
}

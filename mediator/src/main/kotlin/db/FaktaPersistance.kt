package db

import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Versjon
import java.time.LocalDateTime
import java.util.UUID

internal interface FaktaPersistance {
    fun ny(fnr: String, søknadFaktagrupperType: Versjon.FaktagrupperType): Faktagrupper
    fun hent(uuid: UUID, søknadFaktagrupperType: Versjon.FaktagrupperType): Faktagrupper
    fun lagre(fakta: Fakta): Boolean
    fun opprettede(fnr: String): Map<LocalDateTime, UUID>
}

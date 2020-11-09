package db

import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.model.faktum.Søknad
import java.time.LocalDateTime
import java.util.UUID

internal interface SøknadPersistence {
    fun ny(fnr: String, type: Versjon.FaktagrupperType): Faktagrupper
    fun hent(uuid: UUID, type: Versjon.FaktagrupperType? = null): Faktagrupper
    fun lagre(søknad: Søknad, type: Versjon.FaktagrupperType): Boolean
    fun opprettede(fnr: String): Map<LocalDateTime, UUID>
}

package no.nav.dagpenger.quiz.mediator.db

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.HenvendelsesType
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.seksjon.Faktagrupper
import java.time.LocalDateTime
import java.util.UUID

interface SøknadPersistence {
    fun ny(identer: Identer, prosessVersjon: HenvendelsesType, uuid: UUID = UUID.randomUUID()): Faktagrupper
    fun hent(uuid: UUID): Faktagrupper
    fun lagre(fakta: Fakta): Boolean
    fun opprettede(identer: Identer): Map<LocalDateTime, UUID>
    fun slett(uuid: UUID): Boolean
    fun migrer(uuid: UUID, tilVersjon: HenvendelsesType? = null): HenvendelsesType
    fun eksisterer(uuid: UUID): Boolean
}

package no.nav.dagpenger.quiz.mediator.db

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.HenvendelsesType
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import java.time.LocalDateTime
import java.util.UUID

interface FaktaPersistence {
    fun ny(identer: Identer, prosessVersjon: HenvendelsesType, uuid: UUID = UUID.randomUUID()): Utredningsprosess
    fun hent(uuid: UUID): Utredningsprosess
    fun lagre(fakta: Fakta): Boolean
    fun opprettede(identer: Identer): Map<LocalDateTime, UUID>
    fun slett(uuid: UUID): Boolean
    fun migrer(uuid: UUID, tilVersjon: HenvendelsesType? = null): HenvendelsesType
    fun eksisterer(uuid: UUID): Boolean
}

package no.nav.dagpenger.quiz.mediator.db

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import java.time.LocalDateTime
import java.util.UUID

interface FaktaRepository {
    fun ny(identer: Identer, prosessVersjon: Faktaversjon, uuid: UUID = UUID.randomUUID()): Fakta
    fun hent(uuid: UUID): Utredningsprosess
    fun lagre(fakta: Fakta): Boolean
    fun opprettede(identer: Identer): Map<LocalDateTime, UUID>
    fun slett(uuid: UUID): Boolean
    fun migrer(uuid: UUID, tilVersjon: Faktaversjon? = null): Faktaversjon
    fun eksisterer(uuid: UUID): Boolean
}

package no.nav.dagpenger.quiz.mediator.db

import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Prosesstype
import java.util.UUID

interface ProsessRepository {
    fun addObserver(observer: ProsessRepositoryObserver) {}
    fun ny(
        identer: Identer,
        prosesstype: Prosesstype,
        uuid: UUID = UUID.randomUUID(),
        faktaUUID: UUID = UUID.randomUUID(),
    ): Prosess

    fun hent(uuid: UUID): Prosess
    fun lagre(prosess: Prosess): Boolean
    fun slett(uuid: UUID)
}

interface ProsessRepositoryObserver {
    fun slett(prosessUUID: UUID, faktaUUID: UUID)
}

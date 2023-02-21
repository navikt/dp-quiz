package no.nav.dagpenger.quiz.mediator.db

import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.seksjon.Prosess
import java.util.UUID

interface ProsessRepository {
    fun ny(identer: Identer, prosessversjon: Faktaversjon, uuid: UUID = UUID.randomUUID()): Prosess
    fun hent(uuid: UUID): Prosess
    fun lagre(prosess: Prosess): Boolean
}

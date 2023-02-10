package no.nav.dagpenger.quiz.mediator.db

import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import java.util.UUID

interface UtredningsprosessRepository {
    fun ny(identer: Identer, prosessversjon: Faktaversjon, uuid: UUID = UUID.randomUUID()): Utredningsprosess
    fun hent(uuid: UUID): Utredningsprosess
    fun lagre(utredningsprosess: Utredningsprosess): Boolean
}

package no.nav.dagpenger.quiz.mediator.meldinger

import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Prosesstype
import no.nav.dagpenger.model.seksjon.Prosessversjon
import no.nav.dagpenger.quiz.mediator.db.ProsessRepository
import no.nav.dagpenger.quiz.mediator.helpers.Testprosess
import java.util.UUID

internal class ProsessRepositoryFake : ProsessRepository {
    var prosess: Prosess? = null
    var hentet: Int = 0

    override fun ny(person: Identer, prosesstype: Prosesstype, uuid: UUID, faktaUUID: UUID) =
        Prosessversjon.id(prosesstype).utredningsprosess(
            person = Person(person),
            prosessUUID = uuid,
            faktaUUID = faktaUUID,
        )

    override fun hent(uuid: UUID) = prosess!!.also { hentet++ }

    override fun lagre(prosess: Prosess): Boolean {
        this.prosess = Prosessversjon.id(Testprosess.Test).utredningsprosess(prosess.fakta)
        return true
    }

    fun reset() {
        prosess = null
        hentet = 0
    }
}

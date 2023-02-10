package no.nav.dagpenger.quiz.mediator.meldinger

import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.UtredningsprosessRepository
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel
import java.util.UUID

internal class UtredningsprosessRepositoryFake : UtredningsprosessRepository {
    var utredningsprosess: Utredningsprosess? = null
    var hentet: Int = 0

    override fun ny(
        identer: Identer,
        faktaversjon: Faktaversjon,
        uuid: UUID,
    ): Utredningsprosess {
        val fakta = Versjon.id(SøknadEksempel.prosessVersjon).fakta(Person(identer))
        return Versjon.id(faktaversjon).utredningsprosess(fakta)
    }

    override fun hent(uuid: UUID) = utredningsprosess!!.also { hentet++ }

    override fun lagre(prosess: Utredningsprosess): Boolean {
        utredningsprosess = Versjon.id(SøknadEksempel.prosessVersjon).utredningsprosess(prosess.fakta)
        return true
    }

    fun reset() {
        utredningsprosess = null
        hentet = 0
    }
}

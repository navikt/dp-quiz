package no.nav.dagpenger.quiz.mediator.meldinger

import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.ProsessRepository
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel
import java.util.UUID

internal class ProsessRepositoryFake : ProsessRepository {
    var prosess: Prosess? = null
    var hentet: Int = 0

    override fun ny(
        identer: Identer,
        faktaversjon: Faktaversjon,
        uuid: UUID,
    ): Prosess {
        val fakta = Versjon.id(SøknadEksempel.prosessVersjon).fakta(Person(identer))
        return Versjon.id(faktaversjon).utredningsprosess(fakta)
    }

    override fun hent(uuid: UUID) = prosess!!.also { hentet++ }

    override fun lagre(prosess: Prosess): Boolean {
        this.prosess = Versjon.id(SøknadEksempel.prosessVersjon).utredningsprosess(prosess.fakta)
        return true
    }

    fun reset() {
        prosess = null
        hentet = 0
    }
}

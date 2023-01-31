package no.nav.dagpenger.quiz.mediator.meldinger

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.HenvendelsesType
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel
import java.time.LocalDateTime
import java.util.UUID

internal class SøknadPersistenceFake : SøknadPersistence {
    var utredningsprosess: Utredningsprosess? = null
    var hentet: Int = 0

    override fun ny(
        identer: Identer,
        prosessVersjon: HenvendelsesType,
        uuid: UUID
    ): Utredningsprosess =
        Versjon.id(SøknadEksempel.prosessVersjon).utredningsprosess(Person(identer))
            .also { utredningsprosess = it }

    override fun hent(uuid: UUID) = utredningsprosess!!.also { hentet++ }

    override fun lagre(fakta: Fakta): Boolean {
        utredningsprosess = Versjon.id(SøknadEksempel.prosessVersjon).utredningsprosess(fakta)
        return true
    }

    override fun opprettede(identer: Identer): Map<LocalDateTime, UUID> {
        TODO("Not yet implemented")
    }

    override fun slett(uuid: UUID): Boolean {
        TODO("Not yet implemented")
    }

    override fun migrer(uuid: UUID, tilVersjon: HenvendelsesType?): HenvendelsesType {
        TODO("Not yet implemented")
    }

    override fun eksisterer(uuid: UUID): Boolean {
        TODO("Not yet implemented")
    }

    fun reset() {
        utredningsprosess = null
        hentet = 0
    }
}

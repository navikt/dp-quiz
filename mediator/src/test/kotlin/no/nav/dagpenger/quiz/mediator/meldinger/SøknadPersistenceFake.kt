package no.nav.dagpenger.quiz.mediator.meldinger

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.HenvendelsesType
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.seksjon.Faktagrupper
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel
import java.time.LocalDateTime
import java.util.UUID

internal class SøknadPersistenceFake : SøknadPersistence {
    var faktagrupper: Faktagrupper? = null
    var hentet: Int = 0

    override fun ny(
        identer: Identer,
        prosessVersjon: HenvendelsesType,
        uuid: UUID
    ): Faktagrupper =
        Versjon.id(SøknadEksempel.prosessVersjon).søknadprosess(Person(identer))
            .also { faktagrupper = it }

    override fun hent(uuid: UUID) = faktagrupper!!.also { hentet++ }

    override fun lagre(fakta: Fakta): Boolean {
        faktagrupper = Versjon.id(SøknadEksempel.prosessVersjon).søknadprosess(fakta)
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
        faktagrupper = null
        hentet = 0
    }
}

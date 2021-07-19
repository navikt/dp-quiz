package no.nav.dagpenger.quiz.mediator.meldinger

import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.SøknadPersistence
import no.nav.dagpenger.quiz.mediator.helpers.SøknadEksempel
import java.time.LocalDateTime
import java.util.UUID

internal class SøknadPersistenceFake : SøknadPersistence {
    var søknadprosess: Søknadprosess? = null
    var hentet: Int = 0

    override fun ny(identer: Identer, type: Versjon.UserInterfaceType, versjonId: Int) =
        Versjon.id(versjonId).søknadprosess(Person(identer), type)
            .also { søknadprosess = it }

    override fun hent(uuid: UUID, type: Versjon.UserInterfaceType?) = søknadprosess!!.also { hentet++ }

    override fun lagre(søknad: Søknad): Boolean {
        søknadprosess = Versjon.id(SøknadEksempel.versjonId).søknadprosess(søknad, Versjon.UserInterfaceType.Web)
        return true
    }

    override fun opprettede(identer: Identer): Map<LocalDateTime, UUID> {
        TODO("Not yet implemented")
    }

    override fun lagreResultat(resultat: Boolean, søknad: Søknad, resultatJson: ObjectNode) {}

    override fun hentResultat(uuid: UUID): Boolean {
        TODO("Not yet implemented")
    }

    fun reset() {
        søknadprosess = null
        hentet = 0
    }
}

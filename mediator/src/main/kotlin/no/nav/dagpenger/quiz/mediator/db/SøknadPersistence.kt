package no.nav.dagpenger.quiz.mediator.db

import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import java.time.LocalDateTime
import java.util.UUID

internal interface SøknadPersistence {
    fun ny(fnr: String, type: Versjon.UserInterfaceType, versjonId: Int): Søknadprosess
    fun hent(uuid: UUID, type: Versjon.UserInterfaceType? = null): Søknadprosess
    fun lagre(søknad: Søknad): Boolean
    fun opprettede(fnr: String): Map<LocalDateTime, UUID>
}

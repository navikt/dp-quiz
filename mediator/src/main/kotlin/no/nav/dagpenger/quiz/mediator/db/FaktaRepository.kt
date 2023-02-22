package no.nav.dagpenger.quiz.mediator.db

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.seksjon.Prosesstype
import java.time.LocalDateTime
import java.util.UUID

interface FaktaRepository {
    // TODO: Kan vi klare oss med kun en av disse ny-funksjonene? Den med Faktaversjon brukes kun i en test.
    fun ny(identer: Identer, prosesstype: Prosesstype, uuid: UUID = UUID.randomUUID()): Fakta
    fun ny(identer: Identer, faktaversjon: Faktaversjon, uuid: UUID = UUID.randomUUID()): Fakta
    fun hent(uuid: UUID): Fakta
    fun lagre(fakta: Fakta): Boolean
    fun opprettede(identer: Identer): Map<LocalDateTime, UUID>
    fun slett(uuid: UUID): Boolean
    fun migrer(uuid: UUID, tilVersjon: Faktaversjon? = null): Faktaversjon
    fun eksisterer(uuid: UUID): Boolean
    fun ny(fakta: Fakta): Fakta
}

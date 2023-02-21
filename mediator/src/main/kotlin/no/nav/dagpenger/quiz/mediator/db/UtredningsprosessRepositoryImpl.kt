package no.nav.dagpenger.quiz.mediator.db

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.dagpenger.model.faktum.Faktatype
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.seksjon.Versjon
import java.util.UUID

class UtredningsprosessRepositoryImpl : UtredningsprosessRepository {
    private val faktaRepository = FaktaRecord()
    override fun ny(person: Identer, faktaversjon: Faktaversjon, uuid: UUID): Utredningsprosess {
        val fakta = faktaRepository.ny(person, faktaversjon, uuid)
        return Versjon.id(faktaversjon).utredningsprosess(fakta)
    }

    override fun hent(uuid: UUID): Utredningsprosess {
        val rad = sessionOf(PostgresDataSourceBuilder.dataSource).use { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    "SELECT soknad.person_id, versjon.navn , versjon.versjon_id FROM soknad JOIN v1_prosessversjon AS versjon ON (versjon.id = soknad.versjon_id) WHERE uuid = ?",
                    uuid,
                ).map { row ->
                    SoknadRad(row.string(1), row.string(2), row.int(3))
                }.asSingle,
            )
        } ?: throw IllegalArgumentException("Kan ikke hente en utredningsprosess som ikke finnes, uuid: $uuid")

        val versjonId = Faktaversjon(Prosess(rad.navn), rad.versjonId)
        val utredningsprosess = Versjon.id(versjonId).utredningsprosess(PersonRecord().hentPerson(rad.personId), uuid)
        return faktaRepository.rehydrerProsess(utredningsprosess)
    }

    override fun lagre(utredningsprosess: Utredningsprosess) = faktaRepository.lagre(utredningsprosess.fakta)

    private data class Prosess(override val id: String) : Faktatype
    private data class SoknadRad(val personId: UUID, val navn: String, val versjonId: Int) {
        constructor(personId: String, navn: String, versjonId: Int) : this(UUID.fromString(personId), navn, versjonId)
    }
}
